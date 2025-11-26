package com.example.fitapp.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
  private final FirebaseFirestore db;
  private final FirebaseAuth auth;

  public UserRepository() {
    this.db = FirebaseManager.db();
    this.auth = FirebaseManager.auth();
  }

  public Task<DocumentSnapshot> getProfile() {
    return db.collection("users").document(auth.getCurrentUser().getUid()).get();
  }

  public Task<Void> updateProfile(String name, Integer age, Double weight, Double height) {
    Map<String, Object> m = new HashMap<>();
    if (name != null) m.put("name", name);
    if (age != null) m.put("age", age);
    if (weight != null) m.put("weight", weight);
    if (height != null) m.put("height", height);
    return db.collection("users").document(auth.getCurrentUser().getUid()).set(m, com.google.firebase.firestore.SetOptions.merge());
  }

  public Task<DocumentReference> addGoal(String type, int target) {
    Map<String, Object> m = new HashMap<>();
    m.put("userId", auth.getCurrentUser().getUid());
    m.put("type", type);
    m.put("target", target);
    m.put("progress", 0);
    return db.collection("goals").add(m);
  }
}
