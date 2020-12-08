package com.chtj.framework_utils.entity;

public interface UpgradeInterface {
    /**
     * 固件安装前的执行操作回调
     * @param operatType
     */
    void operating(OperatType operatType);

    /**
     * 固件安装前的过程执行失败
     * @param throwable
     */
    void error(Throwable throwable);
}
