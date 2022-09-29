package com.face_chtj.base_iotutils.convert;

/**
 * Create on 2020/3/17
 * author chtj
 *
 * {@link #isEmpty(String)} 判断字符是否为空
 * {@link #randomCommon(int, int, int)}  两重循环去重,随机指定范围内N个不重复的数
 */
public class TypeDataUtils {
    /**
     * 是否为空
     * @param str 字符串
     * @return true 空 false 非空
     */
    public static Boolean isEmpty(String str) {
        if(str == null || str.length() == 0 || "null".equals(str)) {
            return true;
        }
        return false;
    }

    /**
     * 数组中的字符串是否为空
     * @param array 字符串
     * @return true 空 false 非空
     */
    public static Boolean isEmpty(String... array) {
        for (int i = 0; i < array.length; i++) {
            if(isEmpty(array[i])){
                return true;
            }
        }
        return false;
    }

    /**
     * 两重循环去重,随机指定范围内N个不重复的数
     * min ~ max之间的数必须是连续的数
     * @param min        指定范围最小值
     * @param max        指定范围最大值
     * @param randomSize 随机数个数
     */
    public static int[] randomNums(int min, int max, int randomSize) {
        if (randomSize > (max - min + 1) || max < min) {
            return null;
        }
        int[] result = new int[randomSize];
        int count = 0;
        while (count < randomSize) {
            int num = (int) (Math.random() * (max - min)) + min;
            boolean flag = true;
            for (int j = 0; j < randomSize; j++) {
                if (num == result[j]) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                result[count] = num;
                count++;
            }
        }
        return result;
    }

}
