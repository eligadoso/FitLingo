package com.example.fitlingo.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlingo.R;
import com.example.fitlingo.data.AchievementService;
import com.example.fitlingo.data.FirebaseManager;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AchievementsActivity extends BaseNavActivity {
  private AchievementService service;
  private final List<DocumentSnapshot> items = new ArrayList<>();
  private RecyclerView list;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FirebaseManager.init(this);
    setContentView(R.layout.activity_achievements);
    service = new AchievementService(this);
    list = findViewById(R.id.list_achievements);
    list.setLayoutManager(new LinearLayoutManager(this));
    list.setAdapter(new AchievementsAdapter(items));
    load();
  }

  private void load() {
    service.listMine().addOnSuccessListener(rs -> {
      items.clear();
      items.addAll(rs.getDocuments());
      list.getAdapter().notifyDataSetChanged();
    });
  }
}

class AchievementsAdapter extends RecyclerView.Adapter<AchievementsViewHolder> {
  private final List<DocumentSnapshot> data;
  AchievementsAdapter(List<DocumentSnapshot> data) { this.data = data; }
  @NonNull @Override public AchievementsViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
    android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
    return new AchievementsViewHolder(v);
  }
  @Override public void onBindViewHolder(@NonNull AchievementsViewHolder h, int i) {
    DocumentSnapshot d = data.get(i);
    h.title.setText(String.valueOf(d.get("title")));
    h.desc.setText(String.valueOf(d.get("description")));
    h.date.setText(String.valueOf(d.get("date")));
    h.share.setOnClickListener(v -> {
      android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
      intent.setType("text/plain");
      String text = h.title.getText()+" — "+h.desc.getText();
      intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
      v.getContext().startActivity(android.content.Intent.createChooser(intent, "Compartir logro"));
    });
  }
  @Override public int getItemCount() { return data.size(); }
}

class AchievementsViewHolder extends RecyclerView.ViewHolder {
  android.widget.TextView title, desc, date;
  android.widget.Button share;
  AchievementsViewHolder(@NonNull android.view.View itemView) {
    super(itemView);
    title = itemView.findViewById(R.id.achievement_title);
    desc = itemView.findViewById(R.id.achievement_desc);
    date = itemView.findViewById(R.id.achievement_date);
    share = itemView.findViewById(R.id.btn_share_achievement);
  }
}
