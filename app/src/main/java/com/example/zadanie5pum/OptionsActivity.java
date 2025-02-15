package com.example.zadanie5pum;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OptionsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Button bt_menu;
    Button bt_save_settings;
    Button bt_game_reset;
    String game_modes_value;
    String game_lifes_value;
    TextView tv_actual_level;
    TextView tv_more_info;
    Spinner spinner_game_modes;
    Spinner spinner_game_lifes;
    Switch switch_bonus;
    Boolean game_with_bonuses = false;

    SharedPreferences game_data;
    SharedPreferences.Editor game_data_editor;
    private MediaPlayer sound_play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        sound_play = new MediaPlayer();

        //Ustawienie dzielonych danych
        game_data = getSharedPreferences("game_data",Context.MODE_PRIVATE);
        game_data_editor = game_data.edit();
        int game_level = game_data.getInt("game_level",1);
        boolean is_game_started = game_data.getBoolean("is_game_started",false);

        bt_menu = findViewById(R.id.bt_menu);
        bt_save_settings = findViewById(R.id.bt_save_settings);
        bt_game_reset = findViewById(R.id.bt_game_reset);
        tv_actual_level = findViewById(R.id.tv_actual_level);
        tv_more_info = findViewById(R.id.tv_more_info);
        spinner_game_modes = findViewById(R.id.spinner_game_modes);
        spinner_game_lifes = findViewById(R.id.spinner_game_lifes);
        switch_bonus = findViewById(R.id.switch_bonus);

        tv_actual_level.setText("Aktualny poziom: " + game_level);
        if (is_game_started){ // w zależności od wartości flagi, można zmieniać ustawienia gry
            tv_more_info.setText("Ustawienia nie mogą być zmieniane w trakcie gry. Zresetuj stan gry," +
                    " by móc edytować zasady rogrywki. Oznacza to utratę postępów.");
            notEnableSpinner();
        } else {
            tv_more_info.setText("");
            setEnableSpinner();
        }


        //Wczytywanie wyboru poziomu trudności
        String[] string_game_modes = getResources().getStringArray(R.array.array_game_modes);
        ArrayAdapter<CharSequence> adapter_games_modes = new ArrayAdapter( this, android.R.layout.simple_spinner_item, string_game_modes);
        adapter_games_modes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_game_modes.setAdapter(adapter_games_modes);
        spinner_game_modes.setOnItemSelectedListener(this);

        //Wczytywanie liczby żyć
        String[] string_game_lifes = getResources().getStringArray(R.array.array_game_lifes);
        ArrayAdapter<CharSequence> adapter_game_lifes = new ArrayAdapter( this, android.R.layout.simple_spinner_item, string_game_lifes);
        adapter_game_lifes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_game_lifes.setAdapter(adapter_game_lifes);
        spinner_game_lifes.setOnItemSelectedListener(this);

        bt_game_reset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                game_data_editor.putInt("game_level",1).commit();
                game_data_editor.putBoolean("is_game_started",false).commit();
                game_data_editor.putFloat("paddle_width",240).commit();
                game_data_editor.putFloat("ball_radius",30).commit();
                game_data_editor.putFloat("ball_speed_X",10f).commit();
                game_data_editor.putFloat("ball_speed_Y",10f).commit();

                Intent intent = new Intent(OptionsActivity.this, OptionsActivity.class);
                startActivity(intent);
                playSound(R.raw.click);
            }
        });

        switch_bonus.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                game_with_bonuses = true;
                playSound(R.raw.click);
            }
        });

        bt_menu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OptionsActivity.this, MenuActivity.class);
                startActivity(intent);
                playSound(R.raw.click);
            }
        });

        bt_save_settings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                settingsSave();
                Intent intent_moves_schuffle = new Intent(OptionsActivity.this, MenuActivity.class);
                startActivity(intent_moves_schuffle);
                playSound(R.raw.click);
            }
        });

    }

    //Funkcja zapisująca ustawienia
    private void settingsSave() {

        if (game_modes_value.equals("Łatwy")) {
            game_data_editor.putInt("game_modes",45);
        }
        if (game_modes_value.equals("Normalny")) {
            game_data_editor.putInt("game_modes",30);
        }
        if (game_modes_value.equals("Trudny")) {
            game_data_editor.putInt("game_modes",15);
        }

        game_data_editor.putInt("game_lifes",Integer.parseInt(game_lifes_value));
        game_data_editor.putBoolean("game_with_bonuses",game_with_bonuses);
        game_data_editor.commit();
    }

    //Funkcja pozwalająca klikać w spinnery i switch
    private void setEnableSpinner() {
        spinner_game_modes.setEnabled(true);
        spinner_game_lifes.setEnabled(true);
        switch_bonus.setEnabled(true);
    }

    //Funkcja zabraniajaca klikać w spinnery i switch
    private void notEnableSpinner() {
        spinner_game_modes.setEnabled(false);
        spinner_game_lifes.setEnabled(false);
        switch_bonus.setEnabled(false);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spinner_game_modes) {
            game_modes_value = parent.getItemAtPosition(position).toString();
        }
        if (parent.getId() == R.id.spinner_game_lifes) {
            game_lifes_value = parent.getItemAtPosition(position).toString();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
