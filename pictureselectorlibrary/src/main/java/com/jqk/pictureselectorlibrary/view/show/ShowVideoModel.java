package com.jqk.pictureselectorlibrary.view.show;

import androidx.annotation.NonNull;

import com.jqk.pictureselectorlibrary.util.L;
import com.jqk.pictureselectorlibrary.util.ObservableFactroy;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ShowVideoModel {
    private Observable<String> observable;

    public interface ShowVideoOnCallback {
        void onFail(String fail);
        void onFinish(String path);
    }

    public void trimVideo(String path, int startTime, int duration, final ShowVideoOnCallback onCallback) {
        observable = ObservableFactroy.trimVideo(path, startTime, duration);
        observable.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onError(Throwable e) {
                L.d("e = " + e.toString());
                onCallback.onFail(e.toString());
            }

            @Override
            public void onNext(String path) {
                onCallback.onFinish(path);
            }
        });
    }

    public void onDestroy() {
        if (observable != null) {
            observable.unsubscribeOn(Schedulers.io());
        }
    }
}
