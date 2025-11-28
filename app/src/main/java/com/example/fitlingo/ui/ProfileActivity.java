package com.example.fitlingo.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.fitlingo.R;
import com.example.fitlingo.data.FirebaseManager;
import com.example.fitlingo.data.UserRepository;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfileActivity extends BaseNavActivity {
  private final UserRepository repo = new UserRepository();
  private java.util.List<com.google.firebase.firestore.DocumentSnapshot> goals = new java.util.ArrayList<>();
  private com.google.firebase.firestore.ListenerRegistration goalsReg;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FirebaseManager.init(this);
    setContentView(R.layout.activity_profile);
    loadProfile();
    loadGoals();
    setupGoalSpinners();
  }

  private void loadProfile() {
    repo.getProfile().addOnSuccessListener(d -> {
      String name = d.getString("name");
      String email = com.example.fitlingo.data.FirebaseManager.auth().getCurrentUser().getEmail();
      ((TextView)findViewById(R.id.profile_name)).setText(name != null ? name : "");
      ((TextView)findViewById(R.id.profile_email)).setText(email != null ? email : "");
      if (name != null && name.length() > 0) ((TextView)findViewById(R.id.profile_avatar_initial)).setText(name.substring(0,1).toUpperCase());
      Number age = (Number) d.get("age"); Number weight = (Number) d.get("weight"); Number height = (Number) d.get("height");
      if (age != null) ((android.widget.EditText)findViewById(R.id.input_age)).setText(String.valueOf(age.intValue()));
      if (weight != null) ((android.widget.EditText)findViewById(R.id.input_weight)).setText(String.valueOf(weight.doubleValue()));
      if (height != null) ((android.widget.EditText)findViewById(R.id.input_height)).setText(String.valueOf(height.doubleValue()));
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
    android.widget.Toast.makeText(this, "Perfil guardado", android.widget.Toast.LENGTH_SHORT).show();
    loadProfile();
  }

  public void onToggleGoalForm(View v) { findViewById(R.id.goal_form).setVisibility(View.VISIBLE); }
  public void onCancelGoal(View v) { findViewById(R.id.goal_form).setVisibility(View.GONE); }
  public void onAddGoal(View v) {
    android.widget.Spinner exercise = findViewById(R.id.input_goal_exercise);
    android.widget.Spinner type = findViewById(R.id.input_goal_type);
    android.widget.EditText target = findViewById(R.id.input_goal_target);
    String ex = String.valueOf(exercise.getSelectedItem());
    String t = String.valueOf(type.getSelectedItem());
    int tar = 0; try { tar = Integer.parseInt(target.getText().toString()); } catch (Exception ignored) {}
    repo.addGoal(t, tar, ex);
    findViewById(R.id.goal_form).setVisibility(View.GONE);
  }
  private void loadGoals() {
    if (goalsReg != null) goalsReg.remove();
    goalsReg = com.example.fitlingo.data.FirebaseManager.db().collection("goals")
      .whereEqualTo("userId", com.example.fitlingo.data.FirebaseManager.auth().getCurrentUser().getUid())
      .addSnapshotListener((rs, ex) -> {
        if (rs == null) return;
        goals.clear(); goals.addAll(rs.getDocuments());
        android.widget.LinearLayout list = findViewById(R.id.list_goals);
        list.removeAllViews();
        for (com.google.firebase.firestore.DocumentSnapshot d : goals) {
          android.view.View v = android.view.LayoutInflater.from(this).inflate(R.layout.item_goal, list, false);
          String type = String.valueOf(d.get("type"));
          String exType = String.valueOf(d.get("exerciseType"));
          if (type == null || type.equals("null")) type = "Meta";
          int target = ((Number)d.get("target")).intValue();
          int progress = ((Number)d.get("progress")).intValue();
          String title = type + (exType != null && !"null".equals(exType) ? " · " + exType : "") + " ("+progress+"/"+target+")";
          ((android.widget.TextView)v.findViewById(R.id.goal_title)).setText(title);
          android.widget.ProgressBar pb = v.findViewById(R.id.goal_progress);
          pb.setMax(target);
          pb.setProgress(progress);
          ((android.widget.TextView)v.findViewById(R.id.goal_desc)).setText("Seguimiento visual de tu meta");
          list.addView(v);
        }
      });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (goalsReg != null) { goalsReg.remove(); goalsReg = null; }
  }
  private void setupGoalSpinners() {
    android.widget.Spinner exercise = findViewById(R.id.input_goal_exercise);
    android.widget.ArrayAdapter<CharSequence> exAdapter = android.widget.ArrayAdapter.createFromResource(this, R.array.workout_types, android.R.layout.simple_spinner_item);
    exAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    exercise.setAdapter(exAdapter);

    android.widget.Spinner type = findViewById(R.id.input_goal_type);
    java.util.function.Consumer<String> updateTypes = (ex) -> {
      java.util.List<String> items = new java.util.ArrayList<>();
      items.add("Sesiones por semana");
      items.add("Minutos por semana");
      if ("Correr".equalsIgnoreCase(ex) || "Bicicleta".equalsIgnoreCase(ex) || "Caminata".equalsIgnoreCase(ex)) {
        items.add("Kilómetros por mes");
      }
      android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      type.setAdapter(adapter);
    };
    exercise.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) { String ex = String.valueOf(exercise.getSelectedItem()); updateTypes.accept(ex); }
      @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
    });
    updateTypes.accept(String.valueOf(exercise.getSelectedItem()));
  }
}
