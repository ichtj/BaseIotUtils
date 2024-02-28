package com.ichtj.basetools.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.face_chtj.base_iotutils.KLog;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ScreenshotUtil {

    public static Bitmap takeScreenshot() {
        Bitmap screenshot = null;

        try {
            Process process = Runtime.getRuntime().exec("su");

            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes("/system/bin/screencap -p\n");
            outputStream.flush();
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            InputStream inputStream=process.getInputStream();
            InputStreamReader inputStreamReader=new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            KLog.d("screenshotBytes >>> "+sb.toString());
            byte[] screenshotBytes = sb.toString().getBytes();
            ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(screenshotBytes);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            screenshot = BitmapFactory.decodeStream(/*arrayInputStream,null,options*/inputStream);

            reader.close();
            outputStream.close();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return screenshot;
    }

    public static boolean saveBitmapToSdCard(Bitmap bitmap, String fileName) {
        File directory = Environment.getExternalStorageDirectory();
        File file = new File(directory, fileName);

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
