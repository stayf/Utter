package com.stayfprod.utter.util;

import com.stayfprod.utter.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public static final long SECOND_IN_MILLIS = 1000;

    public static final int MINUTE = 60;
    public static final int HOUR = 60 * MINUTE;
    public static final int DAY = HOUR * 24;

    private final static SimpleDateFormat PATTERN_JUST_TIME = new SimpleDateFormat("h:mm a", Locale.US);
    private final static SimpleDateFormat PATTERN_CL_NOT_THIS_YEAR = new SimpleDateFormat("dd.MM.yy", Locale.US);
    private final static SimpleDateFormat PATTERN_CL_THIS_YEAR = new SimpleDateFormat("MMM d", Locale.US);
    private final static SimpleDateFormat PATTERN_CL_THIS_WEEK = new SimpleDateFormat("E", Locale.US);

    private final static SimpleDateFormat PATTERN_CS_NOT_THIS_YEAR = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
    private final static SimpleDateFormat PATTERN_CS_THIS_YEAR = new SimpleDateFormat("MMMM d", Locale.US);

    private final static SimpleDateFormat PATTERN_SM_NOT_THIS_YEAR = new SimpleDateFormat("MMMM yyyy", Locale.US);
    private final static SimpleDateFormat PATTERN_SM_THIS_YEAR = new SimpleDateFormat("MMMM yyyy", Locale.US);

    public enum DateType {
        CHAT_LIST,
        CHAT_MSG,
        CHAT_SEPARATOR,
        SHARED_MEDIA
    }

    public static boolean isDifMonths(int firstDate, int lastDate) {

        Calendar firstCalendar = Calendar.getInstance();
        firstCalendar.setTime(new Date(firstDate * SECOND_IN_MILLIS));

        Calendar lastCalendar = Calendar.getInstance();
        lastCalendar.setTime(new Date(lastDate * SECOND_IN_MILLIS));

        int firstMonth = firstCalendar.get(Calendar.MONTH);
        int lastMonth = lastCalendar.get(Calendar.MONTH);

        int diffMonth = firstMonth - lastMonth;

        if (diffMonth != 0) {
            return true;
        }
        return false;
    }

    public static boolean isDifDates(int firstDate, int lastDate) {

        Calendar firstCalendar = Calendar.getInstance();
        firstCalendar.setTime(new Date(firstDate * SECOND_IN_MILLIS));

        Calendar lastCalendar = Calendar.getInstance();
        lastCalendar.setTime(new Date(lastDate * SECOND_IN_MILLIS));

        int firstDAY = firstCalendar.get(Calendar.DAY_OF_YEAR);
        int lastDAY = lastCalendar.get(Calendar.DAY_OF_YEAR);
        int diffDAYS = firstDAY - lastDAY;

        if (diffDAYS != 0) {
            return true;
        }
        return false;
    }

    //todo глянуть как ведет себя апи при смены дня! Можно самому сделать обновление дат в списке.
    public static String getDateForChat(int receivedTimeUTC, DateType dateType) {
        long longReceivedTime = ((long) receivedTimeUTC) * SECOND_IN_MILLIS; //+ ((long)TimeZone.getDefault().getRawOffset());

        Date receivedDate = new Date(longReceivedTime);
        if (dateType == DateType.CHAT_MSG) {
            return PATTERN_JUST_TIME.format(receivedDate);
        }

        Date currDate = new Date();

        SimpleDateFormat pattern;

        Calendar currCalendar = Calendar.getInstance();
        currCalendar.setTime(currDate);
        Calendar receivedCalendar = Calendar.getInstance();
        receivedCalendar.setTime(receivedDate);

        int receivedYEAR = receivedCalendar.get(Calendar.YEAR);
        int currYEAR = currCalendar.get(Calendar.YEAR);


        if (dateType == DateType.CHAT_SEPARATOR) {
            //не в этом году
            if (receivedYEAR != currYEAR) {
                pattern = PATTERN_CS_NOT_THIS_YEAR;
                return pattern.format(receivedDate);
            } else {
                pattern = PATTERN_CS_THIS_YEAR;
                return pattern.format(receivedDate);
            }
        }

        if (dateType == DateType.SHARED_MEDIA) {
            //не в этом году
            if (receivedYEAR != currYEAR) {
                pattern = PATTERN_SM_NOT_THIS_YEAR;
                return pattern.format(receivedDate);
            } else {
                pattern = PATTERN_SM_THIS_YEAR;
                return pattern.format(receivedDate);
            }
        }

        //не в этом году
        if (receivedYEAR != currYEAR) {
            pattern = dateType == DateType.CHAT_LIST ? PATTERN_CL_NOT_THIS_YEAR : PATTERN_CS_NOT_THIS_YEAR;
            return pattern.format(receivedDate);
        }

        int receivedDAY = receivedCalendar.get(Calendar.DAY_OF_YEAR);
        int currDAY = currCalendar.get(Calendar.DAY_OF_YEAR);

        int diffDAYS = currDAY - receivedDAY;
        //сегодня
        if (diffDAYS == 0) {
            pattern = PATTERN_JUST_TIME;
            return pattern.format(receivedDate);
        }
        //вчера
        if (diffDAYS >= 1 && diffDAYS <= 6) {
            pattern = dateType == DateType.CHAT_LIST ? PATTERN_CL_THIS_WEEK : PATTERN_CS_THIS_YEAR;
            return pattern.format(receivedDate);
        }
        //От 7 дней в этом году
        pattern = dateType == DateType.CHAT_LIST ? PATTERN_CL_THIS_YEAR : PATTERN_CS_THIS_YEAR;
        return pattern.format(receivedDate);
    }

    public static boolean isOnline(int wasOnline) {
        if (wasOnline == 0) {
            return false;
        }

        long dif = (System.currentTimeMillis() / SECOND_IN_MILLIS) - wasOnline;
        return dif < MINUTE;
    }

    public static String calculateLastSeen(int wasOnline) {
        String returned = "";
        if (wasOnline == 0) {
            return returned;
        }

        long dif = (System.currentTimeMillis() / SECOND_IN_MILLIS) - wasOnline;

        if (dif < MINUTE) {
            returned += AndroidUtil.getResourceString(R.string.online);
        } else if (dif < HOUR) {
            long minutes = dif / MINUTE;
            returned += AndroidUtil.getResourceString(R.string.last_seen) + minutes + AndroidUtil.getResourceString(R.string.minutes_ago);
        } else if (dif < DAY) {
            long hours = dif / HOUR;
            returned += AndroidUtil.getResourceString(R.string.last_seen) + hours + AndroidUtil.getResourceString(R.string.hours_ago);
        } else {
            Date receivedDate = new Date(wasOnline * SECOND_IN_MILLIS);
            SimpleDateFormat pattern = PATTERN_CS_NOT_THIS_YEAR;
            returned = AndroidUtil.getResourceString(R.string.last_seen) + pattern.format(receivedDate);
        }
        return returned;
    }
}