package com.jqk.pictureselectorlibrary.view.batchSelector;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jqk.pictureselectorlibrary.R;
import com.jqk.pictureselectorlibrary.adapter.BatchAdapter;
import com.jqk.pictureselectorlibrary.adapter.FolderAdapter;
import com.jqk.pictureselectorlibrary.adapter.VideoAdapter;
import com.jqk.pictureselectorlibrary.base.BaseActivity;
import com.jqk.pictureselectorlibrary.bean.Folder;
import com.jqk.pictureselectorlibrary.bean.Picture;
import com.jqk.pictureselectorlibrary.bean.Video;
import com.jqk.pictureselectorlibrary.message.CheckMessage;
import com.jqk.pictureselectorlibrary.util.L;
import com.jqk.pictureselectorlibrary.view.show.ShowImgActivity;
import com.jqk.pictureselectorlibrary.util.AppConstant;
import com.jqk.pictureselectorlibrary.view.show.ShowVideoActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by  on 2017/9/5.
 */

public class BatchSelectorActivity extends BaseActivity implements View.OnClickListener,
        FolderAdapter.OnFolderSelectListener, BatchAdapter.BatchClickListener, VideoAdapter.VideoClickListener {

    private LinearLayout back;
    private TextView folderName;
    private LinearLayout pictureMenu;
    private FrameLayout folderView;
    private LinearLayout folderViewContent, folderViewBackground;
    private RecyclerView allPicture, pictureFolder;
    private Button define;
    private FolderAdapter folderAdapter;
    private BatchAdapter batchAdapter;
    private VideoAdapter videoAdapter;
    private List<Folder> folders;
    private int screenWidth;
    private int pictureNum = 0;

    private ArrayList<Picture> checkedList;

    private int number = 9;

    private BathSelectorModel bathSelectorModel;

    private GridLayoutManager mGridLayoutManager;
    private ArrayList<Picture> pictures;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_selector);

        initView();
        init();

        pictureNum = getIntent().getIntExtra("number", 0);

        back.setOnClickListener(this);
        pictureMenu.setOnClickListener(this);
        folderViewContent.setOnClickListener(this);
        define.setOnClickListener(this);

        bathSelectorModel = new BathSelectorModel();
        bathSelectorModel.getPictures(this, new BathSelectorModel.GetPictureCallback() {
            @Override
            public void onFinish(List<Folder> folderList) {
                setFolderAdapter(folderList);
            }
        });

        WindowManager wm = this.getWindowManager();

        screenWidth = wm.getDefaultDisplay().getWidth();

        checkedList = new ArrayList<Picture>();
        setButtonText();

        //注册事件
        EventBus.getDefault().register(this);
    }

    public void init() {
        back = (LinearLayout) findViewById(R.id.back);
        pictureMenu = (LinearLayout) findViewById(R.id.pictureMenu);
        folderView = (FrameLayout) findViewById(R.id.folderView);
        folderViewContent = (LinearLayout) findViewById(R.id.folderViewContent);
        folderViewBackground = (LinearLayout) findViewById(R.id.folderViewBackground);
        allPicture = (RecyclerView) findViewById(R.id.allPicture);
        pictureFolder = (RecyclerView) findViewById(R.id.pictureFolder);
        folderName = (TextView) findViewById(R.id.folderName);
        define = (Button) findViewById(R.id.define);
    }

    @Override
    public void onFolderSelect(int position) {
        for (int i = 0; i < folders.size(); i++) {
            if (i == position) {
                folders.get(i).setCheck(true);
                folderName.setText(folders.get(i).getName());
            } else {
                folders.get(i).setCheck(false);
            }
        }

        folderAdapter.notifyDataSetChanged();

        if (folders.get(position).getType() == AppConstant.FOLDER_TYPE_PICTURE) {
            setAllPictureAdapter(folders.get(position).getPictureList());
        } else if (folders.get(position).getType() == AppConstant.FOLDER_TYPE_VIDEO) {
            setAllVideoAdapter(folders.get(position).getVideoList());
        }

        showFolderView();
    }

    @Override
    public void onBatchClick(Picture picture) {
        setCheck(picture);
    }

    public void setCheck(Picture picture) {

        Log.d(AppConstant.TAG, "picture = " + picture.toString());

        if (picture.getCheck()) {
            if ((checkedList.size() + pictureNum) < number) {
                checkedList.add(picture);
            }
        } else {
            Iterator<Picture> it = checkedList.iterator();
            while (it.hasNext()) {
                Picture x = it.next();
                if (x.getUrl().equals(picture.getUrl())) {
                    it.remove();
                }
            }
        }

        setButtonText();
    }

    @Override
    public void onVideoClick(Video video) {
        L.d("视频点击 = " + video.getPath());

        if (video.getDuration() > 1000 * 15) {
            Intent intent = new Intent();
            intent.setClass(this, ShowVideoActivity.class);
            intent.putExtra("videoPath", video.getPath());
            startActivity(intent);
        } else {
            Toast.makeText(this, "时长小于15秒", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onShowClick(String imgPath, int position) {
        // 图片预览
        Intent intent = new Intent();
        intent.setClass(this, ShowImgActivity.class);
        intent.putParcelableArrayListExtra("pictures", pictures);
        intent.putExtra("position", position);

        startActivity(intent);
    }

    public void setButtonText() {
        if ((checkedList.size() + pictureNum) == 0) {
            define.setText("确定");
            define.setBackground(getResources().getDrawable(R.drawable.bg_button_zero));
        } else {
            define.setText("(" + (checkedList.size() + pictureNum) + "/" + number + ")" + "确定");
            define.setBackground(getResources().getDrawable(R.drawable.bg_button));
        }
    }

    public void setAllPictureAdapter(final ArrayList<Picture> pictures) {
        batchAdapter = new BatchAdapter(this, pictures, screenWidth);
        mGridLayoutManager = new GridLayoutManager(this, 3);
        allPicture.setLayoutManager(mGridLayoutManager);
        allPicture.setHasFixedSize(true);
        allPicture.setAdapter(batchAdapter);
        batchAdapter.setOnBatchClickListener(this);

        define.setVisibility(View.VISIBLE);

        this.pictures = pictures;
    }

    public void setAllVideoAdapter(final ArrayList<Video> videos) {
        videoAdapter = new VideoAdapter(this, videos, screenWidth);
        mGridLayoutManager = new GridLayoutManager(this, 3);
        allPicture.setLayoutManager(mGridLayoutManager);
        allPicture.setHasFixedSize(true);
        allPicture.setAdapter(videoAdapter);
        videoAdapter.setOnPictureClickListener(this);

        define.setVisibility(View.GONE);

    }

    public void setFolderAdapter(List<Folder> folders) {
        if (folders.size() == 0) {
            showView(AppConstant.VIEW_EMPTY);
        } else {
            showView(AppConstant.VIEW_CONTENT);
            this.folders = folders;
            folderAdapter = new FolderAdapter(this, this.folders);
            pictureFolder.setLayoutManager(new LinearLayoutManager(this));
            pictureFolder.setAdapter(folderAdapter);
            folderAdapter.setOnFolderSelectListener(this);

            for (Folder folder : folders) {
                if (folder.isCheck()) {
                    setAllPictureAdapter(folder.getPictureList());
                    folderName.setText(folder.getName());
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoonEvent(CheckMessage checkMessage) {
        Log.d(AppConstant.TAG, "点击了");
        pictures.get(checkMessage.getPosition()).setCheck(checkMessage.isCheck());
        setAllPictureAdapter(pictures);
        setCheck(pictures.get(checkMessage.getPosition()));
    }

    public void showFolderView() {
        if (folderView.getVisibility() == View.VISIBLE) {
            // 隐藏动画，背景变亮
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(folderViewBackground, "alpha", 1f, 0f);
            ObjectAnimator outAnim = ObjectAnimator.ofFloat(folderViewContent, "y", 0, folderViewContent.getHeight());
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnim, outAnim);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    pictureMenu.setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    pictureMenu.setClickable(true);
                    folderView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animatorSet.setDuration(AppConstant.DURATIONFORFOLDER);
            animatorSet.start();

        } else if (folderView.getVisibility() == View.INVISIBLE) {
            // 展开动画，背景变暗
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(folderViewBackground, "alpha", 0f, 1f);
            ObjectAnimator inAnim = ObjectAnimator.ofFloat(folderViewContent, "y", folderView.getHeight(), 0);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnim, inAnim);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    pictureMenu.setClickable(false);
                    folderView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    pictureMenu.setClickable(true);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animatorSet.setDuration(AppConstant.DURATIONFORFOLDER);
            animatorSet.start();
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back) {
            if (folderView.getVisibility() == View.VISIBLE) {
                showFolderView();
            } else {
                finish();
            }
        } else if (i == R.id.pictureMenu) {
            showFolderView();
        } else if (i == R.id.folderViewContent) {
            showFolderView();
        } else if (i == R.id.define) {
            if (checkedList.size() == 0 || folderView.getVisibility() == View.VISIBLE) {
                return;
            }

            Intent intent = new Intent();
            intent.putParcelableArrayListExtra("data", checkedList);
            setResult(AppConstant.PICTURE_BATCH, intent);
            finish();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode != 0) {
                this.setResult(AppConstant.PICTURE_BATCH, data);
                finish();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (folderView.getVisibility() == View.VISIBLE) {
                showFolderView();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bathSelectorModel.onDestroy();
        //取消注册事件
        EventBus.getDefault().unregister(this);
        if (folderAdapter != null) {
            folderAdapter.onDestroy();
        }

        if (videoAdapter != null) {
            videoAdapter.onDestroy();
        }
    }
}
