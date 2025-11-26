package com.example.fitapp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.fitapp.R;
import com.example.fitapp.data.FirebaseManager;
import com.example.fitapp.data.UserRepository;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfileActivity extends BaseNavActivity {
  private final UserRepository repo = new UserRepository();
  private java.util.List<com.google.firebase.firestore.DocumentSnapshot> goals = new java.util.ArrayList<>();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FirebaseManager.init(this);
    setContentView(R.layout.activity_profile);
    loadProfile();
    loadGoals();
    setupGoalSpinner();
  }

  private void loadProfile() {
    repo.getProfile().addOnSuccessListener(d -> {
      String name = d.getString("name");
      String email = com.example.fitapp.data.FirebaseManager.auth().getCurrentUser().getEmail();
      ((TextView)findViewById(R.id.profile_name)).setText(name != null ? name : "");
      ((TextView)findViewById(R.id.profile_email)).setText(email != null ? email : "");
      if (name != null && name.length() > 0) ((TextView)findViewById(R.id.profile_avatar_initial)).setText(name.substring(0,1).toUpperCase());
    });
  }

  public void onEditProfile(View v) {
    findViewById(R.id.input_age).setVisibility(View.VISIBLE);
    findViewById(R.id.input_weight).setVisibility(View.VISIBLE);
    findViewById(R.id.input_height).setVisibility(View.VISIBLE);
  }
  public void onCancelProfile(View v) {
    findViewById(R.id.input_age).setVisibility(View.GONE);
    findViewById(R.id.input_weight).setVisibility(View.GONE);
    findViewById(R.id.input_height).setVisibility(View.GONE);
  }
  public void onSaveProfile(View v) {
    EditText age = findViewById(R.id.input_age);
    EditText weight = findViewById(R.id.input_weight);
    EditText height = findViewById(R.id.input_height);
    Integer a = null; Double w = null; Double h = null;
    try { a = Integer.parseInt(age.getText().toString()); } catch (Exception ignored) {}
    try { w = Double.parseDouble(weight.getText().toString()); } catch (Exception ignored) {}
    try { h = Double.parseDouble(height.getText().toString()); } catch (Exception ignored) {}
    repo.updateProfile(null, a, w, h);
    onCancelProfile(v);
  }

  public void onToggleGoalForm(View v) { findViewById(R.id.goal_form).setVisibility(View.VISIBLE); }
  public void onCancelGoal(View v) { findViewById(R.id.goal_form).setVisibility(View.GONE); }
  public void onAddGoal(View v) {
    android.widget.Spinner type = findViewById(R.id.input_goal_type);
    android.widget.EditText target = findViewById(R.id.input_goal_target);
    String t = String.valueOf(type.getSelectedItem());
    int tar = 0; try { tar = Integer.parseInt(target.getText().toString()); } catch (Exception ignored) {}
    repo.addGoal(t, tar);
    findViewById(R.id.goal_form).setVisibility(View.GONE);
  }
  private void loadGoals() {
    com.example.fitapp.data.FirebaseManager.db().collection("goals")
      .whereEqualTo("userId", com.example.fitapp.data.FirebaseManager.auth().getCurrentUser().getUid())
      .get().addOnSuccessListener(rs -> {
        goals.clear(); goals.addAll(rs.getDocuments());
        android.widget.LinearLayout list = findViewById(R.id.list_goals);
        list.removeAllViews();
        for (com.google.firebase.firestore.DocumentSnapshot d : goals) {
          android.view.View v = android.view.LayoutInflater.from(this).inflate(R.layout.item_goal, list, false);
          String type = String.valueOf(d.get("type"));
          if (type == null || type.equals("null")) type = "Meta";
          int target = ((Number)d.get("target")).intValue();
          int progress = ((Number)d.get("progress")).intValue();
          ((android.widget.TextView)v.findViewById(R.id.goal_title)).setText(type+" ("+progress+"/"+target+")");
          android.widget.ProgressBar pb = v.findViewById(R.id.goal_progress);
          pb.setMax(target);
          pb.setProgress(progress);
          ((android.widget.TextView)v.findViewById(R.id.goal_desc)).setText("Seguimiento visual de tu meta");
          list.addView(v);
        }
      });
  }
  private void setupGoalSpinner() {
    android.widget.Spinner type = findViewById(R.id.input_goal_type);
    android.widget.ArrayAdapter<CharSequence> adapter = android.widget.ArrayAdapter.createFromResource(this, R.array.goal_types, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    type.setAdapter(adapter);
  }
}
