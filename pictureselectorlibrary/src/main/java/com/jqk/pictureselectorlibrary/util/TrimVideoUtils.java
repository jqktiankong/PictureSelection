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


import java.io.IOException;
import java.util.Formatter;

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

        String text = "ffmpeg -i /storage/emulated/0/123vidwocache/VID_20190920_174159.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts /storage/emulated/0/123vidwocache/VID_20190920_174159.ts";

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

        String text = "ffmpeg -i \"concat:/storage/emulated/0/123vidwocache/VID_20190920_174159.ts\" -c copy -bsf:a aac_adtstoasc -movflags +faststart /storage/emulated/0/123vidwocache/output.mp4";

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
