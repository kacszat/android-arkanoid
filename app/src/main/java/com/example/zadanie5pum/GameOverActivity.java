package com.example.zadanie5pum;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    TextView tv_game_over;
    TextView tv_loose_time;
    TextView tv_loose_move_count;
    Button bt_menu;
    Button bt_next;

    SharedPreferences game_data;
    SharedPreferences.Editor game_data_editor;
    private MediaPlayer sound_play;
    private int game_level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        sound_play = new MediaPlayer();
        tv_game_over = findViewById(R.id.tv_game_over);
        tv_loose_time = findViewById(R.id.tv_loose_time);
        tv_loose_move_count = findViewById(R.id.tv_loose_move_count);
        bt_menu = findViewById(R.id.bt_menu);
        bt_next = findViewById(R.id.bt_next);
        bt_next.setEnabled(false);
        bt_next.setVisibility(View.INVISIBLE);

        //Wczytywanie danych o grze
        game_data= getSharedPreferences("game_data",MODE_PRIVATE);
        game_data_editor = game_data.edit();
        boolean victoria_check = game_data.getBoolean("victoria_check",false);
        int destroyed_circles = game_data.getInt("destroyed_circles",0);
        int time_of_game = game_data.getInt("time_of_game",0);
        game_level = game_data.getInt("game_level",1);

        if (victoria_check) { //sprawdzenie, czy gracz wygrał, czy przegrał
            tv_game_over.setText("WYGRANA");
            playSound(R.raw.win);
            bt_next.setEnabled(true);
            bt_next.setVisibility(View.VISIBLE);
        } else {
            tv_game_over.setText("PRZEGRANA");
            playSound(R.raw.loose);
        }

        setYourTime(time_of_game);
        tv_loose_move_count.setText("Liczba zniszczonych bloków: " + String.valueOf(destroyed_circles));

        bt_menu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GameOverActivity.this, MenuActivity.class);
                startActivity(intent);
                playSound(R.raw.click);
                game_data_editor.clear();
            }
        });

        bt_next.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                playSound(R.raw.click);
                game_level++;
                game_data_editor.putInt("game_level",game_level);
                game_data_editor.commit();

                Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    private void setYourTime(int temp_time) {
        //System.out.println(temp_time);
        int hours = temp_time / 3600;
        int minutes = (temp_time - hours * 3600) / 60;
        int seconds = temp_time % 60;

        final String seconds_string = (seconds < 10 ? "0" : "") + String.valueOf(seconds);
        final String minutes_string = (minutes < 10 ? "0" : "") + String.valueOf(minutes);
        final String hours_string = (hours < 10 ? "0" : "") + String.valueOf(hours);

        runOnUiThread(() -> tv_loose_time.setText("Czas poziomu: " + hours_string + ":" + minutes_string + ":" +seconds_string));
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
