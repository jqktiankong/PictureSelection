package com.jqk.pictureselectorlibrary.view.recordvideo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
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
import com.jqk.pictureselectorlibrary.util.TrimVideoUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecordVideoActivity extends AppCompatActivity {
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
    private int parentViewWidth, parentViewHeight;
    private Camera.Size size;

    private List<String> fileList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_recordvideo);

        surfaceView = findViewById(R.id.surface_view);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        switchCamera = findViewById(R.id.switch_camera);
        focusView = findViewById(R.id.focus_view);
        parentView = findViewById(R.id.parent_view);

        fileList = new ArrayList<>();

        getCameraInfo();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileList = new ArrayList<>();
                MediaRecorderManager.prepareAndStart(size, surfaceView.getHolder().getSurface(), selectedCameraIndex);
                fileList.add(MediaRecorderManager.getOutputMediaFile().getAbsolutePath());
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaRecorderManager.release();
                // 刷新文件管理器
                Uri localUri = Uri.fromFile(MediaRecorderManager.getOutputMediaFile());
                Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
                sendBroadcast(localIntent);

                L.d("filelist = " + fileList.toString());

                MediaRecorderManager.margeVideos(fileList);
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

        if (cameraCnt == 0) {
            L.d("没有可用的摄像头");
        } else {
            if (backCameraIndex != -1) {
                selectedCameraIndex = backCameraIndex;
                startCamera(backCameraIndex);
            } else {
                if (fontCameraIndex != -1) {
                    selectedCameraIndex = fontCameraIndex;
                    startCamera(fontCameraIndex);
                } else {
                    L.d("没有前后摄像头");
                }
            }
        }

//        TrimVideoUtils.mergeVideos("/storage/emulated/0/123vidwocache/filelist.txt", "/storage/emulated/0/123vidwocache/123.mp4", new TrimVideoUtils.OnCallBack() {
//            @Override
//            public void onSuccess() {
//                L.d("onSuccess");
//            }
//
//            @Override
//            public void onFail() {
//                L.d("onFail");
//            }
//
//            @Override
//            public void onProgress(float progress) {
//                L.d("progress = " + progress);
//            }
//        });

//        TrimVideoUtils.vflipVideo(new TrimVideoUtils.OnCallBack() {
//            @Override
//            public void onSuccess() {
//                L.d("onSuccess");
//            }
//
//            @Override
//            public void onFail() {
//                L.d("onFail");
//            }
//
//            @Override
//            public void onProgress(float progress) {
//                L.d("progress = " + progress);
//            }
//        });

//        TrimVideoUtils.test1(new TrimVideoUtils.OnCallBack() {
//            @Override
//            public void onSuccess() {
//                L.d("onSuccess");
//            }
//
//            @Override
//            public void onFail() {
//                L.d("onFail");
//            }
//
//            @Override
//            public void onProgress(float progress) {
//                L.d("progress = " + progress);
//            }
//        });

//        TrimVideoUtils.test2(new TrimVideoUtils.OnCallBack() {
//            @Override
//            public void onSuccess() {
//                L.d("onSuccess");
//            }
//
//            @Override
//            public void onFail() {
//                L.d("onFail");
//            }
//
//            @Override
//            public void onProgress(float progress) {
//                L.d("progress = " + progress);
//            }
//        });

//        TrimVideoUtils.mergeMP4();

//        TrimVideoUtils.test3(new TrimVideoUtils.OnCallBack() {
//            @Override
//            public void onSuccess() {
//                L.d("onSuccess");
//            }
//
//            @Override
//            public void onFail() {
//                L.d("onFail");
//            }
//
//            @Override
//            public void onProgress(float progress) {
//                L.d("progress = " + progress);
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaRecorderManager.release();
        CameraManager.stopAndRelease();
    }

    public void startCamera(int cameraIndex) {
        if (surfaceView != null) {
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        CameraManager.open(cameraIndex);
                        CameraManager.setPreviewDisplay(surfaceView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    parentViewWidth = parentView.getWidth();
                    parentViewHeight = parentView.getHeight();


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

                    ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
                    lp.width = parentViewWidth;
                    lp.height = parentViewHeight;

                    surfaceView.setLayoutParams(lp);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    cameraWidth = width;
                    cameraHeight = height;

                    L.d("cameraWidth = " + width);
                    L.d("cameraHeight = " + height);

                    CameraManager.setParameters(size);
                    CameraManager.startPreview();
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
                    MediaRecorderManager.release();
                    CameraManager.stopAndRelease();
                    CameraManager.open(backCameraIndex);
                    CameraManager.setPreviewDisplay(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                CameraManager.setParameters(size);
                CameraManager.startPreview();
                MediaRecorderManager.prepareAndStart(size, surfaceView.getHolder().getSurface(), selectedCameraIndex);
                fileList.add(MediaRecorderManager.getOutputMediaFile().getAbsolutePath());

            } else if (selectedCameraIndex == backCameraIndex) {
                selectedCameraIndex = fontCameraIndex;

                try {
                    MediaRecorderManager.release();
                    CameraManager.stopAndRelease();
                    CameraManager.open(fontCameraIndex);
                    CameraManager.setPreviewDisplay(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                CameraManager.setParameters(size);
                CameraManager.startPreview();
                MediaRecorderManager.prepareAndStart(size, surfaceView.getHolder().getSurface(), selectedCameraIndex);
                fileList.add(MediaRecorderManager.getOutputMediaFile().getAbsolutePath());
            } else {
                L.d("切换前后摄像头失败");
            }
        }

    }

    public void getCameraInfo() {
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
    }
}
