package top.keepempty.util;

import android.util.Log;

/**
 * Create on 2019/9/25
 * author chtj
 */
public class DataCehck {
    public static final String TAG="DataCehck";
    /**
     * 根据原始数据获取crc8 校验值
     * @param data 原始数据
     * @return 校验值
     */
    public static  byte calcCrc8(byte[] data){
        byte crc = 0;
        for (int j = 0; j < data.length; j++)
        {
            crc ^= data[j];
            for (int i = 0; i < 8; i++)
            {
                if ((crc & 0x01) != 0)
                {
                    crc = (byte) (((crc & 0xff ))>>> 1);
                    crc ^= 0x8c;
                }
                else
                {
                    crc = (byte) (((crc & 0xff ))>>> 1);
                }
            }
        }
        Log.d(TAG ,"calcCrc8=: "+crc);
        return crc;
    }

}
