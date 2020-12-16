package com.chtj.framework;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 屏幕相关
 */
public class FScreentTools {
    private static final String TAG = "ScreentShotUtil";

    private static final String CLASS1_NAME = "android.view.SurfaceControl";

    private static final String CLASS2_NAME = "android.view.Surface";

    private static final String METHOD_NAME = "screenshot";


    /**
     * 截屏
     * 默认保存在sdcard目录下
     *
     * @return
     */
    public static boolean takeScreenshot() {
        return takeScreenshot("");
    }

    /**
     * 截屏
     *
     * @param fileFullPath 路径+文件名
     * <p>
     * 例如：/sdcard/local/20201515.png
     */
    public static boolean takeScreenshot(String fileFullPath) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = format.format(new Date(System.currentTimeMillis())) + ".png";
        if (fileFullPath == "") {
            fileFullPath = "/sdcard/" + fileName;
        }
        if (FShellTools.isRoot() || FShellTools.execCommand("mount -o rw,remount -t ext4 /system", true).result == 0) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                return FShellTools.execCommand("/system/bin/screencap -p " + fileFullPath + fileName, true).result == 0;
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                WindowManager wm = (WindowManager) FBaseTools.getContext().getSystemService(Context.WINDOW_SERVICE);
                Display mDisplay = wm.getDefaultDisplay();
                Matrix mDisplayMatrix = new Matrix();
                DisplayMetrics mDisplayMetrics = new DisplayMetrics();
                // We need to orient the screenshot correctly (and the Surface api seems to take screenshots
                // only in the natural orientation of the device :!)
                mDisplay.getRealMetrics(mDisplayMetrics);
                float[] dims =
                        {
                                mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels
                        };
                float degrees = getDegreesForRotation(mDisplay.getRotation());
                boolean requiresRotation = (degrees > 0);
                if (requiresRotation) {
                    // Get the dimensions of the device in its native orientation
                    mDisplayMatrix.reset();
                    mDisplayMatrix.preRotate(-degrees);
                    mDisplayMatrix.mapPoints(dims);
                    dims[0] = Math.abs(dims[0]);
                    dims[1] = Math.abs(dims[1]);
                }

                Bitmap mScreenBitmap = screenShot((int) dims[0], (int) dims[1]);
                if (requiresRotation) {
                    // Rotate the screenshot to the current orientation
                    Bitmap ss = Bitmap.createBitmap(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels,
                            Bitmap.Config.ARGB_8888);
                    Canvas c = new Canvas(ss);
                    c.translate(ss.getWidth() / 2, ss.getHeight() / 2);
                    c.rotate(degrees);
                    c.translate(-dims[0] / 2, -dims[1] / 2);
                    c.drawBitmap(mScreenBitmap, 0, 0, null);
                    c.setBitmap(null);
                    mScreenBitmap = ss;
                    if (ss != null && !ss.isRecycled()) {
                        ss.recycle();
                    }
                }

                // If we couldn't take the screenshot, notify the user
                if (mScreenBitmap == null) {
                    return false;
                }

                // Optimizations
                mScreenBitmap.setHasAlpha(false);
                mScreenBitmap.prepareToDraw();

                saveBitmap2file(mScreenBitmap, fileFullPath);
            }
            return true;
        }
        return false;
    }


    /**
     * 保存
     * @param bmp
     * @param fileName
     */
    public static void saveBitmap2file(Bitmap bmp, String fileName) {
        int quality = 100;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, quality, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        byte[] buffer = new byte[1024];
        int len = 0;
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdir();
                file.getParentFile().createNewFile();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            try {
                file.getParentFile().delete();
                file.getParentFile().createNewFile();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            while ((len = is.read(buffer)) != -1) {
                stream.write(buffer, 0, len);
            }
            stream.flush();
        } catch (FileNotFoundException e) {
            Log.i(TAG, e.toString());
        } catch (IOException e) {
            Log.i(TAG, e.toString());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.i(TAG, e.toString());
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.i(TAG, e.toString());
                }
            }
        }
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
        }
    }

    /**
     * 旋转角度
     */
    private static float getDegreesForRotation(int value) {
        switch (value) {
            case Surface.ROTATION_90:
                return 360f - 90f;
            case Surface.ROTATION_180:
                return 360f - 180f;
            case Surface.ROTATION_270:
                return 360f - 270f;
        }
        return 0f;
    }

    private static Bitmap screenShot(int width, int height) {
        Log.i(TAG, "android.os.Build.VERSION.SDK : " + android.os.Build.VERSION.SDK_INT);
        Class<?> surfaceClass = null;
        Method method = null;
        try {
            Log.i(TAG, "width : " + width);
            Log.i(TAG, "height : " + height);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {

                surfaceClass = Class.forName(CLASS1_NAME);
            } else {
                surfaceClass = Class.forName(CLASS2_NAME);
            }
            method = surfaceClass.getDeclaredMethod(METHOD_NAME, int.class, int.class);
            method.setAccessible(true);
            return (Bitmap) method.invoke(null, width, height);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, e.toString());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.toString());
        } catch (IllegalAccessException e) {
            Log.e(TAG, e.toString());
        } catch (InvocationTargetException e) {
            Log.e(TAG, e.toString());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

}