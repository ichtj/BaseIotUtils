package com.face_chtj.base_iotutils.callback;

/**
 * Create on 2019/12/31
 * author chtj
 * desc ：实现此接口，可以监听Notification是否关闭的通知
 */
public interface IDismissListener {
    /**
     * 是否启用了(显示了Notification通知)
     * @param dismiss true | false
     */
    void dismiss(boolean dismiss);
}
