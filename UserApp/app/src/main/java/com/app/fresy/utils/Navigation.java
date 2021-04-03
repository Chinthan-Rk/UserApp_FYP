package com.app.fresy.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class Navigation {
    static List<Activity> activities = new ArrayList<>();

    public static void onStart(Activity activity){
        activities.add(activity);
    }

    public static void onFinish(Activity activity){
        activities.add(activity);
    }


}
