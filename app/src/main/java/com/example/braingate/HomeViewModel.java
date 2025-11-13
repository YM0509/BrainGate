package com.example.braingate;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HomeViewModel extends AndroidViewModel {

    // Keys for saving data in SharedPreferences
    private static final String KEY_SAVED_QUOTE = "saved_quote";
    private static final String KEY_QUOTE_DATE = "quote_date";

    private final MutableLiveData<String> motivationalQuote = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        loadQuote();
    }

    public LiveData<String> getQuote() {
        return motivationalQuote;
    }

    private void loadQuote() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        String todayDateString = LocalDate.now().toString();

        String savedDate = prefs.getString(KEY_QUOTE_DATE, null);
        String savedQuote = prefs.getString(KEY_SAVED_QUOTE, null);

        // If we have a saved quote and it was saved today, use it.
        if (savedQuote != null && todayDateString.equals(savedDate)) {
            motivationalQuote.setValue(savedQuote);
        } else {
            // Otherwise, fetch a new one.
            fetchNewQuote();
        }
    }

    private void fetchNewQuote() {
        motivationalQuote.setValue("Generating quote...");

        AI ai = new AI();
        ai.generateResponse("Generate a motivational quote. The response should not include any other text other than the quote", new AI.AIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                // Save the new quote and the current date
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
                prefs.edit()
                    .putString(KEY_SAVED_QUOTE, response)
                    .putString(KEY_QUOTE_DATE, LocalDate.now().toString())
                    .apply();

                // Update the UI
                motivationalQuote.postValue(response);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                motivationalQuote.postValue("Failed to load quote.");
            }
        });
    }
}
