package com.qiyun.qhfg.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Wang Ying
 * @version 1.0
 * @date 2023/3/25 12:49
 */
public class DateUtil {
    public static final  String TYPE1 ="yyyy-MM-dd HH:mm:ss";
    public static final  String TYPE2="yyyy-MM-dd HH:mm";
    public static final  String TYPE3="yyyy-MM-dd HH";
    public static final  String TYPE4="yyyy-MM-dd";
    public static final  String TYPE5="yyyy-MM";
    public static final  String TYPE6="yyyy";
    public static final  String TYPE7="yyyyMM";
    public static final  String TYPE8="yyyyMMdd";
    public static final  String TYPE9="yyMMddHHmm";
    public static final  String TYPE10="yyyyMMddHHmmss";
    public static final  String TYPE11="yyyyMMddHHmm";
    public static String dateformat(Date date, String pattern){
        if (date == null)return null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }
    public static Date stringformat(String date, String pattern){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        try {
            return simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getTargetTimeStr(String time,String format,int field, int amount) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sdf.parse(time));
        calendar.add(field, amount);
        return sdf.format(calendar.getTime());
    }

    public static String getBeforeDay(String dateStr,String pattern,int num){
        Date date = stringformat(dateStr,pattern);
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH,num);
        date = calendar.getTime();
        return dateformat(date,pattern);
    }

    public static long getDuration(Date startTime,Date endTime){
        //强转格式
        Date startDate = new Date(startTime.getTime());
        Date endDate = new Date(endTime.getTime());
        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        //持续时间等于结束日期减开始日期
        return ChronoUnit.DAYS.between(startLocalDate, endLocalDate)+1;
    }
}
