package com.jqk.pictureselectorlibrary.util;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class FormatUtils {
    public static String duration2Time(int duration) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        String time = format.format(duration);
        return time;
    }
}
