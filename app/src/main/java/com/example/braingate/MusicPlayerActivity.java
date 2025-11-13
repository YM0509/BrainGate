package com.example.braingate;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayerActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Visualizer visualizer;
    private VisualizerView visualizerView;

    // View references
    private Button buttonPlayPause;
    private Button buttonPrev;
    private Button buttonNext;
    private LinearLayout controlsContainer;
    private LinearLayout loadingContainer;
    private TextView statusText;

    private List<String> musicUrls = new ArrayList<>();
    private int currentTrackIndex = 0;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        visualizerView = findViewById(R.id.visualizer_view);
        buttonPlayPause = findViewById(R.id.button_play_pause);
        buttonPrev = findViewById(R.id.button_prev);
        buttonNext = findViewById(R.id.button_next);
        controlsContainer = findViewById(R.id.controls_container);
        loadingContainer = findViewById(R.id.loading_container);
        statusText = findViewById(R.id.status_text);

        populateMusicList();

        buttonPlayPause.setOnClickListener(v -> togglePlayPause());
        buttonPrev.setOnClickListener(v -> playPrevious());
        buttonNext.setOnClickListener(v -> playNext());

        showLoading(true, "Waiting for permission...");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            initializeMediaPlayer();
        }
    }

    private void populateMusicList() {
        // Replaced with the new direct .mp3 URLs provided.
        musicUrls.clear();
        musicUrls.add("https://cdn.pixabay.com/download/audio/2025/07/14/audio_e02a60a0ac.mp3"); // Flow State
        musicUrls.add("https://cdn.pixabay.com/download/audio/2024/09/19/audio_768d7a6d95.mp3"); // Satisfying LoFi
        musicUrls.add("https://cdn.pixabay.com/download/audio/2023/10/23/audio_31af030980.mp3"); // Study Music 432hz
    }

    private void initializeMediaPlayer() {
        showLoading(true, "Loading Music...");

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            );
        } else {
            mediaPlayer.reset();
        }

        try {
            mediaPlayer.setDataSource(musicUrls.get(currentTrackIndex));
            mediaPlayer.setOnPreparedListener(mp -> {
                showLoading(false, "");
                setupVisualizer();
                mp.start();
                buttonPlayPause.setText("Pause");
            });
            mediaPlayer.setOnCompletionListener(mp -> playNext());
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                showLoading(true, "Error playing music. Code: " + what + ", Extra: " + extra);
                return true;
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            showLoading(true, "Invalid URL");
        }
    }

    private void setupVisualizer() {
        if (visualizer != null) {
            visualizer.release();
        }
        try {
            visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            visualizer.setDataCaptureListener(
                    new Visualizer.OnDataCaptureListener() {
                        public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                            if (visualizerView != null) {
                                visualizerView.updateVisualizer(bytes);
                            }
                        }
                        public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {}
                    },
                    Visualizer.getMaxCaptureRate() / 2, true, false
            );
            visualizer.setEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error setting up visualizer", Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            if (visualizer != null) visualizer.setEnabled(false);
            buttonPlayPause.setText("Play");
        } else {
            mediaPlayer.start();
            if (visualizer != null) visualizer.setEnabled(true);
            buttonPlayPause.setText("Pause");
        }
    }

    private void playNext() {
        if (musicUrls.isEmpty()) return;
        currentTrackIndex = (currentTrackIndex + 1) % musicUrls.size();
        initializeMediaPlayer();
    }

    private void playPrevious() {
        if (musicUrls.isEmpty()) return;
        currentTrackIndex = (currentTrackIndex - 1 + musicUrls.size()) % musicUrls.size();
        initializeMediaPlayer();
    }

    private void showLoading(boolean isLoading, String status) {
        if (isLoading) {
            loadingContainer.setVisibility(View.VISIBLE);
            controlsContainer.setVisibility(View.GONE);
            statusText.setText(status);
        } else {
            loadingContainer.setVisibility(View.GONE);
            controlsContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMediaPlayer();
            } else {
                showLoading(true, "Permission denied. Cannot play music.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (visualizer != null) {
            visualizer.release();
        }
    }
}
