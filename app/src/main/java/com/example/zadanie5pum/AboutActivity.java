package com.example.zadanie5pum;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    private MediaPlayer sound_play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        sound_play = new MediaPlayer();

        Button bt_menu = findViewById(R.id.bt_menu);
        bt_menu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AboutActivity.this, MenuActivity.class);
                startActivity(intent);
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
