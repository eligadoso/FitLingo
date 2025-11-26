package com.example.fitapp.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitapp.R;
import com.example.fitapp.data.FirebaseManager;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends BaseNavActivity {
  private String userId;
  private final List<DocumentSnapshot> achievements = new ArrayList<>();
  private RecyclerView list;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FirebaseManager.init(this);
    setContentView(R.layout.activity_user_profile);
    userId = getIntent().getStringExtra("userId");
    list = findViewById(R.id.list_user_achievements);
    list.setLayoutManager(new LinearLayoutManager(this));
    list.setAdapter(new AchAdapter(achievements));
    loadUser();
    loadAchievements();
  }

  private void loadUser() {
    FirebaseManager.db().collection("users").document(userId).get().addOnSuccessListener(d -> {
      ((TextView)findViewById(R.id.user_name)).setText(String.valueOf(d.get("name")));
      ((TextView)findViewById(R.id.user_email)).setText(String.valueOf(d.get("email")));
    });
  }
  private void loadAchievements() {
    FirebaseManager.db().collection("achievements").whereEqualTo("userId", userId).get().addOnSuccessListener(rs -> {
      achievements.clear(); achievements.addAll(rs.getDocuments()); list.getAdapter().notifyDataSetChanged();
    });
  }
}

class AchAdapter extends RecyclerView.Adapter<AchVH> {
  private final List<DocumentSnapshot> data;
  AchAdapter(List<DocumentSnapshot> data) { this.data = data; }
  @NonNull @Override public AchVH onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
    android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
    return new AchVH(v);
  }
  @Override public void onBindViewHolder(@NonNull AchVH h, int i) {
    DocumentSnapshot d = data.get(i);
    h.title.setText(String.valueOf(d.get("title")));
    h.desc.setText(String.valueOf(d.get("description")));
    h.date.setText(String.valueOf(d.get("date")));
  }
  @Override public int getItemCount() { return data.size(); }
}

class AchVH extends RecyclerView.ViewHolder {
  TextView title, desc, date;
  AchVH(@NonNull android.view.View itemView) { super(itemView); title = itemView.findViewById(R.id.achievement_title); desc = itemView.findViewById(R.id.achievement_desc); date = itemView.findViewById(R.id.achievement_date); }
}
