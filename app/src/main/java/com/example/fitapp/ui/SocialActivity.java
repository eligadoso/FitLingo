package com.example.fitapp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitapp.R;
import com.example.fitapp.data.FirebaseManager;
import com.example.fitapp.data.SocialRepository;
import com.example.fitapp.data.FriendRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.fitapp.notifications.Notifications;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SocialActivity extends BaseNavActivity {
  private final SocialRepository repo = new SocialRepository();
  private final FriendRepository friends = new FriendRepository();
  private final List<DocumentSnapshot> items = new ArrayList<>();
  private RecyclerView list;
  private RecyclerView friendsList;
  private final List<DocumentSnapshot> friendsData = new ArrayList<>();
  private boolean friendsOnly = false;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FirebaseManager.init(this);
    setContentView(R.layout.activity_social);
    list = findViewById(R.id.list_posts);
    list.setLayoutManager(new LinearLayoutManager(this));
    list.setAdapter(new PostsAdapter(items));
    friendsList = findViewById(R.id.list_friends);
    if (friendsList != null) {
      friendsList.setLayoutManager(new LinearLayoutManager(this));
      friendsList.setAdapter(new FriendsAdapter(friendsData));
    }
    findViewById(R.id.btn_add_friend).setOnClickListener(v -> onAddFriend());
    android.view.View addByNameBtn = findViewById(R.id.btn_add_friend_name);
    if (addByNameBtn != null) addByNameBtn.setOnClickListener(v -> onAddFriendByName());
    ((android.widget.CheckBox)findViewById(R.id.cb_friends_only)).setOnCheckedChangeListener((buttonView, isChecked) -> { friendsOnly = isChecked; load(); });
    loadFriends();
    load();
  }

  private void load() {
    if (!friendsOnly) {
      Query q = repo.listFeed();
      q.get().addOnSuccessListener(rs -> { items.clear(); items.addAll(rs.getDocuments()); list.getAdapter().notifyDataSetChanged(); });
    } else {
      friends.listFriendIds().addOnSuccessListener(ids -> {
        java.util.List<String> friendUids = new java.util.ArrayList<>();
        for (DocumentSnapshot d : ids) friendUids.add(String.valueOf(d.get("friendId")));
        if (friendUids.isEmpty()) { items.clear(); list.getAdapter().notifyDataSetChanged(); return; }
        repo.listFeed().whereIn("userId", friendUids).get().addOnSuccessListener(rs -> {
          items.clear(); items.addAll(rs.getDocuments()); list.getAdapter().notifyDataSetChanged();
        });
      });
    }
  }

  private void onAddFriend() {
    android.widget.EditText et = findViewById(R.id.input_friend_email);
    String email = et.getText().toString().trim();
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { et.setError("Correo inválido"); return; }
    friends.findUserByEmail(email).addOnSuccessListener(userDoc -> {
      if (userDoc == null) { et.setError("Usuario no encontrado"); return; }
      friends.addFriend(userDoc.getId());
      android.widget.Toast.makeText(this, "Amigo agregado", android.widget.Toast.LENGTH_SHORT).show();
      et.setText("");
      loadFriends();
      load();
    });
  }

  private void onAddFriendByName() {
    android.widget.EditText et = findViewById(R.id.input_friend_name);
    String name = et.getText().toString().trim();
    if (name.isEmpty()) { et.setError("Ingresa un nombre"); return; }
    friends.findUserByName(name).addOnSuccessListener(userDoc -> {
      if (userDoc == null) { et.setError("Usuario no encontrado"); return; }
      friends.addFriend(userDoc.getId());
      android.widget.Toast.makeText(this, "Amigo agregado", android.widget.Toast.LENGTH_SHORT).show();
      et.setText("");
      loadFriends();
      load();
    });
  }

  private void loadFriends() {
    if (friendsList == null) return;
    friends.listFriendIds().addOnSuccessListener(ids -> {
      friendsData.clear();
      if (ids.isEmpty()) { friendsList.getAdapter().notifyDataSetChanged(); return; }
      java.util.List<String> friendUids = new java.util.ArrayList<>();
      for (DocumentSnapshot d : ids) friendUids.add(String.valueOf(d.get("friendId")));
      friendUids.add(com.example.fitapp.data.FirebaseManager.auth().getCurrentUser().getUid());
      com.example.fitapp.data.FirebaseManager.db().collection("users").whereIn("uid", friendUids).get().addOnSuccessListener(rs -> {
        if (rs.isEmpty()) {
          final int[] pending = {friendUids.size()};
          for (String uid : friendUids) {
            com.example.fitapp.data.FirebaseManager.db().collection("users").document(uid).get().addOnSuccessListener(doc -> {
              if (doc.get("name") == null) {
                java.util.Map<String,Object> m = new java.util.HashMap<>();
                m.put("name", String.valueOf(doc.get("email"))); // fallback visual
                doc.getReference().set(m, com.google.firebase.firestore.SetOptions.merge());
              }
              friendsData.add(doc);
              if (--pending[0] == 0) friendsList.getAdapter().notifyDataSetChanged();
            });
          }
        } else {
          friendsData.addAll(rs.getDocuments());
          friendsList.getAdapter().notifyDataSetChanged();
        }
      });
    });
  }
}

