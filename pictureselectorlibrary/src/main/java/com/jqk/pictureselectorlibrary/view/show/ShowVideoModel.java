package com.jqk.pictureselectorlibrary.view.show;

import androidx.annotation.NonNull;

import com.jqk.pictureselectorlibrary.util.L;
import com.jqk.pictureselectorlibrary.util.ObservableFactroy;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;

public class ShowVideoModel {
    private CompositeDisposable compositeDisposable;
    private Observable<String> observable;
    private Observable<String> observable2;

    public ShowVideoModel() {
        compositeDisposable = new CompositeDisposable();
    }

    public interface ShowVideoOnCallback {
        void onFail(String fail);

        void onFinish(String path);

        void onProgress(String progress);
    }

    public void trimVideo(String path, String trimPath, int startTime, int duration, final ShowVideoOnCallback onCallback) {
        observable = ObservableFactroy.trimVideo(path, trimPath, startTime, duration);
        observable.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onComplete() {
                onCallback.onFinish(path);
            }

            @Override
            public void onError(Throwable e) {
                L.d("e = " + e.toString());
                onCallback.onFail(e.toString());
            }

            @Override
            public void onNext(String progress) {
                onCallback.onProgress(progress);
            }
        });
    }

    public void video2pic(String path, String picPath, int picWidth, int picHeight, final ShowVideoOnCallback onCallback) {
        observable2 = ObservableFactroy.video2pic(path, picPath, picWidth, picHeight);
        observable2.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onNext(String s) {
                onCallback.onProgress(s);
            }

            @Override
            public void onError(Throwable e) {
                L.d("e = " + e.toString());
                onCallback.onFail(e.toString());
            }

            @Override
            public void onComplete() {
                onCallback.onFinish(path);
            }
        });
    }

    public void onDestroy() {

        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }

        if (observable != null) {
            observable.unsubscribeOn(Schedulers.io());
        }

        if (observable2 != null) {
            observable2.unsubscribeOn(Schedulers.io());
        }
    }
}
