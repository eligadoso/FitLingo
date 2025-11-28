package com.example.fitlingo.ui;

import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitlingo.data.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;

public class BaseNavActivity extends AppCompatActivity {
  public void onNavDashboard(View v) { startActivity(new Intent(this, DashboardActivity.class)); }
  public void onNavWorkouts(View v) { startActivity(new Intent(this, WorkoutsActivity.class)); }
  public void onNavAchievements(View v) { startActivity(new Intent(this, AchievementsActivity.class)); }
  public void onNavSocial(View v) { startActivity(new Intent(this, SocialActivity.class)); }
  public void onNavProfile(View v) { startActivity(new Intent(this, ProfileActivity.class)); }
  public void onLogoutClick(View v) {
    FirebaseManager.auth().signOut();
    startActivity(new Intent(this, LoginActivity.class));
    finish();
  }
}
