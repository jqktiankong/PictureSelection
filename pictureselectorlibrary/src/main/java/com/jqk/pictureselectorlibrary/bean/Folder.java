package com.jqk.pictureselectorlibrary.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  on 2017/9/5.
 */

public class Folder {
    private boolean check;
    private String name;
    private int type;
    private ArrayList<Picture> pictureList;
    private ArrayList<Video> videoList;

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ArrayList<Picture> getPictureList() {
        return pictureList;
    }

    public void setPictureList(ArrayList<Picture> pictureList) {
        this.pictureList = pictureList;
    }

    public ArrayList<Video> getVideoList() {
        return videoList;
    }

    public void setVideoList(ArrayList<Video> videoList) {
        this.videoList = videoList;
    }

    @Override
    public String toString() {
        return "Folder{" +
                "check=" + check +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", pictureList=" + pictureList +
                ", videoList=" + videoList +
                '}';
    }
}
