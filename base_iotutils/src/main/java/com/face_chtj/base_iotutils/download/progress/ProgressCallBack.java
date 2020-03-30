package com.face_chtj.base_iotutils.download.progress;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.bus.RxBus;
import com.face_chtj.base_iotutils.bus.RxSubscriptions;
import com.face_chtj.base_iotutils.download.DownLoadStateBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import okhttp3.ResponseBody;

public abstract class ProgressCallBack<T> {
    public static final String TAG="ProgressCallBack";
    private String destFileDir; // 本地文件存放路径
    private String destFileName; // 文件名
    private Disposable mSubscription;

    public ProgressCallBack(String destFileDir, String destFileName) {
        this.destFileDir = destFileDir;
        this.destFileName = destFileName;
        subscribeLoadProgress();
    }

    public abstract void onSuccess(T t);

    public abstract void progress(long progress, long total);

    public void onStart() {
    }

    public void onCompleted() {
    }

    public abstract void onError(Throwable e);

    public void saveFile(ResponseBody body) {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len;
        FileOutputStream fos = null;
        try {
            is = body.byteStream();
            File dir = new File(destFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, destFileName);
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                KLog.e("saveFile", e.getMessage());
            }
        }
    }

    /**
     * 订阅加载的进度条
     */
    public void subscribeLoadProgress() {
        mSubscription = RxBus.getDefault().toObservable(DownLoadStateBean.class)
                .observeOn(AndroidSchedulers.mainThread()) //回调到主线程更新UI
                .subscribe(new Consumer<DownLoadStateBean>() {
                    @Override
                    public void accept(final DownLoadStateBean progressLoadBean) throws Exception {
                        progress(progressLoadBean.getBytesLoaded(), progressLoadBean.getTotal());
                    }
                });
        //将订阅者加入管理站
        RxSubscriptions.add(mSubscription);
    }

    /**
     * 取消订阅，防止内存泄漏
     */
    public void unsubscribe() {
        RxSubscriptions.remove(mSubscription);
    }
}