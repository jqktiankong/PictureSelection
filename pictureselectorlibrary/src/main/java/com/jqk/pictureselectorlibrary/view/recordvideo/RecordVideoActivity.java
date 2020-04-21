package com.jqk.pictureselectorlibrary.view.recordvideo;

import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jqk.pictureselectorlibrary.R;
import com.jqk.pictureselectorlibrary.util.L;
import com.jqk.pictureselectorlibrary.view.record.data.FrameToRecord;
import com.jqk.pictureselectorlibrary.view.record.data.RecordFragment;
import com.jqk.pictureselectorlibrary.view.record.util.CameraHelper;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Thread.State.WAITING;

public class RecordVideoActivity extends AppCompatActivity {
    private static final int PREFERRED_PREVIEW_WIDTH = 1920;
    private static final int PREFERRED_PREVIEW_HEIGHT = 1080;

    // both in milliseconds
    private static final long MIN_VIDEO_LENGTH = 1 * 1000;
    private static final long MAX_VIDEO_LENGTH = 90 * 1000;

    private SurfaceView surfaceView;
    private Button start;
    private Button stop;
    private Button switchCamera;
    private FocusView focusView;
    private LinearLayout parentView;

    private int fontCameraIndex = -1;
    private int backCameraIndex = -1;
    private int cameraCnt = 0;
    private int selectedCameraIndex = -1;

    private int cameraWidth, cameraHeight;
    private Camera.Size size;

