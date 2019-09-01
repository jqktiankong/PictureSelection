package com.jqk.pictureselectorlibrary.view.pictureSelector;

import android.content.Context;

import com.jqk.pictureselectorlibrary.bean.Folder;
import com.jqk.pictureselectorlibrary.util.ObservableFactroy;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PictureSelectorModel {
    private Observable<List<Folder>> observable;

    interface GetPictureCallback {
        void onFinish(List<Folder> folderList);
    }

    public void getPictures(Context context, final GetPictureCallback getPictureCallback) {
        observable = ObservableFactroy.getPictures(context);
        observable.subscribe(new Observer<List<Folder>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<Folder> folders) {
                getPictureCallback.onFinish(folders);
            }
        });
    }

    public void onDestroy() {
        if (observable != null) {
            observable.unsubscribeOn(Schedulers.io());
        }
    }
}
