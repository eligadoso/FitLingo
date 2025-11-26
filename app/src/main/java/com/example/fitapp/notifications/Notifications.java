package com.example.fitapp.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.fitapp.R;

public class Notifications {
  public static final String CHANNEL_ID = "fitapp_channel";

  public static void ensureChannel(Context ctx) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "FitApp", NotificationManager.IMPORTANCE_DEFAULT);
      channel.setDescription("Notificaciones de progreso y social");
      NotificationManager nm = ctx.getSystemService(NotificationManager.class);
      nm.createNotificationChannel(channel);
    }
  }

  public static void notify(Context ctx, String title, String text) {
    ensureChannel(ctx);
    NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle(title)
      .setContentText(text)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    NotificationManagerCompat.from(ctx).notify((int)System.currentTimeMillis(), b.build());
  }
}
