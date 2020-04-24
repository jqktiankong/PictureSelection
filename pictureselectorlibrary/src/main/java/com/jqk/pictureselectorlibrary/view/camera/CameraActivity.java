package com.jqk.pictureselectorlibrary.view.camera;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jqk.pictureselectorlibrary.R;
import com.jqk.pictureselectorlibrary.util.L;

public class CameraActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private LinearLayout parentView;
    // 相机控制线程
    private CameraHandlerThread mCameraThread;

    private int fontCameraIndex = -1;
    private int backCameraIndex = -1;
    private int cameraCnt = 0;
    private int selectedCameraIndex = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        surfaceView = findViewById(R.id.surface_view);
        parentView = findViewById(R.id.parent_view);

        mCameraThread = new CameraHandlerThread();

        initCameraInfo();

        openCamera(selectedCameraIndex);
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
    }

    public void openCamera(int cameraIndex) {
        if (surfaceView != null) {
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {

                    int parentViewWidth = parentView.getWidth();
                    int parentViewHeight = parentView.getHeight();

                    L.d("parentViewWidth = " + parentViewWidth);
                    L.d("parentViewHeight = " + parentViewHeight);
                    L.d("parentViewWidth / parentViewHeight = " + parentViewWidth / (float) parentViewHeight);

                    CameraManager.getInstance().setSurfaceViewWidth(parentViewWidth);
                    CameraManager.getInstance().setSurfaceViewHeight(parentViewHeight);

                    mCameraThread.openCamera(selectedCameraIndex);
                    mCameraThread.setPreviewSurface(holder);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                    byte[] bufferByte = new byte[CameraManager.getInstance().getPreviewWidth() * CameraManager.getInstance().getPreviewHeight() * 3 / 2];

                    mCameraThread.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                        long time = 0;

                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            if (time > 0) {
                                L.d("update time = " + (System.currentTimeMillis() - time));
                            }
                            time = System.currentTimeMillis();
                            camera.addCallbackBuffer(data);
                        }
                    }, bufferByte);

                    mCameraThread.startPreview();
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mCameraThread.startPreview();
                    mCameraThread.releaseCamera();
                    mCameraThread.destoryThread();
                }
            });
        } else {
            L.d("打开摄像头失败");
        }
    }
}
