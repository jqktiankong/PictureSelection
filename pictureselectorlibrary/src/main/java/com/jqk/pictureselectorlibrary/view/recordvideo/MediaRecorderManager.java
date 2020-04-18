package com.jqk.pictureselectorlibrary.view.recordvideo;

import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.jqk.pictureselectorlibrary.util.AppConstant;
import com.jqk.pictureselectorlibrary.util.FileUtils;
import com.jqk.pictureselectorlibrary.util.L;
import com.jqk.pictureselectorlibrary.util.TrimVideoUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MediaRecorderManager {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static boolean recording = false;

    public static MediaRecorder mediaRecorder;
    public static File outputMediaFile;

    public static MediaRecorder getInstance() {
        return mediaRecorder;
    }

    public static void prepareAndStart(Camera.Size size, Surface surface, int cameraIndex) {
        if (!recording) {
            try {
                mediaRecorder = new MediaRecorder();

                // Step 1: Unlock and set camera to MediaRecorder
                CameraManager.getInstance().unlock();
                mediaRecorder.setCamera(CameraManager.getInstance());

                // Step 2: Set sources
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                if (cameraIndex == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mediaRecorder.setOrientationHint(90 + 180);
                } else if (cameraIndex == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mediaRecorder.setOrientationHint(90);
                }


                // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
                mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

                // Step 4: Set output file
                outputMediaFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
                mediaRecorder.setOutputFile(outputMediaFile.toString());
                mediaRecorder.setVideoSize(size.height, size.width);

                // Step 5: Set the preview output
                mediaRecorder.setPreviewDisplay(surface);

                // Step 6: Prepare configured MediaRecorder

                mediaRecorder.prepare();
                mediaRecorder.start();

                recording = true;

            } catch (IllegalStateException e) {
                L.d("IllegalStateException preparing MediaRecorder: " + e.getMessage());
                release();
            } catch (IOException e) {
                L.d("IOException preparing MediaRecorder: " + e.getMessage());
                release();
            } catch (Exception e) {
                L.d("Exception preparing MediaRecorder: " + e.getMessage());
                release();
            }
        }
    }

    public static void release() {
        if (recording) {
            if (mediaRecorder != null) {
                mediaRecorder.reset();   // clear recorder configuration
                mediaRecorder.release(); // release the recorder object
                mediaRecorder = null;
                CameraManager.getInstance().lock();           // lock camera for later use
                recording = false;
            }
        }
    }

    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    public static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath() + AppConstant.PATH_VIDEO_CACHE);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                L.d("failed to create directory");
                return null;
            }
        }

        L.d("mediaStorageDir = " + mediaStorageDir.getAbsolutePath());

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".flv");
        } else {
            return null;
        }

        outputMediaFile = mediaFile;

        return mediaFile;
    }

    public static File getOutputMediaFile() {
        return outputMediaFile;
    }

    public static void margeVideos(List<String> filelist) {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                String path = Environment.getExternalStorageDirectory().getPath() + AppConstant.PATH_FILELIST;
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }

                try {
                    file.createNewFile();
                    FileWriter fw = new FileWriter(file, true);
                    PrintWriter pw = new PrintWriter(fw);

                    for (String str : filelist) {
                        pw.println("file " + "\'" + str + "\'");   //字符串末尾不需要换行符
                    }

                    pw.flush();
                    pw.close();
                    fw.close();
                    emitter.onNext(file.getAbsolutePath());
                } catch (IOException e) {
                    emitter.onError(e);
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String string) {
                        String path = Environment.getExternalStorageDirectory().getPath() + AppConstant.PATH_VIDEO_CACHE;

                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        File mediaFile;
                        mediaFile = new File(path + File.separator +
                                "VID_" + timeStamp + ".mp4");

                        TrimVideoUtils.mergeVideos(string, mediaFile.getAbsolutePath(), new TrimVideoUtils.OnCallBack() {
                            @Override
                            public void onSuccess() {
                                L.d("onSuccess");
                            }

                            @Override
                            public void onFail() {
                                L.d("onFail");
                            }

                            @Override
                            public void onProgress(float progress) {
                                L.d("progress = " + progress);
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        L.d("margeVideos e = " + e.toString());
                    }

                    @Override
                    public void onComplete() {


                    }
                });

    }
}
