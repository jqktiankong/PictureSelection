package com.jqk.pictureselectorlibrary.view.recordvideo;

import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.text.TextUtils;

import com.jqk.pictureselectorlibrary.util.L;
import com.jqk.pictureselectorlibrary.view.record.data.FrameToRecord;
import com.jqk.pictureselectorlibrary.view.record.data.RecordFragment;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Thread.State.WAITING;

public class RecordManager {
    private static final int PREFERRED_PREVIEW_WIDTH = 960;
    private static final int PREFERRED_PREVIEW_HEIGHT = 540;

    private static RecordManager mInstance;

    // FFmpeg录制视频
    private FFmpegFrameRecorder mFrameRecorder;
    private AudioRecord mAudioRecord;
    private ShortBuffer audioData;


    private File mVideo;
    private int sampleAudioRateInHz = 44100;
    private int frameRate = 30;
    private int frameDepth = Frame.DEPTH_BYTE;
    private int frameChannels = 2;

    private int mFrameToRecordCount;
    private int mFrameRecordedCount;
    private long mTotalProcessFrameTime;

    private int mPreviewWidth = PREFERRED_PREVIEW_WIDTH;
    private int mPreviewHeight = PREFERRED_PREVIEW_HEIGHT;
    private volatile boolean mRecording = false;

    private LinkedBlockingQueue<FrameToRecord> mFrameToRecordQueue;
    private LinkedBlockingQueue<FrameToRecord> mRecycledFrameQueue;
    private Stack<RecordFragment> mRecordFragments;

    private VideoRecordThread mVideoRecordThread;
    private AudioRecordThread mAudioRecordThread;

    private long lastPreviewFrameTime;

    private FFmpegFrameFilter frameFilter;

    public static RecordManager getInstance() {
        if (mInstance == null) {
            mInstance = new RecordManager();
        }
        return mInstance;
    }

    public RecordManager() {

    }


    public void initRecorder(int videoWidth, int videoHeight, int selectedCameraIndex) {
        mVideo = MediaRecorderManager.getOutputMediaFile(MediaRecorderManager.MEDIA_TYPE_VIDEO);
        L.d("Output Video: " + mVideo);

        // 旋转之后的屏幕宽高
        mFrameRecorder = new FFmpegFrameRecorder(mVideo, videoWidth, videoHeight, 1);
        mFrameRecorder.setFormat("mp4");
        mFrameRecorder.setSampleRate(sampleAudioRateInHz);
        mFrameRecorder.setFrameRate(frameRate);

        // Use H264
        mFrameRecorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        // See: https://trac.ffmpeg.org/wiki/Encode/H.264#crf
        /*
         * The range of the quantizer scale is 0-51: where 0 is lossless, 23 is default, and 51 is worst possible. A lower value is a higher quality and a subjectively sane range is 18-28. Consider 18 to be visually lossless or nearly so: it should look the same or nearly the same as the input but it isn't technically lossless.
         * The range is exponential, so increasing the CRF value +6 is roughly half the bitrate while -6 is roughly twice the bitrate. General usage is to choose the highest CRF value that still provides an acceptable quality. If the output looks good, then try a higher value and if it looks bad then choose a lower value.
         */

        mFrameRecorder.setVideoOption("crf", "28");
        mFrameRecorder.setVideoOption("preset", "superfast");
        mFrameRecorder.setVideoOption("tune", "zerolatency");

        // At most buffer 10 Frame
        mFrameToRecordQueue = new LinkedBlockingQueue<>(10);
        // At most recycle 2 Frame
        mRecycledFrameQueue = new LinkedBlockingQueue<>(2);
        mRecordFragments = new Stack<>();

        initFrameFilter(selectedCameraIndex);

        try {
            mFrameRecorder.start();
        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
        }

        mAudioRecordThread = new AudioRecordThread();
        mAudioRecordThread.start();
        mVideoRecordThread = new VideoRecordThread();
        mVideoRecordThread.start();
    }

