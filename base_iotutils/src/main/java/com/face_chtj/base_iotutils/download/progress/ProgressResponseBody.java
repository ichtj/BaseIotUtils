package com.face_chtj.base_iotutils.download.progress;

import com.face_chtj.base_iotutils.bus.RxBus;
import com.face_chtj.base_iotutils.download.DownLoadStateBean;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
public class ProgressResponseBody extends ResponseBody {
    private static final String TAG="ProgressResponseBody";
    private ResponseBody responseBody;

    private BufferedSource bufferedSource;
    private String tag;

    public ProgressResponseBody(ResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    public ProgressResponseBody(ResponseBody responseBody, String tag) {
        this.responseBody = responseBody;
        this.tag = tag;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long bytesReaded = 0;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                //获得当前的下载进度
                long bytesRead = super.read(sink, byteCount);
                //整合之前下载的进度与现在的进度
                bytesReaded += bytesRead == -1 ? 0 : bytesRead;
                //使用RxBus的方式，实时发送当前已读取(上传/下载)的字节数据
                RxBus.getDefault().post(new DownLoadStateBean(contentLength(), bytesReaded, tag));
                return bytesRead;
            }
        };
    }
}
