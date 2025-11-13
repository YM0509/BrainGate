package com.example.braingate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Quiz extends AppCompatActivity {

    private boolean isFirstFocus = true;
    private String correctAnswer = "";
    private String currentSubjects = ""; // Store the subjects for the current question
    private int questionCount = 0;
    private int correctAnswersCount = 0;
    private CountDownTimer countDownTimer;

    // List to store topics of incorrectly answered questions
    private final List<String> incorrectTopics = new ArrayList<>();

    // View references
    private TextView timerText;
    private TextView resultText;
    private EditText answerEditText;
    private Button submitButton;

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

        // Initialize views
        resultText = findViewById(R.id.resultText);
        answerEditText = findViewById(R.id.ans);
        timerText = findViewById(R.id.timerText);
        submitButton = findViewById(R.id.check);

        submitButton.setOnClickListener(v -> {
                countDownTimer.cancel();
            handleNextStep();
        });

        answerEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && isFirstFocus) {
                if (answerEditText.getText().toString().equals("Your Answer")) {
                    answerEditText.setText("");
                }
                isFirstFocus = false;
            }
        });

        // Start the first question
        generate();
    }

    private void handleNextStep() {
        checkAnswer();

        if (questionCount < 3) {
            answerEditText.setText("");
            generate(); // Generate the next question
        } else {
            // Quiz is over, decide what to do
            if (correctAnswersCount >= 2) {
                // User passed
                Toast.makeText(this, "Quiz passed!", Toast.LENGTH_SHORT).show();
                MyApplication myapp = (MyApplication) getApplication();
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(myapp.getGlobalString());
                startActivity(launchIntent);
                finish();
            } else if (!incorrectTopics.isEmpty()) {
                // User failed and has topics to review, generate recommendations
                generateRecommendations();
            } else {
                // User failed but somehow has no incorrect topics
                Toast.makeText(this, "Come back later.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText(getString(R.string.time_remaining) + millisUntilFinished / 1000);
            }
            public void onFinish() {
                timerText.setText(R.string.time_s_up);
                Toast.makeText(Quiz.this, "Time's up!", Toast.LENGTH_SHORT).show();
                handleNextStep();
            }
        }.start();
    }

    public void generate() {
        questionCount++;
        resultText.setText(getString(R.string.generating));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String course = prefs.getString("course", "general knowledge");
        currentSubjects = prefs.getString("topics", "general knowledge");
        String difficulty = prefs.getString("difficulty", "medium");

        AI ai = new AI();
        String prompt = String.format("Generate one %s-level study-related multiple-choice style question on the topic %s of difficulty %s. For testing ask simple questions such as addition.\nOutput format must be exactly:\n<Question text>\n\nAnswer: <Correct answer>\nDo not include explanations, options, or any extra text.", difficulty, currentSubjects, difficulty);
        ai.generateResponse(prompt, new AI.AIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    // FIX: Replaced the crashing loop with a safe string split.
                    if (response != null && response.contains("Answer:")) {
                        String[] parts = response.split("\\n\\nAnswer:");
                        resultText.setText(parts[0].trim());
                        correctAnswer = parts.length > 1 ? parts[1].trim() : "";
                        startTimer();
                    } else {
                        resultText.setText("Failed to generate question. Trying again...");
                        generate(); // Retry on bad response
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                runOnUiThread(() -> resultText.setText(getString(R.string.error) + t.getMessage()));
            }
        });
    }

    private void checkAnswer() {
        //if (questionCount == 0) return; // Don't check before the first question
        String userAnswer = answerEditText.getText().toString().trim();

        if (userAnswer.equalsIgnoreCase(correctAnswer)) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            correctAnswersCount++;
        } else {
            Toast.makeText(this, "Wrong! The answer was: " + correctAnswer, Toast.LENGTH_LONG).show();
            // Add the topic of the incorrect question to our list
            if (currentSubjects != null && !currentSubjects.isEmpty()) {
                incorrectTopics.add(currentSubjects);
            }
        }
    }

    private void generateRecommendations() {
        // Update UI for recommendation state
        findViewById(R.id.textView2).setVisibility(View.GONE); // Hide "Question" title
        timerText.setVisibility(View.GONE);
        answerEditText.setVisibility(View.GONE);
        submitButton.setText("Close");
        submitButton.setOnClickListener(v -> finish());
        resultText.setText("Generating learning resources for you...");

        // Create a comma-separated string of unique topics
        String topicsString = incorrectTopics.stream().distinct().collect(Collectors.joining(", "));

        AI ai = new AI();
        String prompt = String.format("The user struggled with questions on the following topics: %s. Please recommend 2-3 online learning resources (like articles, video links, or tutorials) to help them understand these topics better. Format the response clearly.The response should be directed to the user directly and should begin with Here are some recommendations for you.", topicsString);

        ai.generateResponse(prompt, new AI.AIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    // Display the recommendations
                    resultText.setMovementMethod(LinkMovementMethod.getInstance());
                    resultText.setText(response);
                });
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                runOnUiThread(() -> resultText.setText("Could not generate recommendations: " + t.getMessage()));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
