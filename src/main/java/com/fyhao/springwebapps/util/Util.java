package com.fyhao.springwebapps.util;

import java.util.Date;

public class Util {
    public static java.sql.Timestamp getSQLTimestamp(Date date) {
        return new java.sql.Timestamp(date.getTime());
    }
}