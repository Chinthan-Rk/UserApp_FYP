package com.app.fresy.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.app.fresy.R;
import com.app.fresy.model.User;
import com.google.gson.Gson;

public class SharedPref {

    private static String default_ringtone_url = "content://settings/system/notification_sound";
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences prefs;

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("MAIN_PREF", Context.MODE_PRIVATE);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // Preference for Fcm register
    public void setFcmRegId(String fcmRegId) {
        sharedPreferences.edit().putString("FCM_PREF_KEY", fcmRegId).apply();
    }

    public String getFcmRegId() {
        return sharedPreferences.getString("FCM_PREF_KEY", null);
    }

    public boolean isFcmRegIdEmpty() {
        return TextUtils.isEmpty(getFcmRegId());
    }

    public void setNeedRegister(boolean value) {
        sharedPreferences.edit().putBoolean("NEED_REGISTER", value).apply();
    }

    public boolean isNeedRegister() {
        return sharedPreferences.getBoolean("NEED_REGISTER", true);
    }

    public void setSubscibeNotif(boolean value) {
        sharedPreferences.edit().putBoolean("SUBSCRIBE_NOTIF", value).apply();
    }

    public boolean isSubscibeNotif() {
        return sharedPreferences.getBoolean("SUBSCRIBE_NOTIF", false);
    }

    // Preference for User Login
    public void saveUser(User user) {
        String str = new Gson().toJson(user);
        sharedPreferences.edit().putString("USER_PREF_KEY", str).apply();
    }

    public User getUser() {
        String str = sharedPreferences.getString("USER_PREF_KEY", null);
        if (str != null) {
            return new Gson().fromJson(str, User.class);
        }
        return null;
    }

    public void clearUser() {
        sharedPreferences.edit().putString("USER_PREF_KEY", null).apply();
    }

    // To save dialog permission state
    public void setNeverAskAgain(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getNeverAskAgain(String key) {
        return sharedPreferences.getBoolean(key, false);
    }


    // Preference for first launch
    public void setFirstLaunch(boolean flag) {
        sharedPreferences.edit().putBoolean("FIRST_LAUNCH", flag).apply();
    }

    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean("FIRST_LAUNCH", true);
    }

    // Preference for settings
    public void setPushNotification(boolean value) {
        sharedPreferences.edit().putBoolean("SETTINGS_PUSH_NOTIF", value).apply();
    }

    public boolean getPushNotification() {
        return sharedPreferences.getBoolean("SETTINGS_PUSH_NOTIF", true);
    }

    public void setVibration(boolean value) {
        sharedPreferences.edit().putBoolean("SETTINGS_VIBRATION", value).apply();
    }

    public boolean getVibration() {
        return sharedPreferences.getBoolean("SETTINGS_VIBRATION", true);
    }

    public void setRingtone(String value) {
        sharedPreferences.edit().putString("SETTINGS_RINGTONE", value).apply();
    }

    public String getRingtone() {
        return sharedPreferences.getString("SETTINGS_RINGTONE", default_ringtone_url);
    }

    public String getRingtoneName() {
        String current = getRingtone();
        if (current.equals(default_ringtone_url)) {
            return context.getString(R.string.ringtone_default);
        }
        Ringtone ringtone = RingtoneManager.getRingtone(context, Uri.parse(current));
        if (ringtone == null) {
            return context.getString(R.string.ringtone_default);
        }
        return ringtone.getTitle(context);
    }

    public void setImageCache(boolean value) {
        sharedPreferences.edit().putBoolean("SETTINGS_IMG_CACHE", value).apply();
    }

    public boolean getImageCache() {
        return sharedPreferences.getBoolean("SETTINGS_IMG_CACHE", true);
    }

    public void setSelectedTheme(int index) {
        sharedPreferences.edit().putInt("SETTINGS_THEME", index).apply();
    }

    public int getSelectedTheme() {
        return sharedPreferences.getInt("SETTINGS_THEME", 0);
    }

    public void setTextSize(int size) {
        sharedPreferences.edit().putInt("SETTINGS_TEXT", size).apply();
    }

    public int getTextSize() {
        return sharedPreferences.getInt("SETTINGS_TEXT", 16);
    }

}
