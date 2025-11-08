package com.example.braingate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Correct imports for the Generative AI SDK

public class Quiz extends AppCompatActivity {

    // Using an ExecutorService to run network operations on a background thread.
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isFirstFocus = true;
    private String correctAnswer = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

       // Button generateButton = findViewById(R.id.generate_button);
        TextView resultText = findViewById(R.id.resultText);
        EditText answerEditText = findViewById(R.id.ans);
        Button submitButton = findViewById(R.id.check);
        submitButton.setOnClickListener(v -> checkAnswer());
        answerEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && isFirstFocus) {
                if (answerEditText.getText().toString().equals("Your Answer")) {
                    answerEditText.setText("");
                }
                isFirstFocus = false; // Ensure it only happens once
            }
        });
         resultText.setText("Generating...");
         generate();


    }
    public void generate()
    {
        TextView resultText = findViewById(R.id.resultText);
        GenerativeModel firebaseAI = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash");
        GenerativeModelFutures model = GenerativeModelFutures.from(firebaseAI);
        Content prompt = new Content.Builder()
                .addText("Generate one study-related multiple-choice style question.For testing purpose let the question be what us the capital of france \n" +
                        "Output format must be exactly:\n" +
                        "\n" +
                        "<Question text>\n" +
                        "\n" +
                        "Answer: <Correct answer>\n" +
                        "\n" +
                        "Do not include explanations, options, or any extra text. \n" +
                        "Only provide the question followed by the correct answer at the end, prefixed with the word \"Answer:\".\n")
                .build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(prompt);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                for(int i = 0;i<=result.getText().length();i++)
                {
                    if(result.getText().startsWith("Answer", i))
                    {
                        resultText.setText(result.getText().substring(0,i-1));
                    }
                }
                String[] parts = result.getText().split("\\n\\nAnswer:");
                String question = parts[0].trim();
                correctAnswer = parts.length > 1 ? parts[1].trim() : "";
            }
            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, executor);
    }
    private void checkAnswer() {
        EditText answerEditText = findViewById(R.id.ans);
        String userAnswer = answerEditText.getText().toString().trim();

        if (userAnswer.isEmpty()) {
            Toast.makeText(this, "Please enter an answer.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userAnswer.equalsIgnoreCase(correctAnswer)) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            com.example.braingate.MyApplication myApp = (com.example.braingate.MyApplication) getApplication();
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(myApp.getGlobalString());
            startActivity(launchIntent);
        } else {
            Toast.makeText(this, "Wrong! The correct answer was: " + correctAnswer, Toast.LENGTH_LONG).show();
        }
    }

}
