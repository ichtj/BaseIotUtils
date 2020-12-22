package com.chtj.base_framework.entity;

public class RomSpace extends Space {
    private long availableCounts;
    private long totalCounts;
    private long eachBlockSize;

    public RomSpace(long totalSize, long useSize, long availableSize, long availableCounts, long totalCounts, long eachBlockSize) {
        super(totalSize, useSize, availableSize);
        this.availableCounts = availableCounts;
        this.totalCounts = totalCounts;
        this.eachBlockSize = eachBlockSize;
    }

    public long getAvailableCounts() {
        return availableCounts;
    }

    public void setAvailableCounts(long availableCounts) {
        this.availableCounts = availableCounts;
    }

    public long getTotalCounts() {
        return totalCounts;
    }

    public void setTotalCounts(long totalCounts) {
        this.totalCounts = totalCounts;
    }

    public long getEachBlockSize() {
        return eachBlockSize;
    }

    public void setEachBlockSize(long eachBlockSize) {
        this.eachBlockSize = eachBlockSize;
    }
}