    public void startRecord() {
        if (!mRecording) {
            RecordFragment recordFragment = new RecordFragment();
            recordFragment.setStartTimestamp(System.currentTimeMillis());
            mRecordFragments.push(recordFragment);
            mRecording = true;
        }

        try {
            mAudioRecord.startRecording();
            mFrameRecorder.start();
        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        if (mRecording) {
            mRecordFragments.peek().setEndTimestamp(System.currentTimeMillis());
            mRecording = false;
        }

        frameFilterStop();

        if (mAudioRecordThread != null) {
            if (mAudioRecordThread.isRunning()) {
                mAudioRecordThread.stopRunning();
            }
        }

        if (mVideoRecordThread != null) {
            if (mVideoRecordThread.isRunning()) {
                mVideoRecordThread.stopRunning();
            }
        }

        try {
            if (mAudioRecordThread != null) {
                mAudioRecordThread.join();
            }
            if (mVideoRecordThread != null) {
                mVideoRecordThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mAudioRecordThread = null;
        mVideoRecordThread = null;


        mFrameToRecordQueue.clear();
        mRecycledFrameQueue.clear();

        if (mFrameRecorder != null) {
            try {
                mFrameRecorder.stop();
            } catch (FFmpegFrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }

        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }

        mRecordFragments.clear();
    }

    public void pauseRecord(boolean started, boolean recording, int selectedCameraIndex) {
        if (started) {
            mRecording = recording;

            if (mRecording) {
                initFrameFilter(selectedCameraIndex);
                frameFilterStart();
            } else {
                frameFilterStop();
            }
        }
    }


    public void onPreviewFrame(byte[] data, Camera camera) {
        long thisPreviewFrameTime = System.currentTimeMillis();
        if (lastPreviewFrameTime > 0) {
            L.d("Preview frame interval: " + (thisPreviewFrameTime - lastPreviewFrameTime) + "ms");
        }
        lastPreviewFrameTime = thisPreviewFrameTime;

        // get video data
        if (mRecording) {
            if (mAudioRecordThread == null || !mAudioRecordThread.isRunning()) {
                // wait for AudioRecord to init and start
                mRecordFragments.peek().setStartTimestamp(System.currentTimeMillis());
            } else {
                // pop the current record fragment when calculate total recorded time
                RecordFragment curFragment = mRecordFragments.pop();
                long recordedTime = calculateTotalRecordedTime(mRecordFragments);
                // push it back after calculation
                mRecordFragments.push(curFragment);
                long curRecordedTime = System.currentTimeMillis()
                        - curFragment.getStartTimestamp() + recordedTime;
                L.d("curRecordedTime = " + curRecordedTime);
                long timestamp = 1000 * curRecordedTime;
                Frame frame;
                FrameToRecord frameToRecord = mRecycledFrameQueue.poll();
                if (frameToRecord != null) {
                    frame = frameToRecord.getFrame();
                    frameToRecord.setTimestamp(timestamp);
                } else {
                    frame = new Frame(mPreviewWidth, mPreviewHeight, frameDepth, frameChannels);
                    frameToRecord = new FrameToRecord(timestamp, frame);
                }
                ((ByteBuffer) frame.image[0].position(0)).put(data);

                if (mFrameToRecordQueue.offer(frameToRecord)) {
                    L.d("mFrameToRecordCount = " + mFrameToRecordCount);
                    mFrameToRecordCount++;
                }
            }
        }
        camera.addCallbackBuffer(data);
    }

    private long calculateTotalRecordedTime(Stack<RecordFragment> recordFragments) {
        long recordedTime = 0;
        for (RecordFragment recordFragment : recordFragments) {
            recordedTime += recordFragment.getDuration();
        }
        return recordedTime;
    }

    class AudioRecordThread extends RunningThread {

        public AudioRecordThread() {
            int bufferSize = AudioRecord.getMinBufferSize(sampleAudioRateInHz,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleAudioRateInHz,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            audioData = ShortBuffer.allocate(bufferSize);
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            isRunning = true;
            /* ffmpeg_audio encoding loop */
            while (isRunning) {
                if (mRecording && mFrameRecorder != null) {
                    int bufferReadResult = mAudioRecord.read(audioData.array(), 0, audioData.capacity());
                    audioData.limit(bufferReadResult);
                    if (bufferReadResult > 0) {
                        L.d("bufferReadResult: " + bufferReadResult);
                        try {
                            mFrameRecorder.recordSamples(audioData);
                        } catch (FFmpegFrameRecorder.Exception e) {
                            L.d(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void initFrameFilter(int selectedCameraIndex) {
        // 这里只做方向的设置
        int previewWidth = mPreviewWidth;
        int previewHeight = mPreviewHeight;

        List<String> filters = new ArrayList<>();
        // Transpose
        String transpose = null;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(selectedCameraIndex, info);
        switch (info.orientation) {
            case 270:
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    transpose = "transpose=clock_flip"; // Same as preview display
                } else {
                    transpose = "transpose=cclock"; // Mirrored horizontally as preview display
                }
                break;
            case 90:
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    transpose = "transpose=cclock_flip"; // Same as preview display
                } else {
                    transpose = "transpose=clock"; // Mirrored horizontally as preview display
                }
                break;
        }
//            // transpose
        if (transpose != null) {
            filters.add(transpose);
        }

        frameFilter = new FFmpegFrameFilter(TextUtils.join(",", filters),
                previewWidth, previewHeight);
        frameFilter.setPixelFormat(avutil.AV_PIX_FMT_NV21);
        frameFilter.setFrameRate(frameRate);
    }

    public void frameFilterStart() {
        if (frameFilter != null) {
            try {
                frameFilter.start();
            } catch (FrameFilter.Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void frameFilterStop() {
        if (frameFilter != null) {
            try {
                frameFilter.stop();
            } catch (FrameFilter.Exception e) {
                e.printStackTrace();
            }
        }
    }

    class VideoRecordThread extends RunningThread {
        @Override
        public void run() {

            frameFilterStart();

            isRunning = true;
            FrameToRecord recordedFrame;

            while (isRunning || !mFrameToRecordQueue.isEmpty()) {
                try {
                    recordedFrame = mFrameToRecordQueue.take();
                } catch (InterruptedException ie) {
                    frameFilterStop();
                    break;
                }

                if (mFrameRecorder != null) {
                    long timestamp = recordedFrame.getTimestamp();
                    L.d("timestamp = " + timestamp);
                    if (timestamp > mFrameRecorder.getTimestamp()) {
                        mFrameRecorder.setTimestamp(timestamp);
                    }
                    long startTime = System.currentTimeMillis();
//                    Frame filteredFrame = recordedFrame.getFrame();
                    Frame filteredFrame = null;
                    try {
                        frameFilter.push(recordedFrame.getFrame());
                        filteredFrame = frameFilter.pull();
                    } catch (FrameFilter.Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        mFrameRecorder.record(filteredFrame);
                    } catch (FFmpegFrameRecorder.Exception e) {
                        e.printStackTrace();
                    }
                    long endTime = System.currentTimeMillis();
                    long processTime = endTime - startTime;
                    mTotalProcessFrameTime += processTime;
                    L.d("This frame process time: " + processTime + "ms");
                    long totalAvg = mTotalProcessFrameTime / ++mFrameRecordedCount;
                    L.d("Avg frame process time: " + totalAvg + "ms");
                }
                L.d(mFrameRecordedCount + " / " + mFrameToRecordCount);
                mRecycledFrameQueue.offer(recordedFrame);
            }
        }

        public void stopRunning() {
            super.stopRunning();
            if (getState() == WAITING) {
                interrupt();
            }
        }
    }

    class RunningThread extends Thread {
        boolean isRunning;

        public boolean isRunning() {
            return isRunning;
        }

        public void stopRunning() {
            this.isRunning = false;
        }
    }
}
