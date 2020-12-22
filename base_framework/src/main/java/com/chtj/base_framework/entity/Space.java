package com.chtj.base_framework.entity;

public class Space {
    private long totalSize;
    private long useSize;
    private long availableSize;

    public Space(long totalSize, long useSize, long availableSize) {
        this.totalSize = totalSize;
        this.useSize = useSize;
        this.availableSize = availableSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getUseSize() {
        return useSize;
    }

    public void setUseSize(long useSize) {
        this.useSize = useSize;
    }

    public long getAvailableSize() {
        return availableSize;
    }

    public void setAvailableSize(long availableSize) {
        this.availableSize = availableSize;
    }
}
