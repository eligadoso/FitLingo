package com.example.fitapp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitapp.R;
import com.example.fitapp.data.AchievementService;
import com.example.fitapp.data.FirebaseManager;
import com.example.fitapp.data.SocialRepository;
import com.example.fitapp.data.WorkoutRepository;
import com.example.fitapp.model.Workout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class WorkoutsActivity extends BaseNavActivity {
  private WorkoutRepository repo;
  private SocialRepository social;
  private AchievementService achievementService;
  private final List<DocumentSnapshot> items = new ArrayList<>();
  private RecyclerView list;
  private View form;
  private String editingId = null;
  private java.util.Calendar selected = java.util.Calendar.getInstance();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FirebaseManager.init(this);
    setContentView(R.layout.activity_workouts);
    repo = new WorkoutRepository();
    social = new SocialRepository();
    achievementService = new AchievementService(this);
    list = findViewById(R.id.list_workouts);
    list.setLayoutManager(new LinearLayoutManager(this));
    list.setAdapter(new WorkoutsAdapter(items, new ItemListener()));
    form = findViewById(R.id.workout_form_container);
    setupSpinners();
    updateSelectedDateLabel();
    setupDatePickers();
    loadWorkoutsForSelectedDay();
  }

  private void loadWorkouts() {
    Query q = repo.listMine();
    q.get().addOnSuccessListener(rs -> {
      items.clear();
      items.addAll(rs.getDocuments());
      list.getAdapter().notifyDataSetChanged();
    });
  }

  private void loadWorkoutsForSelectedDay() {
    String iso = com.example.fitapp.util.DateUtils.toIso(selected.getTime());
    repo.listByDate(iso).get().addOnSuccessListener(rs -> {
      items.clear(); items.addAll(rs.getDocuments()); list.getAdapter().notifyDataSetChanged();
    });
  }

  public void onNewWorkout(View v) {
    editingId = null;
    ((android.widget.TextView)findViewById(R.id.workout_form_title)).setText("Nuevo Entrenamiento");
    clearForm();
    form.setVisibility(View.VISIBLE);
  }

  public void onCloseWorkoutForm(View v) { form.setVisibility(View.GONE); }
  public void onCancelWorkout(View v) { form.setVisibility(View.GONE); }

  public void onSaveWorkout(View v) {
    Spinner type = findViewById(R.id.input_type);
    EditText date = findViewById(R.id.input_date);
    EditText duration = findViewById(R.id.input_duration);
    EditText distance = findViewById(R.id.input_distance);
    Spinner intensity = findViewById(R.id.input_intensity);
    EditText location = findViewById(R.id.input_location);
    EditText notes = findViewById(R.id.input_notes);
    if (date.getText().toString().isEmpty()) { date.setError("Selecciona una fecha"); return; }
    if (!validatePositive(duration, "Duración")) return;
    if (!validatePositiveDecimal(distance, "Distancia")) return;
    Workout w = new Workout();
    w.type = String.valueOf(type.getSelectedItem());
    try {
      java.util.Date parsed = com.example.fitapp.util.DateUtils.parseUi(date.getText().toString());
      w.date = com.example.fitapp.util.DateUtils.toIso(parsed);
    } catch (java.text.ParseException e) { date.setError("Fecha inválida"); return; }
    try { w.durationMinutes = Integer.parseInt(duration.getText().toString()); } catch (Exception ignored) {}
    try { w.distanceKm = Double.parseDouble(distance.getText().toString()); } catch (Exception ignored) {}
    w.intensity = String.valueOf(intensity.getSelectedItem());
    w.location = location.getText().toString();
    w.notes = notes.getText().toString();
    if (editingId == null) {
      repo.add(w).addOnSuccessListener(d -> {
        Toast.makeText(this, "Entrenamiento guardado", Toast.LENGTH_SHORT).show();
        form.setVisibility(View.GONE);
        loadWorkoutsForSelectedDay();
        achievementService.evaluateAndStoreAchievements();
      }).addOnFailureListener(e -> Toast.makeText(this, String.valueOf(e), Toast.LENGTH_LONG).show());
    } else {
      repo.update(editingId, w).addOnSuccessListener(d -> {
        Toast.makeText(this, "Entrenamiento actualizado", Toast.LENGTH_SHORT).show();
        form.setVisibility(View.GONE);
        loadWorkoutsForSelectedDay();
      }).addOnFailureListener(e -> Toast.makeText(this, String.valueOf(e), Toast.LENGTH_LONG).show());
    }
  }

  private class ItemListener implements WorkoutsAdapter.Listener {
    @Override public void onShare(@NonNull DocumentSnapshot d) {
      String type = String.valueOf(d.get("type"));
      String date = String.valueOf(d.get("date"));
      String content = "Entrenamiento " + type + " el " + date;
      social.shareWorkout(content);
      Toast.makeText(WorkoutsActivity.this, "Compartido", Toast.LENGTH_SHORT).show();
    }
    @Override public void onEdit(@NonNull DocumentSnapshot d) {
      editingId = d.getId();
      ((android.widget.TextView)findViewById(R.id.workout_form_title)).setText("Editar Entrenamiento");
      setFormFromDoc(d);
      form.setVisibility(View.VISIBLE);
    }
    @Override public void onDelete(@NonNull DocumentSnapshot d) {
      repo.delete(d.getId()).addOnSuccessListener(v -> { loadWorkoutsForSelectedDay(); });
    }
  }

  private void setupSpinners() {
    android.widget.ArrayAdapter<CharSequence> types = android.widget.ArrayAdapter.createFromResource(this, R.array.workout_types, android.R.layout.simple_spinner_item);
    types.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    ((android.widget.Spinner)findViewById(R.id.input_type)).setAdapter(types);
    android.widget.ArrayAdapter<CharSequence> intens = android.widget.ArrayAdapter.createFromResource(this, R.array.intensities, android.R.layout.simple_spinner_item);
    intens.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    ((android.widget.Spinner)findViewById(R.id.input_intensity)).setAdapter(intens);
  }

  private void setupDatePickers() {
    android.widget.EditText start = findViewById(R.id.filter_start);
    android.widget.EditText end = findViewById(R.id.filter_end);
    android.widget.EditText date = findViewById(R.id.input_date);
    start.setOnClickListener(v -> pickDate(d -> start.setText(com.example.fitapp.util.DateUtils.toUi(d))));
    end.setOnClickListener(v -> pickDate(d -> {
      start.setError(null);
      java.util.Date s = null; try { s = com.example.fitapp.util.DateUtils.parseUi(start.getText().toString()); } catch (Exception ignored) {}
      if (s != null && d.before(s)) { end.setError("Debe ser posterior a inicio"); } else { end.setError(null); end.setText(com.example.fitapp.util.DateUtils.toUi(d)); }
    }));
    date.setOnClickListener(v -> pickDate(d -> date.setText(com.example.fitapp.util.DateUtils.toUi(d))));
  }

  public void onPrevDay(View v) { selected.add(java.util.Calendar.DAY_OF_MONTH, -1); updateSelectedDateLabel(); loadWorkoutsForSelectedDay(); }
  public void onNextDay(View v) { selected.add(java.util.Calendar.DAY_OF_MONTH, 1); updateSelectedDateLabel(); loadWorkoutsForSelectedDay(); }
  private void updateSelectedDateLabel() {
    ((android.widget.TextView)findViewById(R.id.selected_date_label)).setText(com.example.fitapp.util.DateUtils.toUi(selected.getTime()));
  }

  private void pickDate(java.util.function.Consumer<java.util.Date> consumer) {
    java.util.Calendar c = java.util.Calendar.getInstance();
    new android.app.DatePickerDialog(this, (view, y, m, d) -> {
      java.util.Calendar sel = java.util.Calendar.getInstance(); sel.set(y, m, d);
      consumer.accept(sel.getTime());
    }, c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.DAY_OF_MONTH)).show();
  }

  private boolean validatePositive(EditText et, String label) {
    try { int v = Integer.parseInt(et.getText().toString()); if (v < 0) throw new NumberFormatException(); et.setError(null); return true; } catch (Exception e) { et.setError(label+" debe ser positivo"); return false; }
  }
  private boolean validatePositiveDecimal(EditText et, String label) {
    try { double v = Double.parseDouble(et.getText().toString()); if (v < 0) throw new NumberFormatException(); et.setError(null); return true; } catch (Exception e) { et.setError(label+" debe ser positivo"); return false; }
  }

  private void clearForm() {
    ((android.widget.EditText)findViewById(R.id.input_date)).setText("");
    ((android.widget.EditText)findViewById(R.id.input_duration)).setText("");
    ((android.widget.EditText)findViewById(R.id.input_distance)).setText("");
    ((android.widget.EditText)findViewById(R.id.input_location)).setText("");
    ((android.widget.EditText)findViewById(R.id.input_notes)).setText("");
  }

  private void setFormFromDoc(@NonNull com.google.firebase.firestore.DocumentSnapshot d) {
    setSpinnerSelection((android.widget.Spinner)findViewById(R.id.input_type), String.valueOf(d.get("type")));
    ((android.widget.EditText)findViewById(R.id.input_date)).setText(String.valueOf(d.get("date")));
    ((android.widget.EditText)findViewById(R.id.input_duration)).setText(String.valueOf(d.get("durationMinutes")));
    ((android.widget.EditText)findViewById(R.id.input_distance)).setText(String.valueOf(d.get("distanceKm")));
    setSpinnerSelection((android.widget.Spinner)findViewById(R.id.input_intensity), String.valueOf(d.get("intensity")));
    ((android.widget.EditText)findViewById(R.id.input_location)).setText(String.valueOf(d.get("location")));
    ((android.widget.EditText)findViewById(R.id.input_notes)).setText(String.valueOf(d.get("notes")));
  }

  private void setSpinnerSelection(android.widget.Spinner spinner, String value) {
    android.widget.ArrayAdapter adapter = (android.widget.ArrayAdapter) spinner.getAdapter();
    if (adapter == null) return;
    for (int i = 0; i < adapter.getCount(); i++) {
      Object item = adapter.getItem(i);
      if (item != null && String.valueOf(item).equalsIgnoreCase(value)) { spinner.setSelection(i); return; }
    }
  }
}

