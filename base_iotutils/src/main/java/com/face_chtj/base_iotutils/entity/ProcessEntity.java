package com.face_chtj.base_iotutils.entity;

/**
 * Create on 2020/8/7
 * author chtj
 * desc 进程详情
 */
public class ProcessEntity {
    private int pid;
    private String processName;

    public ProcessEntity() {
    }

    public ProcessEntity(int pid, String processName) {
        this.pid = pid;
        this.processName = processName;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }
}
