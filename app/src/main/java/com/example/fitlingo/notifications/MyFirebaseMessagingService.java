package com.example.fitlingo.notifications;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
  @Override
  public void onMessageReceived(RemoteMessage message) {
    String title = message.getNotification() != null ? message.getNotification().getTitle() : "Nueva notificación";
    String body = message.getNotification() != null ? message.getNotification().getBody() : "Tienes una actualización";
    Notifications.notify(getApplicationContext(), title, body);
  }

  @Override
  public void onNewToken(String token) {
    // Token actualizado, listo para registrar en servidor si aplica
  }
}
