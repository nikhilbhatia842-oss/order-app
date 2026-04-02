package com.orderapp;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.orderapp.history.HistoryFragment;

public class MainActivity extends AppCompatActivity {

    private DebugBroadcastReceiver debugReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerDebugReceiver();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            showFragment(new OrderFormFragment());
            bottomNav.setSelectedItemId(R.id.nav_order_form);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (item.getItemId() == R.id.nav_history) {
                if (current instanceof HistoryFragment) return true;
                showFragment(new HistoryFragment());
            } else {
                if (current instanceof OrderFormFragment) return true;
                showFragment(new OrderFormFragment());
            }
            return true;
        });
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (debugReceiver != null) {
            try {
                unregisterReceiver(debugReceiver);
            } catch (IllegalArgumentException e) {
                android.util.Log.d("MainActivity", "Debug receiver already unregistered");
            }
        }
    }

    private void registerDebugReceiver() {
        debugReceiver = new DebugBroadcastReceiver();
        IntentFilter filter = new IntentFilter(DebugBroadcastReceiver.TEST_ACTION);
        try {
            registerReceiver(debugReceiver, filter, Context.RECEIVER_EXPORTED);
            android.util.Log.d("MainActivity", "Debug broadcast receiver registered");
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to register debug receiver", e);
        }
    }
}
