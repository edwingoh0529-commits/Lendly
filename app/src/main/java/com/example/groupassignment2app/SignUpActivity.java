package com.example.groupassignment2app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment2app.data.Repo;


public class SignUpActivity extends AppCompatActivity {

    private EditText edtName, edtStudentId, edtEmail, edtPassword, edtConfirm;
    private Button btnSignUp;
    private TextView txtGoLogin;
    private LoadingOverlay loading;

    private final Repo repo = Repo.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        
        InsetUtil.padTopAndBottom(
                findViewById(android.R.id.content));


        edtName = findViewById(R.id.edtName);
        edtStudentId = findViewById(R.id.edtStudentId);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirm = findViewById(R.id.edtConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtGoLogin = findViewById(R.id.txtGoLogin);
        loading = new LoadingOverlay(this);

        btnSignUp.setOnClickListener(v -> signUp());
        txtGoLogin.setOnClickListener(v -> finish());
    }

    private void signUp() {
        String name = edtName.getText().toString().trim();
        String studentId = edtStudentId.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirm = edtConfirm.getText().toString();

        if (TextUtils.isEmpty(name)) {
            edtName.setError("Enter your name"); edtName.requestFocus(); return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Enter a valid email"); edtEmail.requestFocus(); return;
        }
        if (password.length() < 6) {
            edtPassword.setError("At least 6 characters"); edtPassword.requestFocus(); return;
        }
        if (!password.equals(confirm)) {
            edtConfirm.setError("Passwords do not match"); edtConfirm.requestFocus(); return;
        }

        setBusy(true, "Creating your account\u2026");
        repo.auth().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    loading.show("Setting up your profile\u2026");

                    repo.saveUserProfile(name, email, studentId)
                            .addOnCompleteListener(t -> {
                                
                                loading.show("Welcome, " + name + "!");
                                Toast.makeText(this, "Welcome to Lendly, " + name + "!",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                overridePendingTransition(android.R.anim.fade_in,
                                        android.R.anim.fade_out);
                            });
                })
                .addOnFailureListener(error -> {
                    setBusy(false, null);
                    Toast.makeText(this, "Sign up failed: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void setBusy(boolean busy, String message) {
        if (busy) loading.show(message); else loading.hide();
        btnSignUp.setEnabled(!busy);
    }
}