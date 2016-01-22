package de.jonasrottmann.realmbrowser;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.RealmObject;

public final class RealmBrowser {

    public static final int NOTIFICATION_ID = 1000;

    private static final RealmBrowser sInstance = new RealmBrowser();
    private final List<Class<? extends RealmObject>> mRealmModelList;
    private RealmFactory realmFactory;



    private RealmBrowser() {
        mRealmModelList = new ArrayList<>();
    }



    public static RealmBrowser getInstance() {
        return sInstance;
    }



    public static void startRealmFilesActivity(@NonNull Context context) {
        RealmFilesActivity.start(context);
    }



    public static void startRealmModelsActivity(@NonNull Context context, @NonNull String realmFileName) {
        RealmModelsActivity.start(context, realmFileName);
    }



    public static void showRealmFilesNotification(@NonNull Context context) {
        showRealmNotification(context, RealmFilesActivity.class);
    }



    private static void showRealmNotification(@NonNull Context context, @NonNull Class activityClass) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.realm_browser_ic_rb)
                .setContentTitle(context.getString(R.string.realm_browser_title))
                .setContentText(context.getString(R.string.realm_browser_click_to_launch))
                .setAutoCancel(false);
        Intent notifyIntent = new Intent(context, activityClass);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(notifyPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }



    public List<Class<? extends RealmObject>> getRealmModelList() {
        return mRealmModelList;
    }



    @SafeVarargs
    public final void addRealmModel(Class<? extends RealmObject>... arr) {
        mRealmModelList.addAll(Arrays.asList(arr));
    }

    public RealmFactory getRealmFactory() {
        return realmFactory;
    }

    public void setRealmFactory(RealmFactory realmFactory) {
        this.realmFactory = realmFactory;
    }
}
