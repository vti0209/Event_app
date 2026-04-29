package org.o7planning.eventmanagementapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

public class ClockAlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "CLOCK_ALARM_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        String label = intent.getStringExtra("ALARM_LABEL");
        if (label == null) label = "Báo thức";

        showNotification(context, label);
        playAlarmSound(context);
    }

    private void showNotification(Context context, String label) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Báo thức", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Báo thức")
                .setContentText(label)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void playAlarmSound(Context context) {
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
            Ringtone r = RingtoneManager.getRingtone(context, alert);
            r.play();
            
            // Dừng chuông sau 10 giây
            new Handler(Looper.getMainLooper()).postDelayed(r::stop, 10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
