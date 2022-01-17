package com.ice.main.util;

import org.jetbrains.annotations.Contract;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class TimeFormatter {
    @Contract(pure = true)
    public static String timeFormat(Long input) {

        long sec = input % 60;
        Long min = input / 60 % 60 < 0 ? null : input / 60 % 60;
        Long hour = input / 60 / 60 % 60 < 0 ? null : input / 60 / 60 % 60;
        Long day = input / 60 / 60 / 60 % 24 < 0 ? null : input / 60 / 60 / 60 % 24;

        return (day == null ? "" : day + "天") + (hour == null ? "" : hour + "時") + (min == null ? "" : min + "分") + sec + "秒";
    }

    @Contract("_ -> new")
    public static OffsetDateTime millisToOffset(long epochMillis) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }
}
