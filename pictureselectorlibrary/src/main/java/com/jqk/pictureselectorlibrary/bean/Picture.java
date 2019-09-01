package com.jqk.pictureselectorlibrary.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Created by  on 2017/9/5.
 */

public class Picture implements Parcelable {
    private int id;
    private String folderName;
    private File file;
    private Boolean check;
    private String url;
    public Picture() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public String getUrl() {//将你的图片地址字段返回
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(folderName);
        dest.writeSerializable(file);
        dest.writeInt(check ? 1 : 0);
        dest.writeString(this.url);
    }

    public static final Parcelable.Creator<Picture> CREATOR = new Parcelable.Creator<Picture>() {
        public Picture createFromParcel(Parcel in) {
            return new Picture(in);
        }

        public Picture[] newArray(int size) {
            return new Picture[size];
        }
    };

    private Picture(Parcel in) {
        id = in.readInt();
        folderName = in.readString();
        file = (File) in.readSerializable();
        check = in.readInt() == 1;
        this.url = in.readString();
    }

    @Override
    public String toString() {
        return "Picture{" +
                "id=" + id +
                ", folderName='" + folderName + '\'' +
                ", file=" + file +
                ", check=" + check +
                ", url='" + url + '\'' +
                '}';
    }
}
