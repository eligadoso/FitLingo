package com.example.fitlingo.data;

import androidx.annotation.NonNull;

import com.example.fitlingo.model.Workout;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class WorkoutRepository {
  private final FirebaseFirestore db;
  private final FirebaseAuth auth;
  private final CollectionReference workouts;

  public WorkoutRepository() {
    this.db = FirebaseManager.db();
    this.auth = FirebaseManager.auth();
    this.workouts = db.collection("workouts");
  }

  public Query listMine() {
    String uid = auth.getCurrentUser().getUid();
    return workouts.whereEqualTo("userId", uid).orderBy("date", Query.Direction.DESCENDING);
  }

  public Query listByDate(String isoDate) {
    String uid = auth.getCurrentUser().getUid();
    return workouts.whereEqualTo("userId", uid).whereEqualTo("date", isoDate);
  }

  public Query listByDateAndType(String isoDate, String type) {
    String uid = auth.getCurrentUser().getUid();
    return workouts.whereEqualTo("userId", uid).whereEqualTo("date", isoDate).whereEqualTo("type", type);
  }

  public Task<DocumentReference> add(@NonNull Workout w) {
    String uid = auth.getCurrentUser().getUid();
    Map<String, Object> m = new HashMap<>();
    m.put("userId", uid);
    m.put("type", w.type);
    m.put("date", w.date);
    m.put("durationMinutes", w.durationMinutes);
    m.put("distanceKm", w.distanceKm);
    m.put("intensity", w.intensity);
    m.put("location", w.location);
    m.put("notes", w.notes);
    return workouts.add(m);
  }

  public Task<Void> update(String id, @NonNull Workout w) {
    Map<String, Object> m = new HashMap<>();
    m.put("type", w.type);
    m.put("date", w.date);
    m.put("durationMinutes", w.durationMinutes);
    m.put("distanceKm", w.distanceKm);
    m.put("intensity", w.intensity);
    m.put("location", w.location);
    m.put("notes", w.notes);
    return workouts.document(id).update(m);
  }

  public Task<Void> delete(String id) {
    return workouts.document(id).delete();
  }
}
