package com.jqk.pictureselector;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.icu.text.AlphabeticIndex;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.jqk.pictureselectorlibrary.view.camera.CameraActivity;
import com.jqk.pictureselectorlibrary.view.record.FFmpegRecordActivity;
import com.jqk.pictureselectorlibrary.view.record.RecordActivity;
import com.jqk.pictureselectorlibrary.view.recordvideo.RecordVideoActivity;
import com.jqk.pictureselectorlibrary.view.recordvideo.RecordVideoActivity2;

public class MainActivity extends AppCompatActivity {

    private Button single, more, recordVideo, camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        single = (Button) findViewById(R.id.single);
        more = (Button) findViewById(R.id.more);
        recordVideo = findViewById(R.id.recordVideo);
        camera = findViewById(R.id.camera);

        single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SingleActivity.class);
                startActivity(intent);
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MoreActivity.class);
                startActivity(intent);

//                Intent intent = new Intent();
//                intent.setClass(MainActivity.this, EditPictureActivity.class);
//                startActivity(intent);
            }
        });

        recordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, RecordVideoActivity.class);
                startActivity(intent);
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });
    }
}
