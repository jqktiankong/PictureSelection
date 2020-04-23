package com.jqk.pictureselectorlibrary.view.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;

public class CameraHandlerThread extends HandlerThread {
    private static final String TAG = "CameraHandlerThread";

    private final Handler mHandler;

    public CameraHandlerThread() {
        super(TAG);
        start();
        mHandler = new Handler(getLooper());
    }

    public CameraHandlerThread(String name) {
        super(name);
        start();
        mHandler = new Handler(getLooper());
    }

    /**
     * 销毁线程
     */
    public void destoryThread() {
        releaseCamera();
        mHandler.removeCallbacksAndMessages(null);
        quitSafely();
    }

    /**
     * 检查handler是否可用
     */
    private void checkHandleAvailable() {
        if (mHandler == null) {
            throw new NullPointerException("Handler is not available!");
        }
    }

    /**
     * 等待操作完成
     */
    private void waitUntilReady() {
        try {
            wait();
        } catch (InterruptedException e) {
            Log.w(TAG, "wait was interrupted");
        }
    }

    synchronized public void openCamera(int index) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CameraManager.getInstance().open(index);
                notifyCameraOpened();
            }
        });
        waitUntilReady();
    }

    synchronized private void notifyCameraOpened() {
        notify();
    }

    public void setPreviewSurface(final SurfaceHolder holder) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CameraManager.getInstance().setPreviewSurface(holder);
            }
        });
    }

    /**
     * 释放相机
     */
    synchronized public void releaseCamera() {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CameraManager.getInstance().releaseCamera();
                notifyCameraReleased();
            }
        });
        waitUntilReady();
    }

    /**
     * 通知销毁成功
     */
    synchronized private void notifyCameraReleased() {
        notify();
    }

    /**
     * 设置预览回调
     *
     * @param callback 回调
     * @param buffer   缓冲
     */
    public void setPreviewCallbackWithBuffer(final Camera.PreviewCallback callback,
                                             final byte[] buffer) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CameraManager.getInstance().setPreviewCallbackWithBuffer(callback, buffer);
            }
        });
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CameraManager.getInstance().startPreview();
            }
        });
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CameraManager.getInstance().stopPreview();
            }
        });
    }
}
