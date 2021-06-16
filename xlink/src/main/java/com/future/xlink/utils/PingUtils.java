package com.future.xlink.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PingUtils {
    private static final String TAG = "PingUtils";
    public static String ping(String host, int pingCount, StringBuffer stringBuffer) {
        String result = null;
        String line = null;
        Process process = null;
        BufferedReader successReader = null;
        try {
            String url=null;
            if (host.startsWith("http://")){
                url=host.substring("http://".length());
            }else if (host.startsWith("https://")){
                url=host.substring("https://".length());
            }else {
                url=host;
            }
            String [] pingHosts=url.split(":");
            String command = "ping -c " + pingCount + " " + pingHosts[0];
            process = Runtime.getRuntime().exec(command);
            if (process == null) {
                Log.d(TAG, "ping ping fail:process is null.");
                append(stringBuffer, "ping fail:process is null.");
                return result;
            }
            successReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = successReader.readLine()) != null) {
                Log.d(TAG, "ping line-->" + line);
                if (line.contains("rtt min/avg/max/mdev = ")) {
                    //获取avg数据返回
                    result = host + "#" + parsedata(line);
                }
                append(stringBuffer, line);
            }
            int status = process.waitFor();
            if (status == 0) {
                Log.d(TAG, "ping exec cmd success:" + command);
                append(stringBuffer, "exec cmd success:" + command);
            } else {
                Log.d(TAG, "ping exec cmd fail.");
                append(stringBuffer, "exec cmd fail.");
            }
            Log.d(TAG, "ping exec finished.");
            append(stringBuffer, "exec finished.");
        } catch (InterruptedException e) {
            Log.e(TAG, "ping", e);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
            if (successReader != null) {
                try {
                    successReader.close();
                } catch (IOException e) {
                    Log.e(TAG, "ping", e);
                }
            }
        }
        return result;
    }

    public static boolean ping(String host) {
        String line = null;
        Process process = null;
        boolean result=false;
        try {
            String url=null;
            if (host.startsWith("http://")){
                url=host.substring("http://".length());
            }else if (host.startsWith("https://")){
                url=host.substring("https://".length());
            }else {
                url=host;
            }
            String [] pingHosts=url.split(":");
            String command = "ping -c " + 3 + " " + pingHosts[0];
            //Log.d(PingUtils.class, "ping", command);
            process = Runtime.getRuntime().exec(command);
            if (process == null) {
                Log.d(TAG, "ping ping fail:process is null.");
                return result;
            }

            int status = process.waitFor();
            if (status == 0) {
                Log.d(TAG, "ping exec cmd success:" + command);
                result=true;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "ping", e);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Log.d(PingUtils.class, "ping", "ping exit.");
            if (process != null) {
                process.destroy();
            }

        }
        return result;
    }
    private static void append(StringBuffer stringBuffer, String text) {
        if (stringBuffer != null) {
            stringBuffer.append(text + "/n");
        }
    }

    private static String parsedata(String line) {
        try {
            String end = line.substring("rtt min/avg/max/mdev = ".length());
            return end.split("/")[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
