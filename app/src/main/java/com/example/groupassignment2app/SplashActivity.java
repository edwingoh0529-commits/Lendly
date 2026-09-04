package com.example.groupassignment2app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment2app.data.Repo;


public class SplashActivity extends AppCompatActivity {

    private static final long MINIMUM_SHOW_MS = 1200;

    private TextView status;
    private final Repo repo = Repo.get();
    private long startedAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        InsetUtil.padTopAndBottom(
                findViewById(android.R.id.content));

        ImageView logo = findViewById(R.id.imgSplashLogo);
        status = findViewById(R.id.txtSplashStatus);

        logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splash_fade_in));

        startedAt = System.currentTimeMillis();
        decideWhereToGo();
    }

    private void decideWhereToGo() {
        if (!repo.isLoggedIn()) {
            status.setText("Welcome to Lendly");
            goNext(new Intent(this, LoginActivity.class));
            return;
        }

        status.setText("Signing you in\u2026");

        repo.loadUser(repo.uid(), new Repo.Result<com.example.groupassignment2app.model.AppUser>() {
            @Override
            public void onSuccess(com.example.groupassignment2app.model.AppUser user) {
                String name = user.getName();
                status.setText(name == null || name.isEmpty()
                        ? "Welcome back" : "Welcome back, " + name);
                goNext(new Intent(SplashActivity.this, MainActivity.class));
            }

            @Override
            public void onError(Exception e) {
                
                goNext(new Intent(SplashActivity.this, MainActivity.class));
            }
        });
    }

    private void goNext(Intent intent) {
        long elapsed = System.currentTimeMillis() - startedAt;
        long wait = Math.max(0, MINIMUM_SHOW_MS - elapsed);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, wait);
    }
}