class WorkoutsAdapter extends RecyclerView.Adapter<WorkoutsViewHolder> {
  interface Listener { void onShare(@NonNull DocumentSnapshot d); void onEdit(@NonNull DocumentSnapshot d); void onDelete(@NonNull DocumentSnapshot d); }
  private final List<DocumentSnapshot> data;
  private final Listener listener;
  WorkoutsAdapter(List<DocumentSnapshot> data, Listener listener) { this.data = data; this.listener = listener; }
  @NonNull @Override public WorkoutsViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
    android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
    return new WorkoutsViewHolder(v);
  }
  @Override public void onBindViewHolder(@NonNull WorkoutsViewHolder h, int i) {
    DocumentSnapshot d = data.get(i);
    h.type.setText(String.valueOf(d.get("type")));
    h.intensity.setText(String.valueOf(d.get("intensity")));
    h.date.setText(String.valueOf(d.get("date")));
    h.duration.setText("Minutos: " + String.valueOf(d.get("durationMinutes")));
    h.distance.setText("Km: " + String.valueOf(d.get("distanceKm")));
    h.notes.setText(String.valueOf(d.get("notes")));
    h.share.setOnClickListener(v -> listener.onShare(d));
    h.edit.setOnClickListener(v -> listener.onEdit(d));
    h.delete.setOnClickListener(v -> listener.onDelete(d));
  }
  @Override public int getItemCount() { return data.size(); }
}

class WorkoutsViewHolder extends RecyclerView.ViewHolder {
  android.widget.TextView type, intensity, date, duration, distance, notes;
  android.widget.Button share, edit, delete;
  WorkoutsViewHolder(@NonNull android.view.View itemView) {
    super(itemView);
    type = itemView.findViewById(R.id.workout_type);
    intensity = itemView.findViewById(R.id.workout_intensity);
    date = itemView.findViewById(R.id.workout_date);
    duration = itemView.findViewById(R.id.workout_duration);
    distance = itemView.findViewById(R.id.workout_distance);
    notes = itemView.findViewById(R.id.workout_notes);
    share = itemView.findViewById(R.id.btn_share);
    edit = itemView.findViewById(R.id.btn_edit);
    delete = itemView.findViewById(R.id.btn_delete);
  }
}
