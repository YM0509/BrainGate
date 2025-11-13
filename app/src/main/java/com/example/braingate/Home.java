package com.example.braingate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    private HomeViewModel homeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("com.example.braingate.app_prefs", MODE_PRIVATE);
        if (prefs.getBoolean("isFirstRun", true)) {
            startActivity(new Intent(Home.this, SettingsActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        TextView quoteTextView = findViewById(R.id.quote);
        homeViewModel.getQuote().observe(this, quoteTextView::setText);

        TextView date = findViewById(R.id.date);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            date.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy")));
        }

        findViewById(R.id.apps_drawer_button).setOnClickListener(v -> startActivity(new Intent(Home.this, AppsDrawer.class)));
        findViewById(R.id.focus_music_button).setOnClickListener(v -> startActivity(new Intent(Home.this, MusicPlayerActivity.class)));

        // --- Replaced Button with Dialog --- 
        if (!isMyAppDefaultLauncher()) {
            new AlertDialog.Builder(this)
                .setTitle("Set Default Launcher")
                .setMessage("For the best experience, please set BrainGate as your default home screen.")
                .setPositiveButton("Set Default", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Maybe Later", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
        }
    }

    private boolean isMyAppDefaultLauncher() {
        final Intent filter = new Intent(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<ResolveInfo> filters = getPackageManager().queryIntentActivities(filter, PackageManager.MATCH_DEFAULT_ONLY);


        String myPackageName = getPackageName();
        for (ResolveInfo info : filters) {
            if (myPackageName.equals(info.activityInfo.packageName)) {
                return info.isDefault;
            }
        }
        return false;
    }
}
