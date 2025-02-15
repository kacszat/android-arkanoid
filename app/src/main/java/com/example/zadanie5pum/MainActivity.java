package com.example.zadanie5pum;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private GameView GameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GameView = new GameView(this);
        setContentView(GameView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        GameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GameView.resume();
    }

    @Override
    protected void onDestroy() {
        GameView.stopSound();
        super.onDestroy();
    }
}