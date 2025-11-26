package com.example.fitapp.ui;

import android.os.Bundle;
import android.widget.TextView;

import com.example.fitapp.R;
import com.example.fitapp.data.AchievementService;
import com.example.fitapp.data.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

public class DashboardActivity extends BaseNavActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FirebaseManager.init(this);
    if (FirebaseManager.auth().getCurrentUser() == null) {
      finish();
      return;
    }
    setContentView(R.layout.activity_dashboard);
    loadStats();
  }

  private void loadStats() {
    FirebaseManager.db().collection("workouts")
      .whereEqualTo("userId", FirebaseManager.auth().getCurrentUser().getUid())
      .get()
      .addOnSuccessListener(rs -> {
        int total = rs.size();
        int minutes = 0;
        double kms = 0.0;
        int[] weekly = new int[7];
        java.text.SimpleDateFormat iso = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.util.Calendar now = java.util.Calendar.getInstance();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        for (DocumentSnapshot d : rs.getDocuments()) {
          Number m = (Number) d.get("durationMinutes");
          Number k = (Number) d.get("distanceKm");
          if (m != null) minutes += m.intValue();
          if (k != null) kms += k.doubleValue();
          try {
            String ds = String.valueOf(d.get("date"));
            java.util.Date dt = iso.parse(ds);
            cal.setTime(dt);
            long diffDays = (now.getTimeInMillis() - cal.getTimeInMillis()) / (1000*60*60*24);
            if (diffDays >= 0 && diffDays < 7) {
              int idx = (int)(6 - diffDays);
              weekly[idx] += m != null ? m.intValue() : 0;
            }
          } catch (Exception ignored) {}
        }
        ((TextView)findViewById(R.id.stat_total_workouts)).setText(String.valueOf(total));
        ((TextView)findViewById(R.id.stat_total_minutes)).setText(String.valueOf(minutes));
        ((TextView)findViewById(R.id.stat_total_distance)).setText(String.format("%.1f", kms));
        int thisWeek = 0; for (int v : weekly) thisWeek += v;
        TextView weekTv = findViewById(R.id.stat_this_week_small);
        if (weekTv != null) weekTv.setText("Esta semana: " + thisWeek + " min");
        int weeksCount = 1;
        try {
          java.util.Date firstDate = rs.isEmpty() ? new java.util.Date() : iso.parse(String.valueOf(rs.getDocuments().get(rs.size()-1).get("date")));
          long days = (now.getTimeInMillis() - firstDate.getTime())/(1000*60*60*24);
          weeksCount = (int)Math.max(1, Math.ceil(days/7.0));
        } catch (Exception ignored) {}
        TextView avgTv = findViewById(R.id.stat_avg_per_week_small);
        if (avgTv != null) avgTv.setText("Promedio semanal: " + (minutes/weeksCount) + " min");
        renderCharts(rs, weekly);
      });

    new AchievementService(this).listMine().addOnSuccessListener(a -> {
      ((TextView)findViewById(R.id.stat_achievements_count)).setText(String.valueOf(a.size()));
    });
  }

  private void renderCharts(com.google.firebase.firestore.QuerySnapshot rs, int[] weekly) {
    java.util.Map<String, Integer> byType = new java.util.HashMap<>();
    for (DocumentSnapshot d : rs.getDocuments()) {
      Number m = (Number) d.get("durationMinutes");
      String type = String.valueOf(d.get("type"));
      byType.put(type, (byType.getOrDefault(type, 0) + (m != null ? m.intValue() : 0)));
    }

    java.util.List<BarEntry> bars = new java.util.ArrayList<>();
    for (int i = 0; i < weekly.length; i++) bars.add(new BarEntry(i, weekly[i]));
    BarDataSet bds = new BarDataSet(bars, "Minutos por día (últ. semana)");
    BarData bd = new BarData(bds);
    BarChart barChart = new BarChart(this);
    barChart.setData(bd);
    Description desc = new Description(); desc.setText(""); barChart.setDescription(desc);
    barChart.getAxisLeft().setEnabled(false); barChart.getAxisRight().setEnabled(false);
    barChart.getXAxis().setEnabled(false);
    barChart.getLegend().setEnabled(false);
    barChart.setTouchEnabled(false);
    android.widget.FrameLayout weeklyContainer = findViewById(R.id.chart_weekly_activity);
    weeklyContainer.removeAllViews();
    weeklyContainer.addView(barChart);
    barChart.invalidate();

    java.util.List<PieEntry> pies = new java.util.ArrayList<>();
    for (java.util.Map.Entry<String,Integer> e : byType.entrySet()) pies.add(new PieEntry(e.getValue(), e.getKey()));
    PieDataSet pds = new PieDataSet(pies, "Minutos por actividad");
    PieData pd = new PieData(pds);
    PieChart pieChart = new PieChart(this);
    pieChart.setData(pd);
    pieChart.setDescription(desc);
    pieChart.getLegend().setEnabled(false);
    pieChart.setTouchEnabled(false);
    android.widget.FrameLayout typeContainer = findViewById(R.id.chart_activity_types);
    typeContainer.removeAllViews();
    typeContainer.addView(pieChart);
    pieChart.invalidate();
  }
}
