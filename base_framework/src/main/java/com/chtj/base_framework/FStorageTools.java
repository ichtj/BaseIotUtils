package com.chtj.base_framework;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.chtj.base_framework.entity.Space;
import com.chtj.base_framework.entity.RomSpace;

import java.io.File;

/**
 * 存储，空间相关工具类
 */
public class FStorageTools {
    private static final String TAG = "FStorageTools";

    /**
     * 获取ram占用
     *
     * @return
     */
    public static Space getRamSpace() {
        //获取运行内存的信息
        ActivityManager manager = (ActivityManager) FBaseTools.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(info);
        return new Space(info.totalMem, info.totalMem - info.availMem, info.availMem);
    }

    /**
     * 获取rom占用
     *
     * @return
     */
    public static RomSpace getRomSpace() {
        //获取ROM内存信息
        //调用该类来获取磁盘信息（而getDataDirectory就是内部存储）
        final StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        /*总共的block数*/
        long totalCounts = statFs.getBlockCountLong();
        /*获取可用的block数*/
        long availableCounts = statFs.getAvailableBlocksLong();
        /*每格所占的大小，一般是4KB==*/
        long size = statFs.getBlockSizeLong();
        /*可用内部存储大小*/
        long availRomSize = availableCounts * size;
        /*内部存储总大小*/
        long totalRomSize = totalCounts * size;
        return new RomSpace(totalRomSize, totalRomSize - availRomSize, availRomSize, availableCounts, totalCounts, size);
    }


    /**
     * 获取SDcard空间
     *
     * @return
     */
    public static Space getSdcardSpace() {
        long total = getTotalInternalMemorySize();
        long available = getAvailableInternalMemorySize();
        long use = total - available;
        return new Space(total, use, available);
    }

    /**
     * 获取手机内部空间总大小
     *
     * @return 大小，字节为单位
     */
    public static long getTotalInternalMemorySize() {
        //获取内部存储根目录
        File path = Environment.getDataDirectory();
        //系统的空间描述类
        StatFs stat = new StatFs(path.getPath());
        //每个区块占字节数
        long blockSize = stat.getBlockSize();
        //区块总数
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * 获取手机内部可用空间大小
     *
     * @return 大小，字节为单位
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        //获取可用区块数量
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }


    /**
     * 获取TF卡存储空间
     *
     * @return
     */
    public static Space getTfSpace() {
        FCmdTools.CommandResult commandResult = null;
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= 24) {
            commandResult = FCmdTools.execCommand("df | grep -rn /mnt/media_rw", true);
            if (commandResult.successMsg != null && commandResult.successMsg.length() > 0) {
                String[] result = commandResult.successMsg.substring(4).trim().replaceAll("\\s+", " ").split(" ");
                return new Space(Integer.valueOf(result[2]), Integer.valueOf(result[3]), Integer.valueOf(result[4]));
            } else {
                return new Space(0, 0, 0);
            }
        } else {
            commandResult = FCmdTools.execCommand("df | grep -rn /mnt/media_rw/extsd", true);
            Log.d(TAG, "getTfSpace: successMeg=" + commandResult.successMsg);
            if (commandResult.successMsg != null && commandResult.successMsg.length() > 0) {
                String[] resultCall = commandResult.successMsg.substring(4).trim().replaceAll("\\s+", " ").split(" ");
                return new Space(Long.parseLong(resultCall[2]), Long.parseLong(resultCall[3]), Long.parseLong(resultCall[4]));
            } else {
                return new Space(0, 0, 0);
            }
        }
    }
}
