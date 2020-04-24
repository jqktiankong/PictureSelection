package com.jqk.pictureselectorlibrary.view.recordvideo;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.view.SurfaceHolder;

import androidx.annotation.RequiresApi;

import com.jqk.pictureselectorlibrary.util.L;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraManager {
    private static CameraManager mInstance;

    private Camera mCamera;
    private int cameraIndex;

    private int surfaceViewWidth;
    private int surfaceViewHeight;

    private int previewWidth;
    private int previewHeight;

    /**
     * 获取单例
     *
     * @return
     */
    public static CameraManager getInstance() {
        if (mInstance == null) {
            mInstance = new CameraManager();
        }
        return mInstance;
    }

    private CameraManager() {
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public int getSurfaceViewWidth() {
        return surfaceViewWidth;
    }

    public void setSurfaceViewWidth(int surfaceViewWidth) {
        this.surfaceViewWidth = surfaceViewWidth;
    }

    public int getSurfaceViewHeight() {
        return surfaceViewHeight;
    }

    public void setSurfaceViewHeight(int surfaceViewHeight) {
        this.surfaceViewHeight = surfaceViewHeight;
    }

    public void open(int index) {
        cameraIndex = index;
        mCamera = Camera.open(index);

        Camera.Parameters parameters = mCamera.getParameters();

        if (cameraIndex == Camera.CameraInfo.CAMERA_FACING_FRONT) {

        } else if (cameraIndex == Camera.CameraInfo.CAMERA_FACING_BACK) {
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
        }


        int[] maxFps = getFitFps(parameters.getSupportedPreviewFpsRange());
        Camera.Size size = getFitPreviewSize(parameters.getSupportedPreviewSizes());


        if (size != null) {
            previewWidth = size.width;
            previewHeight = size.height;
        }

        L.d("FitPreview width = " + previewWidth + "  FitPreview height = " + previewHeight);

        // 旋转之前的屏幕宽高
        parameters.setPreviewSize(previewWidth, previewHeight);
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setRecordingHint(true);
        if (maxFps[0] != 0) {
            parameters.setPreviewFpsRange(maxFps[0], maxFps[1]);
        } else {
            parameters.setPreviewFpsRange(30000, 30000);
        }

        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(parameters);

    }

    public void setPreviewSurface(SurfaceHolder holder) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPreviewCallbackWithBuffer(Camera.PreviewCallback callback, byte[] previewBuffer) {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(callback);
            mCamera.addCallbackBuffer(previewBuffer);
        }
    }

    public void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    /**
     * 释放相机
     */
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @RequiresApi
    public void setFocus(Rect rect) {
        L.d("当前版本 = " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            L.d("开始对焦");

            Camera.Parameters params = mCamera.getParameters();

            if (params.getMaxNumMeteringAreas() > 0) { // check that metering areas are supported
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();

                meteringAreas.add(new Camera.Area(rect, 1000)); // set weight to 60%
                params.setMeteringAreas(meteringAreas);

                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                params.setFocusAreas(meteringAreas);
            }
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    L.d("success = " + success);
                }
            });
            try {
                mCamera.setParameters(params);
            } catch (Exception e) {
                L.d("e = " + e.toString());
            }

        }
    }

    /**
     * 取最大的fps
     *
     * @param supportedFps
     * @return
     */
    public int[] getFitFps(List<int[]> supportedFps) {
        int[] result = new int[]{0, 0};
        for (int[] entry : supportedFps) {
            L.d("supportedFps entry[0] = " + entry[0] + ", entry[1] = " + entry[1]);
            if (entry[0] >= result[0] && entry[1] >= result[1]) {
                result[0] = entry[0];
                result[1] = entry[1];
            }
        }

        L.d("fitFps result[0] = " + result[0] + ", result[1] = " + result[1]);

        return result;
    }

    /**
     * 取跟960最接近的size
     *
     * @param sizes
     * @return
     */
    public Camera.Size getFitPreviewSize(List<Camera.Size> sizes) {
        Camera.Size result = null;

        List<Camera.Size> fitSizes = new ArrayList<>();

        if (surfaceViewWidth == 0) {
            return null;
        }
        for (Camera.Size size : sizes) {
            L.d("width = " + size.width + "   height = " + size.height);
            if (surfaceViewWidth / (float) surfaceViewHeight == size.height / (float) size.width) {
                fitSizes.add(size);

                if (size.width == 960) {
                    return size;
                }
            }
        }

        if (fitSizes.size() != 0) {
            int index = Math.abs(960 - fitSizes.get(0).width);
            result = fitSizes.get(0);

            for (Camera.Size size : fitSizes) {
                int abs = Math.abs(960 - size.width);
                if (abs <= index) {
                    index = abs;
                    result = size;
                }
            }
        }

        return result;
    }
}
