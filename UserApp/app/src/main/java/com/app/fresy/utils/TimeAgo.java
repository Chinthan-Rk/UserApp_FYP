package com.app.fresy.utils;

import android.content.Context;

import com.app.fresy.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * source : https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87
 */

public class TimeAgo {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String get(Context ctx, long time) {

        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }


        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return ctx.getString(R.string.just_now);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return ctx.getString(R.string.a_minute_ago);
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " " + ctx.getString(R.string.minutes_ago);
        } else if (diff < 90 * MINUTE_MILLIS) {
            return ctx.getString(R.string.an_hour_ago);
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " " + ctx.getString(R.string.hours_ago);
        } else if (diff < 48 * HOUR_MILLIS) {
            return ctx.getString(R.string.yesterday);
        } else {
            if(diff / DAY_MILLIS >= 5){
                SimpleDateFormat newFormat = new SimpleDateFormat("dd MMM yy");
                return newFormat.format(new Date(time));
            } else {
                return diff / DAY_MILLIS + " " + ctx.getString(R.string.days_ago);
            }
        }
    }

}
