package com.example.fitlingo.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendRepository {
  private final FirebaseFirestore db;
  private final FirebaseAuth auth;

  public FriendRepository() {
    this.db = FirebaseManager.db();
    this.auth = FirebaseManager.auth();
  }

  public Task<DocumentSnapshot> findUserByEmail(String email) {
    return db.collection("users").whereEqualTo("email", email).limit(1).get().continueWith(t -> {
      if (t.getResult() != null && !t.getResult().isEmpty()) return t.getResult().getDocuments().get(0);
      return null;
    });
  }

  public Task<DocumentReference> addFriend(String friendUid) {
    Map<String,Object> m = new HashMap<>();
    m.put("userId", auth.getCurrentUser().getUid());
    m.put("friendId", friendUid);
    return db.collection("friends").add(m);
  }

  public Task<List<DocumentSnapshot>> listFriendIds() {
    return db.collection("friends").whereEqualTo("userId", auth.getCurrentUser().getUid()).get().continueWith(t -> t.getResult().getDocuments());
  }

  public Task<DocumentSnapshot> findUserByName(String name) {
    return db.collection("users").whereEqualTo("name", name).limit(1).get().continueWith(t -> {
      if (t.getResult() != null && !t.getResult().isEmpty()) return t.getResult().getDocuments().get(0);
      return null;
    });
  }
}
