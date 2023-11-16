package com.face_chtj.base_iotutils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * regular tools
 */
public class RegularTools {
    //ip address verification
    public final static String REGULAR_IP="\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
    //URL verification
    public final static String REGULAR_URL="(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^(\\s()<>]+|\\(([^(\\s()<>]+\\))*\\)))*\\))+(?:\\(([^(\\s()<>]+|\\(([^(\\s()<>]+\\))*\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))";
    //email verification
    public final static String REGULAR_EMAIL = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
    //time HH:mm
    private static final String TIME_PATTERN = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$";

    /**
     * general regular method
     * @param input string content
     * @param regexPattern expression
     * @param isCaseSensitive is it case sensitive [true | false]
     * @return result list
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
