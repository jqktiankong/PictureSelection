package com.jqk.pictureselectorlibrary.view.recordvideo;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jqk.pictureselectorlibrary.R;

import java.io.IOException;

public class RecordVideoActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private Button start;
    private Button stop;
    private Button switchCamera;

    private Camera camera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordvideo);

        surfaceView = findViewById(R.id.surface_view);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        switchCamera = findViewById(R.id.switch_camera);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (camera == null) {
                    camera = Camera.open();
                    try {
                        camera.setPreviewDisplay(surfaceView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPictureSize(width, height);
                camera.setDisplayOrientation(90);
                camera.setParameters(parameters);
                camera.startPreview();
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
    }

    public void startCamera() {
        if (surfaceView != null) {

        }

    }
}
