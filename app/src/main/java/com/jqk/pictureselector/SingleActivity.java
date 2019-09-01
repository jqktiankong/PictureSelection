package com.jqk.pictureselector;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.jqk.pictureselectorlibrary.view.pictureSelector.PictureSelectorActivity;

import java.io.FileNotFoundException;

/**
 * Created by Administrator on 2018/4/2 0002.
 */

public class SingleActivity extends AppCompatActivity {

    private Button select;
    private ImageView picture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        select = (Button) findViewById(R.id.select);
        picture = (ImageView) findViewById(R.id.picture);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(SingleActivity.this, PictureSelectorActivity.class);
                startActivityForResult(intent, com.jqk.pictureselectorlibrary.util.AppConstant.PICTURE_SELECT);
            }
        });
    }

    private void getImageToView(Bitmap data, ImageView imageView) {
        Drawable drawable = new BitmapDrawable(this.getResources(), data);
        imageView.setImageDrawable(drawable);
    }

    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            // 先通过getContentResolver方法获得一个ContentResolver实例，
            // 调用openInputStream(Uri)方法获得uri关联的数据流stream
            // 把上一步获得的数据流解析成为bitmap
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data != null) { // 防止直接关闭界面没有数据
            getImageToView(decodeUriAsBitmap((Uri) data.getParcelableExtra("uri")), picture);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
