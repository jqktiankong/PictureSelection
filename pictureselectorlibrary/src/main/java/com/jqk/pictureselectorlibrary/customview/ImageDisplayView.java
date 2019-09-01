package com.jqk.pictureselectorlibrary.customview;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.jqk.pictureselectorlibrary.adapter.ImageDisplayAdapter;
import com.jqk.pictureselectorlibrary.bean.Display;
import com.jqk.pictureselectorlibrary.bean.Picture;
import com.jqk.pictureselectorlibrary.dialog.PromptDialog;
import com.jqk.pictureselectorlibrary.util.AppConstant;
import com.jqk.pictureselectorlibrary.util.DensityUtils;
import com.jqk.pictureselectorlibrary.view.batchSelector.BatchSelectorActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import static android.app.Activity.RESULT_OK;

public class ImageDisplayView extends RecyclerView implements ImageDisplayAdapter.AddClickListener, PromptDialog.PromptClickListener {

    private AppCompatActivity activity;
    private ArrayList<Display> displays;
    private DensityUtils densityUtil;
    private ImageDisplayAdapter imageDisplayAdapter;

    private File takePhotoFile;

    private int id = 1;

    public ImageDisplayView(Context context) {
        super(context);
        init();
    }

    public ImageDisplayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        densityUtil = new DensityUtils();
        displays = new ArrayList<Display>();

        setData();
    }

    public void setActivity(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case AppConstant.PICTURE_BATCH:
                if (data != null) {
                    ArrayList<Picture> pictures = data.getParcelableArrayListExtra("data");
                    for (int i = 0; i < displays.size(); i++) {
                        if (displays.get(i).getViewType() == AppConstant.VIEW_ADD) {
                            displays.remove(i);
                        }
                    }

                    for (Picture picture : pictures) {
                        Display display = new Display();
                        display.setViewType(AppConstant.VIEW_PICTURE);
                        display.setId(id++);
                        display.setPicture(picture);

                        displays.add(display);
                    }

                    setData();
                }
                break;
            case AppConstant.CODE_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        // 拍照成功
                        Log.d(AppConstant.TAG, "拍照成功");
                        for (int i = 0; i < displays.size(); i++) {
                            if (displays.get(i).getViewType() == AppConstant.VIEW_ADD) {
                                displays.remove(i);
                            }
                        }

                        Picture picture = new Picture();
                        picture.setCheck(false);
                        picture.setFile(takePhotoFile);
                        picture.setUrl(takePhotoFile.getPath());

                        Display display = new Display();
                        display.setId(id++);
                        display.setPicture(picture);
                        display.setViewType(AppConstant.VIEW_PICTURE);
                        displays.add(display);

                        setData();
                    }

                    break;
                }
        }
    }

    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    1);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(getContext(), "读取图片权限被拒绝", Toast.LENGTH_SHORT);
                }

                if (grantResults.length > 0
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(getContext(), "拍照权限被拒绝", Toast.LENGTH_SHORT);
                }
                return;
            }
        }
    }

    public ArrayList<Display> getDisplays() {

        displays.remove(displays.size() - 1);

        return displays;
    }

    public void setData() {
        if (displays.size() < 9) {
            Display add = new Display();
            add.setViewType(AppConstant.VIEW_ADD);
            add.setId(0);
            displays.add(add);
        }

        imageDisplayAdapter = new ImageDisplayAdapter(getContext(), displays, densityUtil.getScreenWidth());
        setAdapter(imageDisplayAdapter);
        setLayoutManager(new GridLayoutManager(getContext(), 4));
        imageDisplayAdapter.setOnAddClickListener(this);
    }

    @Override
    public void onAdd() {
        PromptDialog promptDialog = new PromptDialog();
        promptDialog.setPromptClickListener(this);
        promptDialog.show(activity.getSupportFragmentManager(), "PromptDialog");
    }

    @Override
    public void onDelete(int id, int position) {
        Log.d(AppConstant.TAG, "删除的position = " + position);
        Iterator<Display> it = displays.iterator();
        while (it.hasNext()) {
            Display x = it.next();
            if (x.getId() == id) {
                it.remove();
            }
        }

        imageDisplayAdapter.notifyDataSetChanged();

        if (displays.get(displays.size() - 1).getViewType() != AppConstant.VIEW_ADD) {
            Display add = new Display();
            add.setViewType(AppConstant.VIEW_ADD);
            add.setId(0);
            displays.add(add);
            imageDisplayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onShoot() {
        if (Build.VERSION.SDK_INT >= 24) {
            Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = get24MediaFileUri(AppConstant.TYPE_TAKE_PHOTO);
            takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            activity.startActivityForResult(takeIntent, AppConstant.CODE_TAKE_PHOTO);
        } else {
            Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = getMediaFileUri(AppConstant.TYPE_TAKE_PHOTO);
            takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            activity.startActivityForResult(takeIntent, AppConstant.CODE_TAKE_PHOTO);
        }
    }

    @Override
    public void onAlbum() {
        Intent intent = new Intent();
        intent.putExtra("number", displays.size() - 1);
        intent.setClass(getContext(), BatchSelectorActivity.class);
        activity.startActivityForResult(intent, AppConstant.PICTURE_BATCH);
    }

    public Uri getMediaFileUri(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + File.separator + "touxiang");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        //创建Media File
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == AppConstant.TYPE_TAKE_PHOTO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            takePhotoFile = mediaFile;
        } else {
            return null;
        }
        return Uri.fromFile(mediaFile);
    }

    public Uri get24MediaFileUri(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + File.separator + "touxiang");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        //创建Media File
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == AppConstant.TYPE_TAKE_PHOTO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            takePhotoFile = mediaFile;
        } else {
            return null;
        }
        return FileProvider.getUriForFile(getContext(), "com.jqk.pictureselectorlibrary.fileprovider", mediaFile);
    }
}