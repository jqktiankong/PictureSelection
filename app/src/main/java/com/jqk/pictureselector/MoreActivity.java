package com.jqk.pictureselector;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jqk.pictureselectorlibrary.customview.ImageDisplayView;

/**
 * Created by Administrator on 2018/4/2 0002.
 */

public class MoreActivity extends AppCompatActivity {

    private ImageDisplayView imageDisplayView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        imageDisplayView = (ImageDisplayView) findViewById(R.id.imageDisplayView);

        imageDisplayView.setActivity(this);

        imageDisplayView.requestPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        imageDisplayView.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imageDisplayView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
