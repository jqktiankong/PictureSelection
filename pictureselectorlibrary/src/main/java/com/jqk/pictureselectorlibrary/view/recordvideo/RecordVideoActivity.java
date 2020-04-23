package com.jqk.pictureselectorlibrary.view.recordvideo;

import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.jqk.pictureselectorlibrary.view.camera.CameraHandlerThread;


import java.io.IOException;

public class RecordVideoActivity extends AppCompatActivity implements FocusView.OnFocusViewCallback {
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

    private boolean started = false;

    // 相机控制线程
    private CameraHandlerThread mCameraThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_recordvideo);

        mCameraThread = new CameraHandlerThread();

        initView();

        initCameraInfo();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                started = true;
                RecordManager.getInstance().startRecord();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                started = false;
                RecordManager.getInstance().stopRecord();
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

        focusView.setOnFocusViewCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        openCamera(selectedCameraIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraThread.releaseCamera();
        mCameraThread.destoryThread();
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
                    mCameraThread.openCamera(selectedCameraIndex);
                    mCameraThread.setPreviewSurface(holder);

                    try {

                        SurfaceHolder surfaceHolder = surfaceView.getHolder();
                        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

                        CameraManager.setPreviewDisplay(surfaceHolder);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    int parentViewWidth = parentView.getWidth();
                    int parentViewHeight = parentView.getHeight();


//                    size = CameraManager.getOptimalSize(parentViewHeight, parentViewWidth);
////                    // 竖屏交换宽高
//                    int a = size.width;
//                    int b = size.height;
//                    size.width = b;
//                    size.height = a;
//
//                    L.d("size.width = " + size.width);
//                    L.d("size.height = " + size.height);
//                    // 防止图像变形
//                    if (parentViewWidth > size.width) {
//                        parentViewHeight = size.height / size.width * parentViewWidth;
//                    } else if (parentViewHeight > size.height) {
//                        parentViewWidth = (int) (((float) size.width) / size.height * parentViewHeight);
//                    }

                    L.d("处理后parentViewWidth = " + parentViewWidth);
                    L.d("处理后parentViewHeight = " + parentViewHeight);

//                    mPreviewWidth = parentViewWidth;
//                    mPreviewHeight = parentViewHeight;

                    ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
                    lp.width = parentViewWidth;
                    lp.height = parentViewHeight;

                    surfaceView.setLayoutParams(lp);

                    RecordManager.getInstance().initRecorder(540, 960, selectedCameraIndex);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    cameraWidth = width;
                    cameraHeight = height;

                    L.d("cameraWidth = " + width);
                    L.d("cameraHeight = " + height);

                    byte[] bufferByte = new byte[960 * 540 * 3 / 2];

                    mCameraThread.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {

                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            RecordManager.getInstance().onPreviewFrame(data, camera);
                        }
                    }, bufferByte);

                    mCameraThread.startPreview();
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    CameraManager.stopAndRelease();
                    mCameraThread.stopPreview();
                    mCameraThread.releaseCamera();
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

                RecordManager.getInstance().pauseRecord(started, false, selectedCameraIndex);

                mCameraThread.stopPreview();
                mCameraThread.releaseCamera();
                mCameraThread.openCamera(backCameraIndex);
                mCameraThread.setPreviewSurface(surfaceView.getHolder());
                byte[] bufferByte = new byte[960 * 540 * 3 / 2];

                mCameraThread.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {

                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        RecordManager.getInstance().onPreviewFrame(data, camera);
                    }
                }, bufferByte);

                mCameraThread.startPreview();

                RecordManager.getInstance().pauseRecord(started, true, selectedCameraIndex);

            } else if (selectedCameraIndex == backCameraIndex) {
                selectedCameraIndex = fontCameraIndex;

                RecordManager.getInstance().pauseRecord(started, false, selectedCameraIndex);

                mCameraThread.stopPreview();
                mCameraThread.releaseCamera();
                mCameraThread.openCamera(fontCameraIndex);
                mCameraThread.setPreviewSurface(surfaceView.getHolder());
                byte[] bufferByte = new byte[960 * 540 * 3 / 2];

                mCameraThread.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {

                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        RecordManager.getInstance().onPreviewFrame(data, camera);
                    }
                }, bufferByte);

                mCameraThread.startPreview();

                RecordManager.getInstance().pauseRecord(started, true, selectedCameraIndex);
            } else {
                L.d("切换前后摄像头失败");
            }
        }
    }

    public void initCameraInfo() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCnt = Camera.getNumberOfCameras();

        if (cameraCnt == 0) {
            L.d("没有可用的摄像头");
            return;
        }

        for (int i = 0; i < cameraCnt; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                fontCameraIndex = i;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backCameraIndex = i;
            }
        }

        if (backCameraIndex != -1) {
            selectedCameraIndex = backCameraIndex;
            return;
        }

        if (fontCameraIndex != -1) {
            selectedCameraIndex = fontCameraIndex;
            return;
        }
    }

//    private void pauseRecording() {
//        if (mRecording) {
//            mRecordFragments.peek().setEndTimestamp(System.currentTimeMillis());
//            mRecording = false;
//        }
//    }

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
}