    // FFmpeg录制视频
    private FFmpegFrameRecorder mFrameRecorder;
    private VideoRecordThread mVideoRecordThread;
    private AudioRecordThread mAudioRecordThread;
    private LinkedBlockingQueue<FrameToRecord> mFrameToRecordQueue;
    private LinkedBlockingQueue<FrameToRecord> mRecycledFrameQueue;
    private int mPreviewWidth = PREFERRED_PREVIEW_WIDTH;
    private int mPreviewHeight = PREFERRED_PREVIEW_HEIGHT;
    private volatile boolean mRecording = false;
    private int mFrameToRecordCount;
    private int mFrameRecordedCount;
    private long mTotalProcessFrameTime;
    private Stack<RecordFragment> mRecordFragments;
    private File mVideo;
    private int videoWidth = 0;
    private int videoHeight = 0;
    private int videoZoom = 1;
    private int sampleAudioRateInHz = 44100;
    private int frameRate = 30;
    private int frameDepth = Frame.DEPTH_BYTE;
    private int frameChannels = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_recordvideo);

        init();
        initView();

        initCameraInfo();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resumeRecording();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseRecording();
                stopRecording();
                stopRecorder();
                // 刷新文件管理器
                Uri localUri = Uri.fromFile(MediaRecorderManager.getOutputMediaFile());
                Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
                sendBroadcast(localIntent);

            }
        });

        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        focusView.setOnFocusViewCallback(new FocusView.OnFocusViewCallback() {
            @Override
            public void onFocus(Rect rect) {
                float xScale = (float) 2000 / cameraWidth;
                float yScale = (float) 2000 / cameraHeight;

                L.d("xScale = " + xScale);
                L.d("yScale = " + yScale);

                float left = rect.left;
                float top = rect.top;
                float right = rect.right;
                float bottom = rect.bottom;

                if (left < cameraWidth / 2) {
                    left = -(cameraWidth / 2 - left);
                } else {
                    left = left - cameraWidth / 2;
                }

                if (right < cameraWidth / 2) {
                    right = -(cameraWidth / 2 - right);
                } else {
                    right = right - cameraWidth / 2;
                }

                if (top < cameraHeight / 2) {
                    top = -(cameraHeight / 2 - top);
                } else {
                    top = top - cameraHeight / 2;
                }

                if (bottom < cameraHeight / 2) {
                    bottom = -(cameraHeight / 2 - bottom);
                } else {
                    bottom = bottom - cameraHeight / 2;
                }

                L.d("left = " + left);
                L.d("top = " + top);
                L.d("right = " + right);
                L.d("bottom = " + bottom);

                left = left * xScale;
                top = top * xScale;
                right = right * xScale;
                bottom = bottom * xScale;

                if (left > 1000) {
                    left = 1000;
                }

                if (left < -1000) {
                    left = -1000;
                }

                if (top > 1000) {
                    top = 1000;
                }

                if (top < -1000) {
                    top = -1000;
                }

                if (right > 1000) {
                    right = 1000;
                }

                if (right < -1000) {
                    right = -1000;
                }

                if (bottom > 1000) {
                    bottom = 1000;
                }

                if (bottom < -1000) {
                    bottom = -1000;
                }

                Rect focusRect = new Rect((int) (left), (int) (top), (int) (right), (int) (bottom));
                L.d("------------------");
                L.d("left = " + (left));
                L.d("top = " + (top));
                L.d("right = " + (right));
                L.d("bottom = " + (bottom));

                CameraManager.setFocus(focusRect);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraManager.stopAndRelease();
    }

    public void init() {
        // At most buffer 10 Frame
        mFrameToRecordQueue = new LinkedBlockingQueue<>(1024);
        // At most recycle 2 Frame
        mRecycledFrameQueue = new LinkedBlockingQueue<>(1024);
        mRecordFragments = new Stack<>();
    }

    public void initView() {
        surfaceView = findViewById(R.id.surface_view);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        switchCamera = findViewById(R.id.switch_camera);
        focusView = findViewById(R.id.focus_view);
        parentView = findViewById(R.id.parent_view);
    }

    public void openCamera(int cameraIndex) {
        if (surfaceView != null) {
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    CameraManager.open(cameraIndex);

                    try {

                        SurfaceHolder surfaceHolder = surfaceView.getHolder();
                        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

                        CameraManager.setPreviewDisplay(surfaceHolder);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    int parentViewWidth = parentView.getWidth();
                    int parentViewHeight = parentView.getHeight();


                    size = CameraManager.getOptimalSize(parentViewHeight, parentViewWidth);
//                    // 竖屏交换宽高
                    int a = size.width;
                    int b = size.height;
                    size.width = b;
                    size.height = a;

                    L.d("size.width = " + size.width);
                    L.d("size.height = " + size.height);
                    // 防止图像变形
                    if (parentViewWidth > size.width) {
                        parentViewHeight = size.height / size.width * parentViewWidth;
                    } else if (parentViewHeight > size.height) {
                        parentViewWidth = (int) (((float) size.width) / size.height * parentViewHeight);
                    }

                    L.d("处理后parentViewWidth = " + parentViewWidth);
                    L.d("处理后parentViewHeight = " + parentViewHeight);

//                    mPreviewWidth = parentViewWidth;
//                    mPreviewHeight = parentViewHeight;

                    ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
                    lp.width = parentViewWidth;
                    lp.height = parentViewHeight;

                    surfaceView.setLayoutParams(lp);

                    videoWidth = parentViewWidth / videoZoom;
                    videoHeight = parentViewHeight / videoZoom;

                    initRecorder();
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    cameraWidth = width;
                    cameraHeight = height;

                    L.d("cameraWidth = " + width);
                    L.d("cameraHeight = " + height);

                    CameraManager.setParameters(size);

                    startPreview();


                    startRecorder();
                    startRecording();

//                    CameraManager.getInstance().startPreview();
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    CameraManager.stopAndRelease();
                }
            });
        } else {
            L.d("打开摄像头失败");
        }
    }

    public void switchCamera() {
        if (cameraCnt <= 1) {
            L.d("只有一个摄像头");
        } else {
            if (selectedCameraIndex == fontCameraIndex) {
                selectedCameraIndex = backCameraIndex;

                try {
                    CameraManager.stopAndRelease();
                    CameraManager.open(backCameraIndex);
                    CameraManager.setPreviewDisplay(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                CameraManager.setParameters(size);
                CameraManager.startPreview();

            } else if (selectedCameraIndex == backCameraIndex) {
                selectedCameraIndex = fontCameraIndex;

                try {
                    CameraManager.stopAndRelease();
                    CameraManager.open(fontCameraIndex);
                    CameraManager.setPreviewDisplay(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                CameraManager.setParameters(size);
                CameraManager.startPreview();
            } else {
                L.d("切换前后摄像头失败");
            }
        }

    }

    public void initCameraInfo() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCnt = Camera.getNumberOfCameras();

        for (int i = 0; i < cameraCnt; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                fontCameraIndex = i;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backCameraIndex = i;

            }
        }

        if (cameraCnt == 0) {
            L.d("没有可用的摄像头");
            return;
        } else {
            if (backCameraIndex != -1) {
                selectedCameraIndex = backCameraIndex;
            } else {
                if (fontCameraIndex != -1) {
                    selectedCameraIndex = fontCameraIndex;
                } else {
                    L.d("没有前后摄像头");
                    return;
                }
            }
        }

        openCamera(backCameraIndex);
    }

    private void initRecorder() {
        L.d("init mFrameRecorder");

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

        L.d("mFrameRecorder initialize success");
    }

    private void startPreview() {
        if (CameraManager.getInstance() == null) {
            L.d("startPreview return");
            return;
        }

        // YCbCr_420_SP (NV21) format
        byte[] bufferByte = new byte[mPreviewWidth * mPreviewHeight * 3 / 2];
        CameraManager.getInstance().addCallbackBuffer(bufferByte);
        CameraManager.getInstance().setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {

            int i = 0;

            private long lastPreviewFrameTime;

            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                i++;
                L.d("onPreviewFrame = " + i);
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
                CameraManager.getInstance().addCallbackBuffer(data);
            }
        });

        CameraManager.getInstance().startPreview();
    }

    private void stopPreview() {
        if (CameraManager.getInstance() != null) {
            CameraManager.getInstance().stopPreview();
            CameraManager.getInstance().setPreviewCallbackWithBuffer(null);
        }
    }

    private void startRecorder() {
        try {
            mFrameRecorder.start();
        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecorder() {
        if (mFrameRecorder != null) {
            try {
                mFrameRecorder.stop();
            } catch (FFmpegFrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }

        mRecordFragments.clear();
    }

    private void resumeRecording() {
        if (!mRecording) {
            RecordFragment recordFragment = new RecordFragment();
            recordFragment.setStartTimestamp(System.currentTimeMillis());
            mRecordFragments.push(recordFragment);
            mRecording = true;
        }
    }

    private void startRecording() {
        mAudioRecordThread = new AudioRecordThread();
        mAudioRecordThread.start();
        mVideoRecordThread = new VideoRecordThread();
        mVideoRecordThread.start();
    }

    private void stopRecording() {
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
    }

    private void pauseRecording() {
        if (mRecording) {
            mRecordFragments.peek().setEndTimestamp(System.currentTimeMillis());
            mRecording = false;
        }
    }

    private long calculateTotalRecordedTime(Stack<RecordFragment> recordFragments) {
        long recordedTime = 0;
        for (RecordFragment recordFragment : recordFragments) {
            recordedTime += recordFragment.getDuration();
        }
        return recordedTime;
    }

    class AudioRecordThread extends RunningThread {
        private AudioRecord mAudioRecord;
        private ShortBuffer audioData;

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

            L.d("mAudioRecord startRecording");
            mAudioRecord.startRecording();

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
            L.d("mAudioRecord stopRecording");
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
            L.d("mAudioRecord released");
        }
    }

    class VideoRecordThread extends RunningThread {
        @Override
        public void run() {
            int previewWidth = mPreviewWidth;
            int previewHeight = mPreviewHeight;

            List<String> filters = new ArrayList<>();
            // Transpose
            String transpose = null;
            String hflip = null;
            String vflip = null;
            String crop = null;
            String scale = null;
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
//            // horizontal flip
//            if (hflip != null) {
//                filters.add("vflip");
//            }
//            // vertical flip
//            if (vflip != null) {
//                filters.add(vflip);
//            }
//            // crop
//            if (crop != null) {
//                filters.add(crop);
//            }
            // scale (to designated size)
//            if (scale != null) {
//                filters.add(scale);
//            }


            FFmpegFrameFilter frameFilter = new FFmpegFrameFilter(TextUtils.join(",", filters),
                    previewWidth, previewHeight);
            frameFilter.setPixelFormat(avutil.AV_PIX_FMT_NV21);
            frameFilter.setFrameRate(frameRate);
            try {
                frameFilter.start();
            } catch (FrameFilter.Exception e) {
                e.printStackTrace();
            }

            isRunning = true;
            FrameToRecord recordedFrame;


            while (isRunning || !mFrameToRecordQueue.isEmpty()) {
                try {
                    recordedFrame = mFrameToRecordQueue.take();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                    try {
                        frameFilter.stop();
                    } catch (FrameFilter.Exception e) {
                        e.printStackTrace();
                    }
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
