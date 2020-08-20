package com.example.uprightchallenge;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import static android.content.Context.MODE_PRIVATE;

//receive periodically notifications pending intents
public class AlarmReceiver extends BroadcastReceiver {

    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID = 0;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private static final String LOG_TAG = AlarmReceiver.class.getSimpleName();


    @Override
    public void onReceive(Context context, Intent intent) {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        deliverNotification(context);
        Log.d(LOG_TAG, "notification fired!");
    }

    private void deliverNotification(Context context) {
        //this intent causes that when the notification is clicked, the MainActivity is launched
        Intent notifClickIntent = new Intent(context, MainActivity.class);
        PendingIntent notifClickPendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                notifClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // after you click action button on notification, the intent will be sent
        Intent postureYesIntent = new Intent(MainActivity.POSTURE_YES_ACTION);
        PendingIntent yesPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, postureYesIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
                .setContentTitle("Are you straighten up?")
                .setSmallIcon(R.drawable.ic_notify)
                .setAutoCancel(true) // closes the notification when user taps on it
                .setContentIntent(notifClickPendingIntent)
                .addAction(R.drawable.ic_posture_yes, "yes", yesPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }


}
