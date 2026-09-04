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


public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin, btnGoogle;
    private TextView txtGoSignUp, txtForgot;
    private LoadingOverlay loading;

    private final Repo repo = Repo.get();
    private GoogleSignInHelper googleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        
        InsetUtil.padTopAndBottom(
                findViewById(android.R.id.content));

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtGoSignUp = findViewById(R.id.txtGoSignUp);
        txtForgot = findViewById(R.id.txtForgotPassword);
        btnGoogle = findViewById(R.id.btnGoogleSignIn);
        loading = new LoadingOverlay(this);
        googleSignIn = new GoogleSignInHelper(this);

        btnLogin.setOnClickListener(v -> login());
        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        txtGoSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));

        txtForgot.setOnClickListener(v -> resetPassword());
    }

    private void login() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Enter a valid email");
            edtEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Enter your password");
            edtPassword.requestFocus();
            return;
        }

        setBusy(true, "Signing you in\u2026");
        repo.auth().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    loading.show("Loading your campus\u2026");
                    goToMain();
                })
                .addOnFailureListener(error -> {
                    setBusy(false, null);
                    Toast.makeText(this,
                            "Login failed: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void signInWithGoogle() {
        setBusy(true, "Opening Google\u2026");

        googleSignIn.signIn(new GoogleSignInHelper.Callback() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser user, boolean isNewUser) {
                loading.show(isNewUser ? "Setting up your profile\u2026" : "Loading your campus\u2026");
                if (isNewUser) {
                    Toast.makeText(LoginActivity.this,
                            "Welcome to Lendly!", Toast.LENGTH_SHORT).show();
                }
                goToMain();
            }

            @Override
            public void onFailure(String message) {
                setBusy(false, null);
                new androidx.appcompat.app.AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Could not sign in with Google")
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show();
            }

            @Override
            public void onCancelled() {
                setBusy(false, null);
            }
        });
    }

    private void resetPassword() {
        String email = edtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Type your email first, then tap this again");
            edtEmail.requestFocus();
            return;
        }
        repo.auth().sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> Toast.makeText(this,
                        "Reset link sent to " + email, Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(this,
                        e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void setBusy(boolean busy, String message) {
        if (busy) loading.show(message); else loading.hide();
        btnLogin.setEnabled(!busy);
        btnGoogle.setEnabled(!busy);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}