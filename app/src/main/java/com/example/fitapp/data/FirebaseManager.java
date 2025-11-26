package com.example.fitapp.data;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseManager {
  private static FirebaseAuth auth;
  private static FirebaseFirestore db;

  public static void init(Context context) {
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context);
    }
    if (auth == null) auth = FirebaseAuth.getInstance();
    if (db == null) db = FirebaseFirestore.getInstance();
  }

  public static FirebaseAuth auth() {
    return auth;
  }

  public static FirebaseFirestore db() {
    return db;
  }
}
