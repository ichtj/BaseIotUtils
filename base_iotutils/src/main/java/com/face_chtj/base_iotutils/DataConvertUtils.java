package com.face_chtj.base_iotutils;

/**
 * @author chtj
 * create by chtj on 2019-8-6
 * desc:进制转换工具类
 * --判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数 {@link #isOdd(int)}
 * --将int转成byte {@link #intToByte(int number)}
 * --将int转成hex字符串 {@link #intToHex(int number)}
 * --字节转十进制 {@link #byteToDec(byte b)}
 * --字节数组转十进制 {@link #bytesToDec(byte[] bytes)}
 * --Hex字符串转int {@link #hexToInt(String inHex)}
 * --字节转十六进制字符串 {@link #byteToHex(byte num)}
 * --十六进制转byte字节 {@link #hexToByte(String hexString)}
 * --字节数组转十六进制 {@link #encodeHexString(byte[] byteArray)}
 * --十六进制转字节数组 {@link #decodeHexString(String hexString)}
 * --十进制转十六进制 {@link #decToHex(int dec)}
 * --十六进制转十进制 {@link #hexToDec(String hex)}
 * --十进制转byte数组 低字节在前 {@link #decToByteLH(int n,int length)}
 * --十进制转byte数组 高字节在前 {@link #decToByteHL(int n,int length)}
 * --根据原始数据获取crc8 校验值 {@link #calcCrc8(byte[] data)}
 */
public class DataConvertUtils {

    /**
     * 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
     *
     * @param num
     * @return
     */
    public static int isOdd(int num) {
        return num & 0x1;
    }

    /**
     * 将int转成byte
     *
     * @param number
     * @return byte字节
     */
    public static byte intToByte(int number) {
        return hexToByte(intToHex(number));
    }

    /**
     * 将int转成hex字符串
     *
     * @param number
     * @return
     */
    public static String intToHex(int number) {
        String st = Integer.toHexString(number).toUpperCase();
        return String.format("%2s", st).replaceAll(" ", "0");
    }

    /**
     * 字节转十进制
     *
     * @param b
     * @return
     */
    public static int byteToDec(byte b) {
        String s = byteToHex(b);
        return (int) hexToDec(s);
    }

    /**
     * 字节数组转十进制
     *
     * @param bytes
     * @return
     */
    public static int bytesToDec(byte[] bytes) {
        String s = encodeHexString(bytes);
        return (int) hexToDec(s);
    }

    /**
     * Hex字符串转int
     *
     * @param inHex
     * @return
     */
    public static int hexToInt(String inHex) {
        return Integer.parseInt(inHex, 16);
    }

    /**
     * 字节转十六进制字符串
     *
     * @param num
     * @return
     */
    public static String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits).toUpperCase();
    }

    /**
     * 十六进制转byte字节
     *
     * @param hexString
     * @return
     */
    public static byte hexToByte(String hexString) {
        int firstDigit = toDigit(hexString.charAt(0));
        int secondDigit = toDigit(hexString.charAt(1));
        return (byte) ((firstDigit << 4) + secondDigit);
    }

    private static int toDigit(char hexChar) {
        int digit = Character.digit(hexChar, 16);
        if (digit == -1) {
            throw new IllegalArgumentException(
                    "Invalid Hexadecimal Character: " + hexChar);
        }
        return digit;
    }

    /**
     * 字节数组转十六进制
     *
     * @param byteArray
     * @return
     */
    public static String encodeHexString(byte[] byteArray) {
        if (byteArray == null) {
            return "";
        }
        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            hexStringBuffer.append(byteToHex(byteArray[i]));
        }
        return hexStringBuffer.toString().toUpperCase();
    }

    /**
     * 十六进制转字节数组
     *
     * @param hexString
     * @return
     */
    public static byte[] decodeHexString(String hexString) {
        if (hexString.length() % 2 == 1) {
            hexString=hexString+"0";
        }
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
        }
        return bytes;
    }

    /**
     * 十进制转十六进制
     *
     * @param dec
     * @return
     */
    public static String decToHex(int dec) {
        String hex = Integer.toHexString(dec);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        return hex.toLowerCase();
    }

    /**
     * 十六进制转十进制
     *
     * @param hex
     * @return
     */
    public static long hexToDec(String hex) {
        return Long.parseLong(hex, 16);
    }

    /**
     * 将int转为低字节在前，高字节在后的byte数组
     */
    public static byte[] decToByteLH(int n, int length) {
        byte[] b = new byte[length];
        int cursor = 0;
        for (int i = 0; i < length; i++) {
            b[i] = (byte) (n >> cursor & 0xff);
            cursor += 8;
        }
        return b;
    }

    /**
     * 将int转为高字节在前，低字节在后的byte数组
     * @param n int
     * @return byte[]
     */
    public static byte[] decToByteHL(int n,int length) {
        byte[] b = new byte[length];
        int cursor=0;
        for (int i = 0; i <length ; i++) {
            b[length-i-1]=(byte) (n >> cursor & 0xff);
            cursor += 8;
        }
        return b;
    }

    /**
     * 根据原始数据获取crc8 校验值
     *
     * @param data 原始数据
     * @return 校验值
     */
    public static byte calcCrc8(byte[] data) {
        byte crc = 0;
        for (int j = 0; j < data.length; j++) {
            crc ^= data[j];
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x01) != 0) {
                    crc = (byte) (((crc & 0xff)) >>> 1);
                    crc ^= 0x8c;
                } else {
                    crc = (byte) (((crc & 0xff)) >>> 1);
                }
            }
        }
        return crc;
    }

}