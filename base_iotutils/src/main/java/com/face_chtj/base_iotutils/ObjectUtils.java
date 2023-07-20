package com.face_chtj.base_iotutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Create on 2020/3/17
 * author chtj
 *
 * {@link #isEmpty(String)} 判断字符是否为空
 * {@link #getRandomList(String[], int)}  两重循环去重,随机指定范围内N个不重复的数
 */
public class ObjectUtils {
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
     * 判断obj是否为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null || obj.toString().length() == 0|| "null".equals(obj.toString())) {
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
     * 获取集合中随机个数不重复的项，返回下标数组
     *
     * @param list 数据列表
     * @param num  随机数量
     * @return 子集
     */
    public static String[] getRandomList(String[] list, int num) {
        if (num >= list.length) {
            //如果集合的长度小于等于需要产生的长度
            return shuffleStringArray(list);
        }else{
            List<Integer> indexList = new ArrayList<Integer>();
            // 创建随机数
            Random random = new Random();
            // 当set长度不足 指定数量时
            List flagExist = new ArrayList();
            while (flagExist.size() < num) {
                // 获取源集合的长度的随机数
                Integer index = random.nextInt(list.length);
                // 获取随机数下标对应的元素
                Object obj = list[index];
                // 不包含该元素时
                if (!flagExist.contains(obj)) {
                    // 添加到集合中
                    flagExist.add(obj);
                    // 记录下标
                    indexList.add(index);
                }
            }
            String[] pingList=new String[indexList.size()];
            for (int i = 0; i < indexList.size(); i++) {
                pingList[i]=list[indexList.get(i)];
            }
            return pingList;
        }
    }

    /**
     * 将一组String字符串随机排序后放到新的String[]中
     */
    public static String[] shuffleStringArray(String[] array) {
        if(array==null||array.length<=0){
            return new String[0];
        }
        // 复制原始数组，避免修改原始数组
        String[] shuffledArray = Arrays.copyOf(array, array.length);
        Random random = new Random();
        // 从数组的最后一个元素开始，依次与随机位置的元素交换位置
        for (int i = shuffledArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String temp = shuffledArray[i];
            shuffledArray[i] = shuffledArray[j];
            shuffledArray[j] = temp;
        }
        return shuffledArray;
    }
}
