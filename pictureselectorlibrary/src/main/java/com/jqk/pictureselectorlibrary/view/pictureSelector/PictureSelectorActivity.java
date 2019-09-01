package com.jqk.pictureselectorlibrary.view.pictureSelector;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jqk.pictureselectorlibrary.R;
import com.jqk.pictureselectorlibrary.adapter.FolderAdapter;
import com.jqk.pictureselectorlibrary.adapter.PictureAdapter;
import com.jqk.pictureselectorlibrary.base.BaseActivity;
import com.jqk.pictureselectorlibrary.bean.Folder;
import com.jqk.pictureselectorlibrary.bean.Picture;
import com.jqk.pictureselectorlibrary.util.AppConstant;
import com.jqk.pictureselectorlibrary.util.FileUtils;

import java.io.File;
import java.util.List;

/**
 * Created by  on 2017/9/5.
 */

public class PictureSelectorActivity extends BaseActivity implements View.OnClickListener,
        FolderAdapter.OnFolderSelectListener, PictureAdapter.PictureClickListener {

    private LinearLayout back;
    private TextView folderName;
    private LinearLayout pictureMenu;
    private FrameLayout folderView;
    private LinearLayout folderViewContent, folderViewBackground;
    private RecyclerView allPicture, pictureFolder;
    private FolderAdapter folderAdapter;
    private PictureAdapter pictureAdapter;
    private List<Picture> pictures;
    private List<Folder> folders;
    private int screenWidth;

    private Uri imageUri = null;

    private PictureSelectorModel pictureSelectorModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_selector);

        initView();
        init();

        back.setOnClickListener(this);
        pictureMenu.setOnClickListener(this);
        folderViewContent.setOnClickListener(this);

        pictureSelectorModel = new PictureSelectorModel();

        WindowManager wm = this.getWindowManager();

        screenWidth = wm.getDefaultDisplay().getWidth();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            pictureSelectorModel.getPictures(this, new PictureSelectorModel.GetPictureCallback() {
                @Override
                public void onFinish(List<Folder> folderList) {
                    setFolderAdapter(folderList);
                }
            });
        }
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

        setAllPictureAdapter(folders.get(position).getPictureList());

        if (folderViewContent.getVisibility() == View.VISIBLE) {
            showFolderView();
        }
    }

    @Override
    public void onPictureClick(File file) {
        startPhotoCrop(file);
    }

    public void setAllPictureAdapter(List<Picture> pictures) {
        pictureAdapter = new PictureAdapter(this, pictures, screenWidth);
        allPicture.setLayoutManager(new GridLayoutManager(this, 3));
        pictureAdapter.setOnPictureClickListener(this);
        allPicture.setAdapter(pictureAdapter);
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
                    pictures = folder.getPictureList();
                    folderName.setText(folder.getName());
                }
            }

            setAllPictureAdapter(pictures);
        }
    }

    // 跳转图片裁剪界面
    public void startPhotoCrop(File inputfile) {

        if (Build.VERSION.SDK_INT >= 24) {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(getImageContentUri(this, inputfile), "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 180);
            intent.putExtra("outputY", 180);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
            imageUri = FileUtils.getMediaFileUri(AppConstant.TYPE_TAKE_PHOTO);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//定义输出的File Uri
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true);
            startActivityForResult(intent, AppConstant.PICTURE_SELECT);
        } else {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(Uri.fromFile(inputfile), "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 180);
            intent.putExtra("outputY", 180);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", false);
            imageUri = FileUtils.getMediaFileUri(AppConstant.TYPE_TAKE_PHOTO);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//定义输出的File Uri，之后根据这个Uri去拿裁剪好的图片信息  ————代码B
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true);
            startActivityForResult(intent, AppConstant.PICTURE_SELECT);
        }
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
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
            if (folderView.getVisibility() == View.VISIBLE) {
                showFolderView();
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstant.PICTURE_SELECT) {

                Intent intent = new Intent();
                intent.putExtra("uri", imageUri);

                this.setResult(AppConstant.PICTURE_SELECT, intent);
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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pictureSelectorModel.getPictures(this, new PictureSelectorModel.GetPictureCallback() {
                        @Override
                        public void onFinish(List<Folder> folderList) {
                            setFolderAdapter(folderList);
                        }
                    });
                } else {
                    Toast.makeText(this, "读取图片权限被拒绝", Toast.LENGTH_SHORT);
                }
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pictureSelectorModel.onDestroy();

        folderAdapter.onDestroy();
    }
}
