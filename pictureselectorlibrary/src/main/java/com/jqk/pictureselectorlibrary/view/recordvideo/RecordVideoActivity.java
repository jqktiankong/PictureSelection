package com.jqk.pictureselectorlibrary.view.recordvideo;

import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.jqk.pictureselectorlibrary.R;
import com.jqk.pictureselectorlibrary.util.L;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecordVideoActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private Button start;
    private Button stop;
    private Button switchCamera;
    private FocusView focusView;

    private Camera camera;
    private int fontCameraIndex = -1;
    private int backCameraIndex = -1;
    private int cameraCnt = 0;
    private int selectedCameraIndex = -1;

    private int cameraWidth, cameraHeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordvideo);

        surfaceView = findViewById(R.id.surface_view);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        switchCamera = findViewById(R.id.switch_camera);
        focusView = findViewById(R.id.focus_view);

        getCameraInfo();

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

                setFocus(focusRect);
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
    }

    public void startCamera(int cameraIndex) {
        if (surfaceView != null) {
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    if (camera == null) {
                        camera = Camera.open(cameraIndex);

                        try {
                            camera.setPreviewDisplay(surfaceView.getHolder());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    cameraWidth = width;
                    cameraHeight = height;

                    setCamera();
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    if (camera == null) {
                        return;
                    }

                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            });
        } else {
            L.d("打开摄像头失败");
        }
    }

    public void setCamera() {
        Camera.Parameters parameters = camera.getParameters();

        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        int[] a = new int[sizes.size()];
        int[] b = new int[sizes.size()];
        for (int i = 0; i < sizes.size(); i++) {
            int supportH = sizes.get(i).height;
            int supportW = sizes.get(i).width;
            L.d("supportW:" + supportW + "supportH:" + supportH);
        }

//        List<String> focusModes = parameters.getSupportedFocusModes();
//        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//        }

        parameters.setPreviewSize(2224, 1668);
        camera.setDisplayOrientation(90);
        camera.setParameters(parameters);
        camera.startPreview();
    }

    public void switchCamera() {
        if (cameraCnt <= 1) {
            L.d("只有一个摄像头");
        } else {
            if (selectedCameraIndex == fontCameraIndex) {
                if (camera != null) {
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                    selectedCameraIndex = backCameraIndex;
                    camera = Camera.open(backCameraIndex);
                    setCamera();

                    try {
                        camera.setPreviewDisplay(surfaceView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    camera.startPreview();
                } else {

                }


            } else if (selectedCameraIndex == backCameraIndex) {
                if (camera != null) {
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                    selectedCameraIndex = fontCameraIndex;
                    camera = Camera.open(fontCameraIndex);
                    setCamera();

                    try {
                        camera.setPreviewDisplay(surfaceView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    camera.startPreview();
                } else {

                }
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

    @RequiresApi
    public void setFocus(Rect rect) {

        L.d("当前版本 = " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            L.d("开始对焦");

            Camera.Parameters params = camera.getParameters();

            if (params.getMaxNumMeteringAreas() > 0) { // check that metering areas are supported
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();

                meteringAreas.add(new Camera.Area(rect, 1000)); // set weight to 60%
                params.setMeteringAreas(meteringAreas);

                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                params.setFocusAreas(meteringAreas);
            }
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    L.d("success = " + success);
                }
            });
            try {
                camera.setParameters(params);
            } catch (Exception e) {
                L.d("e = " + e.toString());
            }

        }
    }

}
