package com.jqk.pictureselectorlibrary.view.show;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jqk.pictureselectorlibrary.R;
import com.jqk.pictureselectorlibrary.adapter.MyViewPagerAdapter;
import com.jqk.pictureselectorlibrary.bean.Picture;
import com.jqk.pictureselectorlibrary.editPicture.EditPictureActivity;
import com.jqk.pictureselectorlibrary.message.CheckMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/1/29 0029.
 */

public class ShowImgActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private ImageView back;
    private TextView title;
    private ImageView check;
    private ViewPager myViewPager;
    private LinearLayout edit;

    private int pos;
    private ArrayList<Picture> pictures;
    private ArrayList<ShowImgFragment> fragments;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showimg);

        back = (ImageView) findViewById(R.id.back);
        title = (TextView) findViewById(R.id.title);
        check = (ImageView) findViewById(R.id.check);
        myViewPager = (ViewPager) findViewById(R.id.viewPager);
        edit = (LinearLayout) findViewById(R.id.editPicture);

        back.setOnClickListener(this);
        check.setOnClickListener(this);
        edit.setOnClickListener(this);

        pos = getIntent().getIntExtra("position", 0);
        pictures = getIntent().getParcelableArrayListExtra("pictures");

        initData();
    }

    public void initData() {
        title.setText((pos + 1) + "/" + pictures.size());

        setCheck(pos);

        fragments = new ArrayList<ShowImgFragment>();
        for (Picture picture : pictures) {
            ShowImgFragment showImgFragment = new ShowImgFragment();
            Bundle bundle = new Bundle();
            bundle.putString("path", picture.getUrl());
            showImgFragment.setArguments(bundle);
            fragments.add(showImgFragment);
        }

        MyViewPagerAdapter myViewPagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager(), fragments);
        myViewPager.setAdapter(myViewPagerAdapter);
        myViewPager.setOnPageChangeListener(this);
        myViewPager.setCurrentItem(pos, false);

    }

    public void setCheck(int position) {
        if (pictures.get(position).getCheck()) {
            check.setImageDrawable(getResources().getDrawable(R.drawable.icon_checkbox_on));
        } else {
            check.setImageDrawable(getResources().getDrawable(R.drawable.icon_checkbox));
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        pos = position;
        title.setText((pos + 1) + "/" + pictures.size());

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        setCheck(pos);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.back) {
            finish();
        } else if (id == R.id.check) {
            if (pictures.get(pos).getCheck()) {
                pictures.get(pos).setCheck(false);

                CheckMessage checkMessage = new CheckMessage();
                checkMessage.setCheck(false);
                checkMessage.setPosition(pos);

                EventBus.getDefault().post(checkMessage);
            } else {
                pictures.get(pos).setCheck(true);

                CheckMessage checkMessage = new CheckMessage();
                checkMessage.setCheck(true);
                checkMessage.setPosition(pos);

                EventBus.getDefault().post(checkMessage);
            }

            setCheck(pos);
        } else if (id == R.id.editPicture) {
            Intent intent = new Intent();
            intent.setClass(this, EditPictureActivity.class);
            intent.putExtra("imgPath", pictures.get(pos).getUrl());
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
