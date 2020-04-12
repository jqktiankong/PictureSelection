/*
 * MIT License
 *
 * Copyright (c) 2016 Knowledge, education for life.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.jqk.pictureselectorlibrary.util;


import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;

public class TrimVideoUtils {

    public interface OnCallBack {
        void onSuccess();

        void onFail();

        void onProgress(float progress);
    }

    public static void startTrim(String path, String trimPath, int startTime, int duration, OnCallBack onCallBack) throws IOException {

        String text = "ffmpeg -ss " + startTime + " -t " + duration + " -i " + path + " -vcodec copy -acodec copy " + trimPath;

        String[] commands = text.split(" ");

        RxFFmpegInvoke.getInstance().runCommandRxJava(commands).subscribe(new RxFFmpegSubscriber() {
            @Override
            public void onFinish() {
                onCallBack.onSuccess();
            }

            @Override
            public void onProgress(int progress, long progressTime) {
                onCallBack.onProgress(progress);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(String message) {
                L.d("解析错误 = " + message);
                onCallBack.onFail();
            }
        });
    }

    public static void video2pic(String path, String picPath, int picWidth, int picHeight, OnCallBack onCallBack) {
        String text = "ffmpeg -i " + path + " -s " + picWidth + "x" + picHeight + " -r 1 -q:v 2 -f image2 " + picPath;

        String[] commands = text.split(" ");

        RxFFmpegInvoke.getInstance().runCommandRxJava(commands).subscribe(new RxFFmpegSubscriber() {
            @Override
            public void onFinish() {
                onCallBack.onSuccess();
            }

            @Override
            public void onProgress(int progress, long progressTime) {
                onCallBack.onProgress(progress);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(String message) {
                L.d("解析错误 = " + message);
                onCallBack.onFail();
            }
        });
    }

    public static void mergeVideos(String filelistPath, String mergePaht, OnCallBack onCallBack) {
//        ffmpeg -f concat -i Cam01.txt -c copy Cam01.mp4
        String text = "ffmpeg -f concat -safe 0 -i " + filelistPath + " -c copy " + mergePaht;

        String[] commands = text.split(" ");

        RxFFmpegInvoke.getInstance().runCommandRxJava(commands).subscribe(new RxFFmpegSubscriber() {
            @Override
            public void onFinish() {
                onCallBack.onSuccess();
            }

            @Override
            public void onProgress(int progress, long progressTime) {
                onCallBack.onProgress(progress);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(String message) {
                L.d("解析错误 = " + message);
                onCallBack.onFail();
            }
        });
    }

    public static void vflipVideo(OnCallBack onCallBack) {
//        ffmpeg -i fan.jpg -vf vflip -y vflip.png
        String text = "ffmpeg -i /storage/emulated/0/123vidwocache/VID_20190920_161227.mp4 -vf hflip -y /storage/emulated/0/123vidwocache/vflip.mp4";

        String[] commands = text.split(" ");

        RxFFmpegInvoke.getInstance().runCommandRxJava(commands).subscribe(new RxFFmpegSubscriber() {
            @Override
            public void onFinish() {
                onCallBack.onSuccess();
            }

            @Override
            public void onProgress(int progress, long progressTime) {
                onCallBack.onProgress(progress);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(String message) {
                L.d("解析错误 = " + message);
                onCallBack.onFail();
            }
        });
    }

    public static void test1(OnCallBack onCallBack) {
//        ffmpeg -i input1.flv -c copy -bsf:v h264_mp4toannexb -f mpegts input1.ts

        String text = "ffmpeg -noautorotate -i /storage/emulated/0/123vidwocache/VID_20190921_144251.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts /storage/emulated/0/123vidwocache/2.ts";

        String[] commands = text.split(" ");

        RxFFmpegInvoke.getInstance().runCommandRxJava(commands).subscribe(new RxFFmpegSubscriber() {
            @Override
            public void onFinish() {
                onCallBack.onSuccess();
            }

            @Override
            public void onProgress(int progress, long progressTime) {
                onCallBack.onProgress(progress);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(String message) {
                L.d("解析错误 = " + message);
                onCallBack.onFail();
            }
        });
    }

    public static void test2(OnCallBack onCallBack) {
//        ffmpeg -i "concat:input1.ts|input2.ts|input3.ts" -c copy -bsf:a aac_adtstoasc -movflags +faststart output.mp4

        String text = "ffmpeg -i \"concat:/storage/emulated/0/123vidwocache/1.ts|/storage/emulated/0/123vidwocache/2.ts\" -acodec copy -vcodec copy -absf aac_adtstoasc /storage/emulated/0/123vidwocache/output.mp4";

        String[] commands = text.split(" ");

        RxFFmpegInvoke.getInstance().runCommandRxJava(commands).subscribe(new RxFFmpegSubscriber() {
            @Override
            public void onFinish() {
                onCallBack.onSuccess();
            }

            @Override
            public void onProgress(int progress, long progressTime) {
                onCallBack.onProgress(progress);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(String message) {
                L.d("解析错误 = " + message);
                onCallBack.onFail();
            }
        });
    }

    public static void test3(OnCallBack onCallBack) {
//        ffmpeg -i /storage/emulated/0/123vidwocache/VID_20190921_163637.mp4 -c:v libx264 -crf 19 -preset slow -c:a aac -b:a 192k -ac 2 /storage/emulated/0/123vidwocache/output.mp4
//        ffmpeg -y -i /storage/emulated/0/123vidwocache/VID_20190921_163630.mp4 -b 2097k -r 30 -vcodec libx264 -preset superfast /storage/emulated/0/123vidwocache/output1.mp4.mp4
        String text = "ffmpeg -i /storage/emulated/0/123vidwocache/VID_20190921_182148.mp4 -c:v libx264 -crf 19 -preset superfast -c:a aac -b:a 192k -ac 2 /storage/emulated/0/123vidwocache/output1.mp4";

        String[] commands = text.split(" ");

        RxFFmpegInvoke.getInstance().runCommandRxJava(commands).subscribe(new RxFFmpegSubscriber() {
            @Override
            public void onFinish() {
                onCallBack.onSuccess();
            }

            @Override
            public void onProgress(int progress, long progressTime) {
                onCallBack.onProgress(progress);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(String message) {
                L.d("解析错误 = " + message);
                onCallBack.onFail();
            }
        });
    }

    public static void mergeMP4() {
        List<String> fileList = new ArrayList<String>();
        List<com.googlecode.mp4parser.authoring.Movie> moviesList = new LinkedList<>();
//添加需要合并的文件
        fileList.add("/storage/emulated/0/123vidwocache/VID_20190921_130227.mp4");
        fileList.add("/storage/emulated/0/123vidwocache/VID_20190921_130233.mp4");

        try {
            for (String file : fileList) {
                moviesList.add(MovieCreator.build(file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();
        for (com.googlecode.mp4parser.authoring.Movie m : moviesList) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals("soun")) {
                    audioTracks.add(t);
                }
                if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }
            }
        }

        Movie result = new Movie();

        try {
            if (audioTracks.size() > 0) {
                result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
            }
            if (videoTracks.size() > 0) {
                result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Container out = new DefaultMp4Builder().build(result);

        try {
//输出合并后的文件
            FileChannel fc = new RandomAccessFile("/storage/emulated/0/123vidwocache/123456.mp4", "rw").getChannel();
            out.writeContainer(fc);
            fc.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        moviesList.clear();
        fileList.clear();
        L.d("合成完成");
    }

    public static String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        Formatter mFormatter = new Formatter();
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
}
