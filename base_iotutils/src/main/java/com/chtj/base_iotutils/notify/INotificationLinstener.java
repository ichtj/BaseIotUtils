package com.chtj.base_iotutils.notify;

/**
 * Create on 2019/12/31
 * author chtj
 * desc ：实现此接口，可以监听Notification是否关闭的通知
 */
public interface INotificationLinstener {
    /**
     * 是否启用了(显示了Notification通知)
     * @param isEnable true | false
     */
    void enableStatus(boolean isEnable);
}
