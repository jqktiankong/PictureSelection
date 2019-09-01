package com.jqk.pictureselectorlibrary.bean;

import android.os.Parcel;
import android.os.Parcelable;


public class Video implements Parcelable {
    private String name;
    private String path;
    private String time;
    private int duration;

    public Video() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(path);
        dest.writeString(time);
        dest.writeInt(duration);
    }

    public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    private Video(Parcel in) {
        name = in.readString();
        path = in.readString();
        time = in.readString();
        duration = in.readInt();
    }

    @Override
    public String toString() {
        return "Video{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", time='" + time + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
