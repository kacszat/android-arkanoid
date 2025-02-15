package com.example.zadanie5pum;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HighScoresActivity extends AppCompatActivity {

    Button bt_menu;
    Button bt_reset_scores;
    TextView tv_time_best;
    TextView tv_level_best;
    TextView tv_destroyed_best;
    SharedPreferences game_data;
    SharedPreferences.Editor game_data_editor;
    private MediaPlayer sound_play;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        bt_menu = findViewById(R.id.bt_menu);
        bt_reset_scores = findViewById(R.id.bt_reset_scores);
        tv_level_best = findViewById(R.id.tv_level_best);
        tv_time_best = findViewById(R.id.tv_time_best);
        tv_destroyed_best = findViewById(R.id.tv_destroyed_best);
        sound_play = new MediaPlayer();

        //Wczytywanie dzielonych danych najlepszych wyników
        game_data = getSharedPreferences("game_data",MODE_PRIVATE);
        game_data_editor = game_data.edit();
        int best_game_level = game_data.getInt("best_achieved_level",0);
        int all_game_time = game_data.getInt("all_time_of_game",0);
        int all_game_destroyed_circles = game_data.getInt("all_destroyed_circles",0);

        setBestTime(all_game_time);
        tv_level_best.setText(String.valueOf(best_game_level));
        tv_destroyed_best.setText(String.valueOf(all_game_destroyed_circles));

        bt_menu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HighScoresActivity.this, MenuActivity.class);
                startActivity(intent);
                playSound(R.raw.click);
            }
        });

        bt_reset_scores.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                game_data_editor.putInt("time_of_game",0);
                game_data_editor.putInt("destroyed_circles",0);
                game_data_editor.putInt("all_time_of_game",0);
                game_data_editor.putInt("all_destroyed_circles",0);
                game_data_editor.putInt("best_achieved_level",0);
                game_data_editor.commit();
                tv_time_best.setText("00:00:00");
                tv_level_best.setText("0");
                tv_destroyed_best.setText("0");
                playSound(R.raw.click);
            }
        });
    }

    //Funkcja pokazująca najlepszy czas w godzinach, minutach i sekundach
    private void setBestTime(int temp_time) {
        int hours = temp_time / 3600;
        int minutes = (temp_time - hours * 3600) / 60;
        int seconds = temp_time % 60;

        final String seconds_string = (seconds < 10 ? "0" : "") + String.valueOf(seconds);
        final String minutes_string = (minutes < 10 ? "0" : "") + String.valueOf(minutes);
        final String hours_string = (hours < 10 ? "0" : "") + String.valueOf(hours);

        runOnUiThread(() -> tv_time_best.setText(hours_string + ":" + minutes_string + ":" +seconds_string));
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
