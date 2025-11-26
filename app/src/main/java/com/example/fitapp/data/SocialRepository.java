package com.example.fitapp.data;

import com.example.fitapp.model.Post;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class SocialRepository {
  private final FirebaseFirestore db;
  private final FirebaseAuth auth;
  private final CollectionReference posts;

  public SocialRepository() {
    this.db = FirebaseManager.db();
    this.auth = FirebaseManager.auth();
    this.posts = db.collection("posts");
  }

  public Query listFeed() {
    return posts.orderBy("createdAt", Query.Direction.DESCENDING).limit(100);
  }

  public Task<DocumentReference> shareWorkout(String content) {
    String uid = auth.getCurrentUser().getUid();
    Map<String, Object> m = new HashMap<>();
    m.put("userId", uid);
    m.put("content", content);
    m.put("createdAt", System.currentTimeMillis());
    m.put("likeCount", 0);
    return posts.add(m);
  }

  public Task<Void> like(String postId) {
    return posts.document(postId).update("likeCount", com.google.firebase.firestore.FieldValue.increment(1));
  }
}
