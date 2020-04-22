package com.jqk.pictureselectorlibrary.view.camera;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.jqk.pictureselectorlibrary.util.L;

import java.io.IOException;
import java.util.List;

public class CameraManager {
    private static CameraManager mInstance;

    private Camera mCamera;
    private int cameraIndex;

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

        List<int[]> supportedFps = parameters.getSupportedPreviewFpsRange();
        for (int[] entry : supportedFps) {
            L.d("supportedFps entry[0] = " + entry[0] + ", entry[1] = " + entry[1]);
        }

        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : sizes) {
            L.d("width = " + size.width + "   height = " + size.height);
        }

        parameters.setPreviewSize(960, 540);
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setRecordingHint(true);
        parameters.setPreviewFpsRange(30000, 30000);
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
}
