package com.example.zadanie5pum;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MenuActivity extends AppCompatActivity {

    private MediaPlayer sound_play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button bt_game = findViewById(R.id.bt_game);
        Button bt_high_scores = findViewById(R.id.bt_high_scores);
        Button bt_options = findViewById(R.id.bt_options);
        Button bt_about = findViewById(R.id.bt_about);
        Button bt_exit = findViewById(R.id.bt_exit);
        sound_play = new MediaPlayer();

        bt_game.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                startActivity(intent);
                playSound(R.raw.click);
            }
        });

        bt_high_scores.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, HighScoresActivity.class);
                startActivity(intent);
                playSound(R.raw.click);
            }
        });

        bt_options.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, OptionsActivity.class);
                startActivity(intent);
                playSound(R.raw.click);
            }
        });

        bt_about.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, AboutActivity.class);
                startActivity(intent);
                playSound(R.raw.click);
            }
        });

        bt_exit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ActivityCompat.finishAffinity(MenuActivity.this);
                playSound(R.raw.click);
            }
        });

    }

    //Funkcja odtwarzajaca dźwięki
    private void playSound(int tempSound) {
        try {
            sound_play.reset();
            sound_play = MediaPlayer.create(this, tempSound);
            sound_play.start();
        } catch (Exception temp) {
            temp.printStackTrace();
        }
    }

    //Funkcja wyłączająca dźwięki
    @Override
    protected void onDestroy() {
        if (sound_play!=null){
            sound_play.release();
            sound_play=null;
        }
        super.onDestroy();
    }
}
