package com.app.fresy.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import com.app.fresy.ActivityDialogNotification;
import com.app.fresy.R;
import com.app.fresy.data.Constant;
import com.app.fresy.data.SharedPref;
import com.app.fresy.model.type.NotifType;
import com.app.fresy.room.AppDatabase;
import com.app.fresy.room.DAO;
import com.app.fresy.room.table.NotificationEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FcmMessagingService extends FirebaseMessagingService {

    private static int VIBRATION_TIME = 500; // in millisecond
    private SharedPref sharedPref;
    private int retry_count = 0;
    private DAO dao;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        sharedPref = new SharedPref(this);
        sharedPref.setFcmRegId(s);
        sharedPref.setNeedRegister(true);
        sharedPref.setSubscibeNotif(false);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sharedPref = new SharedPref(this);
        dao = AppDatabase.getDb(this).get();

        retry_count = 0;
        if (!sharedPref.getPushNotification()) return;
        try {
            NotificationEntity ne = null;
            if (remoteMessage.getData().size() > 0) {
                Object obj = remoteMessage.getData();
                ne = new Gson().fromJson(new Gson().toJson(obj), NotificationEntity.class);
            }
            if (remoteMessage.getNotification() != null) {
                RemoteMessage.Notification rn = remoteMessage.getNotification();
                if (ne == null) ne = new NotificationEntity();
                ne.title = rn.getTitle();
                ne.content = rn.getBody();
                ne.type = NotifType.NORMAL.name();
            }

            if (ne == null) return;
            ne.id = System.currentTimeMillis();
            ne.created_at = System.currentTimeMillis();
            ne.read = false;

            // display notification
            prepareImageNotification(ne);

            // save notification to room db
            saveNotification(ne);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareImageNotification(final NotificationEntity notif) {
        String image_url = null;
        if (notif.type.equalsIgnoreCase(NotifType.IMAGE.name())) {
            image_url = notif.data;
        }

        if (image_url != null) {
            glideLoadImageFromUrl(this, image_url, new CallbackImageNotif() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    showNotification(notif, bitmap);
                }

                @Override
                public void onFailed(String string) {
                    Log.e("onFailed", string);
                    if (retry_count <= Constant.LOAD_IMAGE_NOTIF_RETRY) {
                        retry_count++;
                        prepareImageNotification(notif);
                    } else {
                        showNotification(notif, null);
                    }
                }
            });
        } else {
            showNotification(notif, null);
        }
    }

    private void showNotification(NotificationEntity notif, Bitmap bitmap) {
        Intent intent = ActivityDialogNotification.navigateBase(this, notif, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        String channelId = getString(R.string.notification_channel_server);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setContentTitle(notif.title);
        builder.setContentText(notif.content);
        builder.setSmallIcon(R.drawable.logo_notification);
        builder.setDefaults(android.app.Notification.DEFAULT_LIGHTS);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(android.app.Notification.PRIORITY_HIGH);
        }
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);
        builder.setLargeIcon(largeIcon);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(notif.content));
        if (bitmap != null) {
            builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap).setSummaryText(notif.content));
        }

        // display push notif
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
        int unique_id = (int) System.currentTimeMillis();
        notificationManager.notify(unique_id, builder.build());

        vibrationAndPlaySound();
    }

    private void vibrationAndPlaySound() {
        // play vibration
        if (sharedPref.getVibration()) {
            if (Build.VERSION.SDK_INT >= 26) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.DEFAULT_AMPLITUDE);
            } else {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VIBRATION_TIME);
            }
        }
        // play tone
        try {
            RingtoneManager.getRingtone(this, Uri.parse(sharedPref.getRingtone())).play();
        } catch (Exception e) {
        }
    }


    // load image with callback
    Handler mainHandler = new Handler(Looper.getMainLooper());
    Runnable myRunnable;

    private void glideLoadImageFromUrl(final Context ctx, final String url, final CallbackImageNotif callback) {

        myRunnable = new Runnable() {
            @Override
            public void run() {
                Glide.with(ctx).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        callback.onSuccess(bitmap);
                        mainHandler.removeCallbacks(myRunnable);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        callback.onFailed("On Load Failed");
                        mainHandler.removeCallbacks(myRunnable);
                    }
                });
            }
        };
        mainHandler.post(myRunnable);
    }

    private void saveNotification(NotificationEntity notification) {
        dao.insertNotification(notification);
    }

    public interface CallbackImageNotif {

        void onSuccess(Bitmap bitmap);

        void onFailed(String string);

    }

}
