package com.app.fresy.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class PageRoute {

    public static List<Activity> pages = new ArrayList<>();

    public static void openPage(Activity activity) {
        pages.add(activity);
    }

    public static void closePage(Activity activity) {
        pages.add(activity);
    }

}
