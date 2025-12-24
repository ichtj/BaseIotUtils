package com.face_chtj.base_iotutils.download;

public class DownloadErrorCode {

    /**
     * 网络不可达或 DNS 解析失败，例如用户未连接网络
     * 对应异常：UnknownHostException
     */
    public static final int UNKNOWN_HOST = 1001;

    /**
     * 网络连接或读取超时
     * 对应异常：SocketTimeoutException
     */
    public static final int SOCKET_TIMEOUT = 1002;

    /**
     * 服务器拒绝连接，通常是服务器未启动或防火墙拦截
     * 对应异常：ConnectException
     */
    public static final int CONNECT_EXCEPTION = 1003;

    /**
     * SSL 握手失败，通常是证书不受信任或 HTTPS 问题
     * 对应异常：SSLHandshakeException
     */
    public static final int SSL_HANDSHAKE = 1004;

    /**
     * 找不到文件或无法创建目标文件，可能是路径无效或权限不足
     * 对应异常：FileNotFoundException
     */
    public static final int FILE_NOT_FOUND = 1005;

    /**
     * 通用的输入输出错误，例如磁盘读写失败
     * 对应异常：IOException（非超时或连接拒绝）
     */
    public static final int IO_ERROR = 1006;

    /**
     * 存储空间不足（注意：需在业务层自行判断文件系统空间）
     * 没有特定异常类型，建议业务层检测
     */
    public static final int INSUFFICIENT_SPACE = 1007;

    /**
     * 下载任务被手动取消（如调用 call.cancel()）
     * 可结合 call.isCanceled() 检测
     */
    public static final int TASK_CANCELLED = 1008;

    /**
     * HTTP 请求响应码异常，例如 404、500 等
     * 需要从 Response.code() 中判断
     */
    public static final int HTTP_ERROR = 1009;

    /**
     * 未知或未分类的其他异常
     * 捕获 Exception 或 Throwable 时的兜底错误
     */
    public static final int UNKNOWN_ERROR = 1099;
}
