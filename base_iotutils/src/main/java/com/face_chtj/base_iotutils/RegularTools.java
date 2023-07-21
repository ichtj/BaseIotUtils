package com.face_chtj.base_iotutils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegularTools {
    //ip地址校验
    public final static String REGULAR_IP="\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
    //网址校验
    public final static String REGULAR_URL="(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^(\\s()<>]+|\\(([^(\\s()<>]+\\))*\\)))*\\))+(?:\\(([^(\\s()<>]+|\\(([^(\\s()<>]+\\))*\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))";
    //邮箱校验
    public final static String REGULAR_EMAIL = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";

    /**
     * 通用的正则校验并返回数据
     * @param input 字符串内容
     * @param regexPattern 正则表达式
     * @param isCaseSensitive 区分大小写 true | 不区分大小写false
     * @return 结果
     */
    public static List<String> matchRegex(String input, String regexPattern, boolean isCaseSensitive) {
        List<String> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile(regexPattern,isCaseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String match = matcher.group();
            matches.add(match);
        }
        return matches;
    }
}
