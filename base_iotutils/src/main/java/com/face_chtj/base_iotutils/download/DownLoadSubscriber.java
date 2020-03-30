package com.face_chtj.base_iotutils.download;

import com.face_chtj.base_iotutils.download.progress.ProgressCallBack;

import io.reactivex.observers.DisposableObserver;

public class DownLoadSubscriber<T> extends DisposableObserver<T> {
    private ProgressCallBack fileCallBack;

    public DownLoadSubscriber(ProgressCallBack fileCallBack) {
        this.fileCallBack = fileCallBack;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (fileCallBack != null)
            fileCallBack.onStart();
    }

    @Override
    public void onComplete() {
        if (fileCallBack != null){
            fileCallBack.onCompleted();
            fileCallBack.unsubscribe();
        }
    }

    @Override
    public void onError(Throwable e) {
        if (fileCallBack != null){
            fileCallBack.onError(e);
            fileCallBack.unsubscribe();
        }
    }

    @Override
    public void onNext(T t) {
        if (fileCallBack != null)
            fileCallBack.onSuccess(t);
    }
}