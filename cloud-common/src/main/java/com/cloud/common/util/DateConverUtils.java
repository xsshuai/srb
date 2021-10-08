package com.cloud.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @ClassName DateConverUtils
 * @Author xsshuai
 * @Date 2021/8/26 2:30 下午
 **/
public class DateConverUtils {

    public static LocalDate dateConver(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, dateTimeFormatter);
    }
}