class PostsAdapter extends RecyclerView.Adapter<PostViewHolder> {
  private final List<DocumentSnapshot> data;
  PostsAdapter(List<DocumentSnapshot> data) { this.data = data; }
  @NonNull @Override public PostViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
    android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
    return new PostViewHolder(v);
  }
  @Override public void onBindViewHolder(@NonNull PostViewHolder h, int i) {
    DocumentSnapshot d = data.get(i);
    String content = String.valueOf(d.get("content"));
    h.userName.setText(String.valueOf(d.get("userId")));
    h.content.setText(content);
    h.createdAt.setText(String.valueOf(d.get("createdAt")));
    h.like.setOnClickListener(v -> {
      new SocialRepository().like(d.getId());
      Notifications.notify(h.itemView.getContext(), "Nueva interacción", "Le dieron me gusta a una publicación");
    });
    h.commentsToggle.setOnClickListener(v -> {
      h.commentsSection.setVisibility(h.commentsSection.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    });
    h.sendComment.setOnClickListener(v -> {
      // Placeholder UI action
      h.commentsList.addView(new TextView(h.itemView.getContext()));
    });
  }
  @Override public int getItemCount() { return data.size(); }
}

class PostViewHolder extends RecyclerView.ViewHolder {
  TextView userName, content, createdAt;
  android.widget.Button like, commentsToggle, sendComment;
  LinearLayout commentsSection, commentsList;
  PostViewHolder(@NonNull android.view.View itemView) {
    super(itemView);
    userName = itemView.findViewById(R.id.post_user_name);
    content = itemView.findViewById(R.id.post_content);
    createdAt = itemView.findViewById(R.id.post_created_at);
    like = itemView.findViewById(R.id.btn_like);
    commentsToggle = itemView.findViewById(R.id.btn_comments);
    sendComment = itemView.findViewById(R.id.btn_send_comment);
    commentsSection = itemView.findViewById(R.id.comments_section);
    commentsList = itemView.findViewById(R.id.comments_list);
  }
}

class FriendsAdapter extends RecyclerView.Adapter<FriendVH> {
  private final List<DocumentSnapshot> data;
  FriendsAdapter(List<DocumentSnapshot> data) { this.data = data; }
  @NonNull @Override public FriendVH onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
    android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
    return new FriendVH(v);
  }
  @Override public void onBindViewHolder(@NonNull FriendVH h, int i) {
    DocumentSnapshot d = data.get(i);
    h.name.setText(String.valueOf(d.get("name")));
    h.view.setOnClickListener(v -> {
      android.content.Intent intent = new android.content.Intent(v.getContext(), UserProfileActivity.class);
      intent.putExtra("userId", d.getId());
      v.getContext().startActivity(intent);
    });
  }
  @Override public int getItemCount() { return data.size(); }
}

class FriendVH extends RecyclerView.ViewHolder {
  android.widget.TextView name;
  android.widget.Button view;
  FriendVH(@NonNull android.view.View itemView) {
    super(itemView);
    name = itemView.findViewById(R.id.friend_name);
    view = itemView.findViewById(R.id.btn_view_profile);
  }
}
