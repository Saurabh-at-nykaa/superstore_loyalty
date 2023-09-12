package com.nykaa.loyalty.util;

import org.apache.commons.lang.time.DateUtils;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(Constants.YYYY_MM_DD_HH_mm_ss);

    public static Date getDateAfterDays(int noOfDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, noOfDays);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static Date getCronStartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 55);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public static Date getCronEndDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 5);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static DateFormat getEventSchedularParser() {
        return new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
    }

    public static Date getStartDateOfMonth(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getEndDateOfMonth(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Calendar getCalendarByMonth(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, month);
        return calendar;
    }

    public static String parseDate(Date date) {
        return SIMPLE_DATE_FORMAT.format(date);
    }

    public static Date getOfferEndEventDate(Date date) {
        Calendar calendar = DateUtils.toCalendar(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, randomGenerator());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static int randomGenerator() {
        SecureRandom random = new SecureRandom();
        return random.nextInt(60);
    }

    public static Date getOfferStartEventDate(Date date) {
        int dwhSyncHour = Integer.parseInt(SystemPropertyUtil
                .getProperty(Constants.Dwh.DWH_SYNC_HOUR, Constants.Dwh.DWH_SYNC_DEFAULT_HOUR));
        Calendar calendar = DateUtils.toCalendar(date);
        calendar.set(Calendar.HOUR_OF_DAY, dwhSyncHour);
        calendar.set(Calendar.MINUTE, randomGenerator());
        return calendar.getTime();
    }

    public static Date getOfferStartDate(Date date) {
        Calendar calendar = DateUtils.toCalendar(date);
        int dwhSyncHour = Integer.parseInt(SystemPropertyUtil
                .getProperty(Constants.Dwh.DWH_SYNC_HOUR, Constants.Dwh.DWH_SYNC_DEFAULT_HOUR));
        calendar.set(Calendar.HOUR_OF_DAY, dwhSyncHour + 2);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getOfferEndDate(Date date) {
        Calendar calendar = DateUtils.toCalendar(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
