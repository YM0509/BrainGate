package com.example.braingate;

import androidx.annotation.NonNull;
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

public class AI {
    // Define the executor for background tasks
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // The interface to send the response back asynchronously
    public interface AIResponseCallback {
        void onSuccess(String response);
        void onFailure(@NonNull Throwable t);
    }

    // Modified method to accept a callback
    public void generateResponse(String prompt, AIResponseCallback callback) {
        GenerativeModel firebaseAI = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash-lite"); // Corrected model name
        GenerativeModelFutures model = GenerativeModelFutures.from(firebaseAI);
        Content content = new Content.Builder().addText(prompt).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                // Instead of trying to return, send the result to the callback
                callback.onSuccess(result.getText());
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                // Send the error to the callback
                callback.onFailure(t);
                t.printStackTrace();
            }
        }, executor);
    }
}