package com.jqk.pictureselectorlibrary.view.recordvideo;

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
    private static Camera camera;

    private static int cameraIndex;

    public static Camera getInstance() {
        return camera;
    }

    public static void open(int index) {
        cameraIndex = index;
        camera = Camera.open(index);
    }

    public static void setPreviewDisplay(SurfaceHolder surfaceHolder) throws IOException {
        if (camera != null) {
            camera.setPreviewDisplay(surfaceHolder);
        }
    }

    public static void setParameters(Camera.Size size) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();

            if (cameraIndex == Camera.CameraInfo.CAMERA_FACING_FRONT) {

            } else if (cameraIndex == Camera.CameraInfo.CAMERA_FACING_BACK) {
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }
            }

            parameters.setPreviewSize(size.height, size.width);

            camera.setDisplayOrientation(90);
            camera.setParameters(parameters);
        }
    }

    public static void setParameters(Camera.Parameters parameters) {
        if (camera != null) {
            camera.setParameters(parameters);
        }
    }

    public static Camera.Parameters getParameters() {
        return camera.getParameters();
    }

    public static void startPreview() {
        if (camera != null) {
            camera.startPreview();
        }
    }

    public static void stopAndRelease() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public static Camera.Size getOptimalSize(int w, int h) {
        Camera.Parameters parameters = CameraManager.getInstance().getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : sizes) {
            L.d("width = " + size.width + "   height = " + size.height);
        }

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    @RequiresApi
    public static void setFocus(Rect rect) {

        L.d("当前版本 = " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            L.d("开始对焦");

            Camera.Parameters params = CameraManager.getParameters();

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
                CameraManager.setParameters(params);
            } catch (Exception e) {
                L.d("e = " + e.toString());
            }

        }
    }
}
