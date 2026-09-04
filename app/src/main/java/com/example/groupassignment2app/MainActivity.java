package com.example.groupassignment2app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.groupassignment2app.data.Repo;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private final Repo repo = Repo.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!repo.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        
        InsetUtil.padBottom(bottomNavigation);
        setupNavListener();

        if (savedInstanceState == null) {
            showFragment(new HomeFragment(), false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPendingBadge();
    }

    private void setupNavListener() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                showFragment(new HomeFragment(), false);
                return true;
            }
            if (id == R.id.nav_messages) {
                showFragment(new ChatListFragment(), false);
                return true;
            }
            if (id == R.id.nav_lends) {
                showFragment(new LendsFragment(), false);
                return true;
            }
            if (id == R.id.nav_notifications) {
                showFragment(new NotificationsFragment(), false);
                return true;
            }
            if (id == R.id.nav_profile) {
                showFragment(new ProfileFragment(), false);
                return true;
            }
            return false;
        });
    }

    
    public void showFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();

        if (!addToBackStack) {
            fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        FragmentTransaction tx = fm.beginTransaction();
        if (addToBackStack) {
            tx.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right);
        }
        tx.replace(R.id.fragment_container, fragment);
        if (addToBackStack) tx.addToBackStack(null);
        tx.commit();
    }

    public void selectTab(int menuItemId) {
        bottomNavigation.setOnItemSelectedListener(null);
        bottomNavigation.setSelectedItemId(menuItemId);
        setupNavListener();
    }

    public void refreshPendingBadge() {
        repo.loadIncomingRequests(requests -> {
            int pending = 0;
            for (com.example.groupassignment2app.model.BorrowRequest r : requests) {
                if (com.example.groupassignment2app.model.BorrowRequest.PENDING.equals(r.getStatus())) {
                    pending++;
                }
            }
            BadgeDrawable badge = bottomNavigation.getOrCreateBadge(R.id.nav_notifications);
            if (pending == 0) {
                badge.setVisible(false);
            } else {
                badge.setVisible(true);
                badge.setNumber(pending);
                badge.setBackgroundColor(ContextCompat.getColor(this, R.color.unread_dot_color));
                badge.setBadgeTextColor(ContextCompat.getColor(this, R.color.white));
            }
        });
    }

    public void logout() {
        repo.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}