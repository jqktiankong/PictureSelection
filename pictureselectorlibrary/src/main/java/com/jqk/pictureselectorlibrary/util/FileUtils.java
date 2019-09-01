package com.jqk.pictureselectorlibrary.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.jqk.pictureselectorlibrary.bean.Folder;
import com.jqk.pictureselectorlibrary.bean.Picture;
import com.jqk.pictureselectorlibrary.bean.Video;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by YASCN on 2017/9/5.
 */

public class FileUtils {
    // 文件夹排列顺序（倒序）
    public static String[] imageFileNames = {"QQ_Images", "zuiyou", "WeiXin", "Camera", "Screenshots"};

    public static List<Folder> getPictures(Context context) {
        // 获取图片
        HashMap<String, List<Picture>> folderMap = new HashMap<String, List<Picture>>();
        ArrayList<Folder> folders = new ArrayList<Folder>();
        ArrayList<Picture> allPicture = new ArrayList<Picture>();

        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        // 获取图片绝对路径
        String[] picProjection = {MediaStore.Images.Media.DATA};
        // 只搜索JPG,PNG格式的图片，图片大小大于20K
        int imgSize = 1024 * 20;
        //全部图片
        String picWhere = MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=?";
        //指定格式
        String[] picWhereArgs = {"image/jpeg", "image/png", "image/jpg"};

        int id = 0;

        ContentResolver mContentResolver = context.getContentResolver();
        Cursor mCursor = mContentResolver.query(mImageUri,
                picProjection,
                picWhere,
                picWhereArgs,
                MediaStore.Images.Media.DATE_MODIFIED + " desc");

        if (mCursor == null) {
            return null;
        }

        while (mCursor.moveToNext()) {
            String path = mCursor.getString(mCursor
                    .getColumnIndex(MediaStore.Images.Media.DATA));

            File file = new File(path);

            //获取该图片的父路径名
            String parentName = file.getParentFile().getName();

            //根据父路径名将图片放入到folderMap中
            if (!folderMap.containsKey(parentName)) {
                List<Picture> pictures = new ArrayList<Picture>();
                Picture picture = new Picture();
                picture.setId(id++);
                picture.setFolderName(parentName);
                picture.setUrl(path);
                picture.setFile(file);
                picture.setCheck(false);
                pictures.add(picture);
                folderMap.put(parentName, pictures);
            } else {
                Picture picture = new Picture();
                picture.setId(id++);
                picture.setFolderName(parentName);
                picture.setUrl(path);
                picture.setFile(file);
                picture.setCheck(false);
                folderMap.get(parentName).add(picture);
            }
        }

        mCursor.close();

        Iterator iter = folderMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            ArrayList<Picture> val = (ArrayList<Picture>) entry.getValue();

            // 插眼 不知道为什么要判断长度
            if (key.length() <= 15) {
                Folder folder = new Folder();
                folder.setName(key);
                folder.setType(AppConstant.FOLDER_TYPE_PICTURE);
                folder.setPictureList(val);
                folders.add(folder);
            }
        }

        // 按顺序排列图片文件夹
        for (int i = 0; i < imageFileNames.length; i++) {
            for (int j = 0; j < folders.size(); j++) {
                if (imageFileNames[i].equals(folders.get(j).getName())) {
                    folders.add(0, folders.get(j));
                    folders.remove(j + 1);
                }
            }
        }

        // 获取视频
        ArrayList<Video> allVideo = new ArrayList<Video>();

        String[] videoProjection = {
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE
        };
        //全部视频
        String videoWhere = MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=?";
        String[] videoWhereArgs = {"video/mp4", "video/3gp", "video/aiv", "video/rmvb", "video/vob", "video/flv",
                "video/mkv", "video/mov", "video/mpg"};

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoProjection, videoWhere,
                videoWhereArgs, MediaStore.Video.Media.DATE_MODIFIED + " desc");

        if (cursor == null) {
            return null;
        }

        while (cursor.moveToNext()) {

            long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)); // 大小
            if (size < 200 * 1024 * 1024) {//<200M

                String name = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));

                String path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Video.Media.DATA));

                int duration = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Video.Media.DURATION));

                Video video = new Video();
                video.setName(name);
                video.setPath(path);
                video.setDuration(duration);

                video.setTime(FormatUtils.duration2Time(duration));

//                L.d("name = " + name);
//                L.d("path = " + path);
//                L.d("duration = " + time);

                allVideo.add(video);
            }
        }

        Folder videoFolder = new Folder();
        videoFolder.setName("全部视频");
        videoFolder.setCheck(false);
        videoFolder.setType(AppConstant.FOLDER_TYPE_VIDEO);
        videoFolder.setVideoList(allVideo);

        // 添加全部图片
        for (Folder folder : folders) {
            allPicture.addAll(folder.getPictureList());
            if (folder.getName().equals("Camera")) {
                Log.d("123", folder.getPictureList().toString());
            }
        }

        Folder allPictrueFolder = new Folder();
        allPictrueFolder.setName("全部图片");
        allPictrueFolder.setCheck(true);
        allPictrueFolder.setType(AppConstant.FOLDER_TYPE_PICTURE);
        allPictrueFolder.setPictureList(allPicture);

        if (allVideo.size() != 0) {
            folders.add(0, videoFolder);
        }

        if (allPicture.size() != 0) {
            folders.add(0, allPictrueFolder);
        }

        return folders;
    }

    public static Uri getMediaFileUri(int type) {
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
        } else {
            return null;
        }
        return Uri.fromFile(mediaFile);
    }

    public static Uri get24MediaFileUri(int type, Context context) {
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
            Log.d(AppConstant.TAG, "path = " + mediaFile.getAbsolutePath());
            Toast.makeText(context, "path = " + mediaFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } else {
            return null;
        }
        return FileProvider.getUriForFile(context, "com.jqk.pictureselectorlibrary.fileprovider", mediaFile);
    }

    public static String getDestinationPath() {
        File folder = Environment.getExternalStorageDirectory();
        String mFinalPath = folder.getPath() + File.separator;
        L.d("Using default path " + mFinalPath);
        return mFinalPath;
    }
}
