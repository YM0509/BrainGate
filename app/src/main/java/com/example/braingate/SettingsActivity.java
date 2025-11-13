package com.example.braingate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // Load the SettingsFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        // Set up the continue button
        Button continueButton = findViewById(R.id.continue_button);
        continueButton.setOnClickListener(v -> {
            // Mark the first run as complete
            SharedPreferences prefs = getSharedPreferences("com.example.braingate.app_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("isFirstRun", false).apply();

            // Launch the Home activity
            Intent intent = new Intent(SettingsActivity.this, Home.class);
            // Clear the activity stack so the user can't go back to settings
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Hide the action bar for a cleaner look on the initial setup screen
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }
}