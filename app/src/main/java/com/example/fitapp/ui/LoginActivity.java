package com.example.fitapp.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitapp.R;
import com.example.fitapp.data.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
  private boolean signupMode = false;
  private FirebaseAuth auth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FirebaseManager.init(this);
    auth = FirebaseManager.auth();
    if (auth.getCurrentUser() != null) {
      startActivity(new Intent(this, DashboardActivity.class));
      finish();
      return;
    }
    setContentView(R.layout.activity_login);
  }

  public void onToggleLoginMode(View v) {
    signupMode = !signupMode;
    TextView sub = findViewById(R.id.login_subtitle);
    TextView toggle = findViewById(R.id.toggle_mode);
    EditText name = findViewById(R.id.input_name);
    if (signupMode) {
      sub.setText(R.string.login_subtitle_signup);
      toggle.setText(R.string.toggle_to_signin);
      name.setVisibility(View.VISIBLE);
      ((TextView)findViewById(R.id.btn_submit)).setText(R.string.action_signup);
    } else {
      sub.setText(R.string.login_subtitle_signin);
      toggle.setText(R.string.toggle_to_signup);
      name.setVisibility(View.GONE);
      ((TextView)findViewById(R.id.btn_submit)).setText(R.string.action_signin);
    }
  }

  public void onLoginSubmit(View v) {
    EditText emailEt = findViewById(R.id.input_email);
    EditText passEt = findViewById(R.id.input_password);
    EditText nameEt = findViewById(R.id.input_name);
    String email = emailEt.getText().toString().trim();
    String pass = passEt.getText().toString();
    if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
      Toast.makeText(this, "Completa email y contraseña", Toast.LENGTH_SHORT).show();
      return;
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      Toast.makeText(this, "Correo electrónico no válido", Toast.LENGTH_SHORT).show();
      return;
    }
    if (pass.length() < 6) {
      Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
      return;
    }
    v.setEnabled(false);
    if (signupMode) {
      auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(t -> {
        if (t.isSuccessful()) {
          String uid = auth.getCurrentUser().getUid();
          String name = nameEt.getText().toString().trim();
          java.util.Map<String,Object> m = new java.util.HashMap<>();
          m.put("email", email);
          if (!name.isEmpty()) m.put("name", name);
          com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .set(m, com.google.firebase.firestore.SetOptions.merge())
            .addOnCompleteListener(x -> {
              v.setEnabled(true);
              startActivity(new Intent(this, DashboardActivity.class));
              finish();
            });
        } else {
          v.setEnabled(true);
          showAuthError(t.getException());
        }
      });
    } else {
      auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(t -> {
        if (t.isSuccessful()) {
          v.setEnabled(true);
          startActivity(new Intent(this, DashboardActivity.class));
          finish();
        } else {
          v.setEnabled(true);
          showAuthError(t.getException());
        }
      });
    }
  }

  private void showAuthError(Exception ex) {
    String msg = "Ocurrió un error. Intenta nuevamente.";
    if (ex instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
      msg = "Este correo ya está registrado.";
    } else if (ex instanceof com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
      msg = "La contraseña es demasiado débil.";
    } else if (ex instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
      msg = "Credenciales inválidas. Verifica tu correo y contraseña.";
    } else if (ex instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {
      msg = "Usuario no encontrado o deshabilitado.";
    } else if (ex != null && ex.getMessage() != null) {
      msg = ex.getMessage();
    }
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
  }
}
