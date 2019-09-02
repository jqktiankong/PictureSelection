package com.jqk.pictureselectorlibrary.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.jqk.pictureselectorlibrary.bean.Folder;

import org.reactivestreams.Subscriber;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class ObservableFactroy {
    public static Observable<List<Folder>> getPictures(final Context context) {

        Observable<List<Folder>> observable = Observable.create(new ObservableOnSubscribe<List<Folder>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Folder>> emitter) throws Exception {
                emitter.onNext(FileUtils.getPictures(context));
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        return observable;
    }

    public static Observable<String> trimVideo(String path, String trimPath, int startTime, int duration) {
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                TrimVideoUtils.startTrim(path, trimPath, startTime, duration, new TrimVideoUtils.OnCallBack() {
                    @Override
                    public void onSuccess() {
                        emitter.onComplete();
                    }

                    @Override
                    public void onFail() {
                        emitter.onError(new Throwable("解析失败"));
                    }

                    @Override
                    public void onProgress(float progress) {
                        emitter.onNext(progress + "");
                    }
                });
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        return observable;
    }

    public static Observable<String> video2pic(String path, String picPath, int picWidth, int picHeight) {
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                TrimVideoUtils.video2pic(path, picPath, picWidth, picHeight, new TrimVideoUtils.OnCallBack() {
                    @Override
                    public void onSuccess() {
                        emitter.onComplete();
                    }

                    @Override
                    public void onFail() {
                        emitter.onError(new Throwable("解析失败"));
                    }

                    @Override
                    public void onProgress(float progress) {
                        emitter.onNext(progress + "");
                    }
                });
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        return observable;
    }
}
