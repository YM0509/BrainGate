package com.example.braingate;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppsDrawer extends AppCompatActivity {

    private RecyclerView appsRecyclerView;
    private List<ResolveInfo> installedApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.apps_drawer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        appsRecyclerView = findViewById(R.id.RView);
        loadInstalledApps();
        appsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        appsRecyclerView.setAdapter(new AppsAdapter());
    }

    /**
     * Retrieves a list of all launchable applications installed on the device.
     * This method queries the PackageManager for activities that can be launched
     * from the main launcher screen. The results are stored in the
     * {@code installedApps} list.
     */
    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        installedApps = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> allApps = pm.queryIntentActivities(intent, 0);
        if (allApps != null) {
            installedApps.addAll(allApps);
        }
    }

    class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ResolveInfo appInfo = installedApps.get(position);
            PackageManager pm = getPackageManager();
            holder.appName.setText(appInfo.loadLabel(pm));
            holder.appIcon.setImageDrawable(appInfo.loadIcon(pm));
        }

        @Override
        public int getItemCount() {
            return installedApps.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView appName;
            ImageView appIcon;

            ViewHolder(View itemView) {
                super(itemView);
                appName = itemView.findViewById(R.id.label);
                appIcon = itemView.findViewById(R.id.icon);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                ResolveInfo appInfo = installedApps.get(pos);
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(appInfo.activityInfo.packageName);
                String s=appInfo.loadLabel(getPackageManager()).toString();
                if("WhatsApp".equals(s)||"YouTube".equals(s))
                {
                    com.example.braingate.MyApplication myApp = (com.example.braingate.MyApplication) getApplication();
                    myApp.setGlobalString(appInfo.activityInfo.packageName);
                    Intent intent = new Intent(AppsDrawer.this, Quiz.class);
                    startActivity(intent);
                }
                else
                    startActivity(launchIntent);
            }
        }
    }
}
