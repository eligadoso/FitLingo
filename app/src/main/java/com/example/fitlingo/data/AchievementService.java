package com.example.fitlingo.data;

import android.content.Context;

import com.example.fitlingo.notifications.Notifications;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AchievementService {
  private final FirebaseFirestore db;
  private final FirebaseAuth auth;
  private final Context ctx;

  public AchievementService(Context ctx) {
    this.db = FirebaseManager.db();
    this.auth = FirebaseManager.auth();
    this.ctx = ctx;
  }

  public Task<QuerySnapshot> listMine() {
    return db.collection("achievements").whereEqualTo("userId", auth.getCurrentUser().getUid()).get();
  }

  public void evaluateAndStoreAchievements() {
    String uid = auth.getCurrentUser().getUid();
    db.collection("workouts").whereEqualTo("userId", uid).get().addOnSuccessListener(ws -> {
      int count = ws.size();
      double totalKm = 0.0;
      for (DocumentSnapshot d : ws.getDocuments()) {
        Number n = (Number) d.get("distanceKm");
        if (n != null) totalKm += n.doubleValue();
      }
      if (count >= 1) ensureAchievement(uid, "Primer entrenamiento", "Has registrado tu primer entrenamiento");
      if (count >= 10) ensureAchievement(uid, "10 entrenamientos", "Has completado 10 entrenamientos");
      if (totalKm >= 5.0) ensureAchievement(uid, "Primer 5 km", "Has alcanzado 5 km acumulados");
    });
  }

  private void ensureAchievement(String uid, String title, String description) {
    db.collection("achievements")
      .whereEqualTo("userId", uid)
      .whereEqualTo("title", title)
      .get()
      .addOnSuccessListener(r -> {
        if (r.isEmpty()) {
          Map<String, Object> m = new HashMap<>();
          m.put("userId", uid);
          m.put("title", title);
          m.put("description", description);
          m.put("date", String.valueOf(System.currentTimeMillis()));
          db.collection("achievements").add(m).addOnSuccessListener(x -> {
            Notifications.notify(ctx, "Nuevo logro", title);
          });
        }
      });
  }
}
