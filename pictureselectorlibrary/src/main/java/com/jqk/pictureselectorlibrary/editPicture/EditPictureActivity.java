package com.jqk.pictureselectorlibrary.editPicture;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jqk.pictureselectorlibrary.R;

/**
 * Created by Administrator on 2018/6/5.
 */

public class EditPictureActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView img;
    private RelativeLayout menuView;
    private LinearLayout editView, iconView, inputView, mosaicView, screenshotsView;
    private TextView cancel, submit;

    private String imgPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editpicture);
        init();

//        imgPath = getIntent().getStringExtra("imgPath");
        imgPath = "/storage/emulated/0/DCIM/Camera/IMG_20180604_152400.jpg";

//        Glide.with(this).load(imgPath).into(img);
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideSystemUI();
    }

    public void init() {
        img = (ImageView) findViewById(R.id.img);
        menuView = (RelativeLayout) findViewById(R.id.menuView);
        editView = (LinearLayout) findViewById(R.id.editView);
        iconView = (LinearLayout) findViewById(R.id.iconView);
        inputView = (LinearLayout) findViewById(R.id.inputView);
        mosaicView = (LinearLayout) findViewById(R.id.mosaicView);
        screenshotsView = (LinearLayout) findViewById(R.id.screenshotsView);
        cancel = (TextView) findViewById(R.id.cancel);
        submit = (TextView) findViewById(R.id.submit);
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.editView) {

        }
    }
}
