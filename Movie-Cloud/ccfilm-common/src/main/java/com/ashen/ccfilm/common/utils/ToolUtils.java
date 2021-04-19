package com.ashen.ccfilm.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基础工具类
 */
public class ToolUtils {
    private ToolUtils() {
    }

    /**
     * 字符串为空
     */
    public static boolean strIsNull(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * 字符串不为空
     */
    public static boolean strIsNotNul(String str) {
        if (strIsNull(str)) {
            return false;
        } else {
            return true;
        }
    }

    // 判断数字正则表达式
    private static final Pattern pattern = Pattern.compile("[0-9]*");

    // 检查字符串是不是int类型
    public static boolean checkInt(String str) {
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    // 字符串转换为int类型
    public static Integer str2Int(String str) {
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return 0;
        } else {
            return Integer.parseInt(str);
        }
    }

    // 字符串转换为LocalDateTime
    public static LocalDateTime str2LocalDateTime(String dateStr) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateStr, df);
    }
}
