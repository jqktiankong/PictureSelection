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

import androidx.annotation.NonNull;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceViaHeapImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;

public class TrimVideoUtils {

    public interface OnCallBack {
        void onSuccess();

        void onFail();

        void onProgress(float progress);
    }

    public static void startTrim(String path, String trimPath, int startTime, int duration, OnCallBack onCallBack) throws IOException {

//        EpVideo epVideo = new EpVideo(path);
//
////一个参数为剪辑的起始时间，第二个参数为持续时间,单位：秒
//        epVideo.clip(startTime, duration);
//
//        //输出选项，参数为输出文件路径(目前仅支持mp4格式输出)
//        EpEditor.OutputOption outputOption = new EpEditor.OutputOption(trimPath);
//        outputOption.frameRate = 30;//输出视频帧率,默认30
//        outputOption.bitRate = 10;//输出视频码率,默认10
//        EpEditor.exec(epVideo, outputOption, new OnEditorListener() {
//            @Override
//            public void onSuccess() {
//                L.d("处理完成");
//                onCallBack.onSuccess();
//            }
//
//            @Override
//            public void onFailure() {
//                L.d("处理失败");
//                onCallBack.onFail();
//            }
//
//            @Override
//            public void onProgress(float progress) {
//                //这里获取处理进度
//                L.d("progress = " + progress);
//                onCallBack.onProgress(progress);
//            }
//        });

        String text = "ffmpeg -ss " + startTime + " -t " + duration + " -i "+ path +" -vcodec copy -acodec copy " + trimPath;

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
//        EpEditor.video2pic(path, picPath, picWidth, picHeight, 1, new OnEditorListener() {
//            @Override
//            public void onSuccess() {
//                L.d("处理完成");
//                onCallBack.onSuccess();
//            }
//
//            @Override
//            public void onFailure() {
//                L.d("处理失败");
//                onCallBack.onFail();
//            }
//
//            @Override
//            public void onProgress(float progress) {
//                L.d("progress = " + progress);
//                onCallBack.onProgress(progress);
//            }
//        });

        String text = "ffmpeg -i path -r 1 -q:v 2 -f image2 picPath";

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
