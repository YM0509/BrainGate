package com.example.braingate;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AI_Help extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ai_help);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AI ai = new AI();
        EditText input = findViewById(R.id.input);
        String prompt = input.getText().toString();
        TextView resultText = findViewById(R.id.result);
        Button generate = findViewById(R.id.generate);
        generate.setOnClickListener(v ->{ resultText.setText("Generating...");
        ai.generateResponse(prompt, new AI.AIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    resultText.setText(response);
                });
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                runOnUiThread(() -> resultText.setText(getString(R.string.error) + t.getMessage()));
            }
        });});

    }
}