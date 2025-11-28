package com.example.fitlingo.data;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseManager {
  private static Context appCtx;
  private static FirebaseAuth auth;
  private static FirebaseFirestore db;

  public static void init(Context context) {
    appCtx = context.getApplicationContext();
    if (FirebaseApp.getApps(appCtx).isEmpty()) {
      FirebaseApp.initializeApp(appCtx);
    }
    auth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
  }

  public static FirebaseAuth auth() {
    if (auth == null) {
      if (appCtx != null) init(appCtx);
      else auth = FirebaseAuth.getInstance();
    }
    return auth;
  }

  public static FirebaseFirestore db() {
    if (db == null) {
      if (appCtx != null) init(appCtx);
      else db = FirebaseFirestore.getInstance();
    }
    return db;
  }
}
