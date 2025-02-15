package com.example.zadanie5pum;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.content.ContextCompat;

public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {


    private Thread gameThread = null;
    private SurfaceHolder surfaceHolder;
    private boolean running = false;
    private Canvas canvas;
    private Paint paint;
    private Context context;

    private List<Ball> balls_list; // Lista piłek
    private float ballX, ballY; // Położenie piłki
    private float ballSpeedX, ballSpeedY; // Prędkość piłki
    private float ballRadius; // Promień piłki

    private float paddleX, paddleY; // Położenie paletki
    private float paddleWidth; // Szerokość paletki
    private float paddleHeight = 20; // Wysokość paletki
    private float paddleSpeed = 10; // Prędkość paletki

    private List<Circle> circles_list;
    private int circles_number = 1; // liczba kul w rzędzie
    private int circles_number_rows = 1; // liczba rzędów z kulami
    private int circle_radius; // promień kul
    private int circle_lifes; // życia danej kuli
    private boolean circle_colision; // flaga sprawdzająca czy kula jest w stanie kolizji
    private boolean circle_bonus; // flaga sprawdzająca, czy dana kula ma bonus w sobie
    private Random random_circle_lifes, random_bonus;
    private int circles_destroyed = 0; // liczba zniszczonych kuli

    SharedPreferences game_data; // współzielenie danych pomiędzy aktywnościami
    SharedPreferences.Editor game_data_editor;

    private int time_count = 0; // mierzenie czasu gry
    private Timer timer_game;
    private boolean is_timer_active;

    private boolean show_game_info = false; // flaga sprawdzająca, czy widoczna jest informacja o życiach i poziomie
    private long show_game_info_until; // informacja do jakiej chwili opóźnić ukrycie kouminkatu
    private Handler handler_game_lifes = new Handler();

    private boolean show_bonus_info = false; // flaga sprawdzająca, czy widoczna jest informacja o bonusie
    private long show_bonus_info_until; // informacja do jakiej chwili opóźnić ukrycie kouminkatu
    private Handler handler_bonus = new Handler();
    private String bonus_type;

    private int game_lifes; // liczba żyć gracza
    private int game_level; // aktualny poziom

    private MediaPlayer sound_play; // odtwarzacz dźwięków


    private static class Circle { //Klasa definiująca parametry kuli do zbicia
        float x, y, radius;
        int lifes;
        boolean colision, bonus;

        Circle(float x, float y, float radius, int lifes, boolean colision, boolean bonus) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.lifes = lifes;
            this.colision = colision;
            this.bonus = bonus;
        }
    }


    private static class Ball { // Klasa definiująca parametry piłki (dzięki bonusom może być ich więcej)
        float x, y, speedX, speedY, radius;
        Ball(float x, float y, float speedX, float speedY, float radius) {
            this.x = x;
            this.y = y;
            this.speedX = speedX;
            this.speedY = speedY;
            this.radius = radius;
        }
    }


    public GameView(Context context) {
        super(context);
        this.context = context;
        surfaceHolder = getHolder();
        paint = new Paint();
        circles_list = new ArrayList<>();
        balls_list = new ArrayList<>();
        //dead_balls_list = new ArrayList<>();
        random_circle_lifes = new Random();
        random_bonus = new Random();
        sound_play = new MediaPlayer();

        game_data = context.getSharedPreferences("game_data", Context.MODE_PRIVATE);
        game_data_editor = game_data.edit();

        loadLevel(); // pobranie danych o aktualnym poziomie
        game_data_editor.putInt("game_level", game_level).commit();
        game_data_editor.putBoolean("is_game_started", true).commit(); // Flaga sygnalizująca aktywną grę

        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //initializationBall(false); funkcja ta uruchamiana jest w funkcji showGameInfo()

        paddleX = (getWidth() - paddleWidth) / 2f;
        paddleY = getHeight() - 12 * paddleHeight; // Umieść paletkę na dole ekranu

        // Generowanie kul do zbicia
        generateCircles();

        // Rozpocznij wątek po utworzeniu powierzchni
        resume();

        // Pokaż liczbę żyć i numer poziomu
        showGameInfo(null);

        // Uruchomienie mierzenia czasu
        loadTimer();
    }

    private void initializationBall(Ball loose_ball, boolean is_ball_static) { // Inicjalizacja położenia i prędkości piłki
        if (balls_list.isEmpty()) { // jeśli lista piłek jest posta, dodaj nową piłkę
            Ball ball = new Ball(getWidth() / 2f, getHeight() / 2f, 10f, 10f, ballRadius);
            balls_list.add(ball);
        }

        if (loose_ball == null) {
            loose_ball = balls_list.get(0);
        }

        loose_ball.x = getWidth() / 2f;
        loose_ball.y = getHeight() / 2f;

        if (is_ball_static) {
            loose_ball.speedX = 0;
            loose_ball.speedY = 0;
        } else {
            loose_ball.speedX = game_data.getFloat("ball_speed_X", 10f);
            loose_ball.speedY = game_data.getFloat("ball_speed_Y", 10f);
        }

    }

    private void loadLevel() { // Funkcja wczytująca aktualny poziom i jego ustawienia
        circle_radius = game_data.getInt("game_modes", 30);
        game_lifes = game_data.getInt("game_lifes", 10);
        game_level = game_data.getInt("game_level", 1);
        paddleWidth = game_data.getFloat("paddle_width", 240);
        ballRadius = game_data.getFloat("ball_radius", 30);
        if (game_level % 2 == 0) { // W zależności od poziomy, generowana jest określona liczba wierszy i kul w wierszach
            circles_number = 10;
        } else {
            circles_number = 5;
        }
        circles_number_rows = (int) Math.floor((game_level + 1) / 2);
    }

    private void generateCircles() { // Funkcja generująca kule (bloki) do zbicia
        circles_list.clear();

        // Obliczana odległość między kulami
        float columns_spacing = (getHeight() / 3f) / circles_number_rows; // 1/3 wysokości ekranu podzielona przez liczbę rzędów
        float rows_spacing = getWidth() / (circles_number + 1); // Szerokość ekranu podzielona przez liczbę kolumn

        // Generacja kul w równych rzędach
        for (int i = 0; i < circles_number_rows; i++) {
            for (int j = 0; j < circles_number; j++) {
                // Obliczanie pozycji każdej kuli
                float x = (j + 1) * rows_spacing; // Pozycja x, z odpowiednim odstępem
                float y = (i + 1) * columns_spacing; // Pozycja y, z odpowiednim odstępem

                circle_lifes = random_circle_lifes.nextInt(3) + 1; // wylosowanie liczby żyć kuli
                circle_colision = false;
                circle_bonus = (random_bonus.nextInt(20) + 1) == 1; // szansa wylosowania bonusu 1/20

                // Dodanie nowej kuli do listy
                circles_list.add(new Circle(x, y, circle_radius, circle_lifes, circle_colision, circle_bonus));
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Zmiana rozmiaru powierzchni - aktualizacja położenia piłki i paletki
        ballX = width / 2f;
        ballY = height / 2f;

        paddleX = (width - paddleWidth) / 2f;
        paddleY = height - 12 * paddleHeight; // Umieść paletkę na dole ekranu

        // Ponowna generacja kuli po zmianie powierzchni
        //generateCircles();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Opcjonalnie zatrzymaj animację lub inne działania przed zniszczeniem powierzchni
        pause();
    }

    @Override
    public void run() {
        while (running) {
            if (surfaceHolder.getSurface().isValid()) {
                canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.BLACK); // Tło ekranu

                // Rysuj paletkę
                paint.setColor(ContextCompat.getColor(context, R.color.white));
                canvas.drawRect(paddleX, paddleY, paddleX + paddleWidth, paddleY + paddleHeight, paint);

                // Rysuj piłki
                for (Ball ball : balls_list) {
                    paint.setColor(ContextCompat.getColor(context, R.color.white));
                    canvas.drawCircle(ball.x, ball.y, ball.radius, paint);
                }

                // Rysuj kule do zbicia
                for (Circle circle : circles_list) {
                    switch (circle.lifes) {
                        case 3:
                            paint.setColor(Color.RED); // Czerwony
                            break;
                        case 2:
                            paint.setColor(Color.MAGENTA); // Różowy
                            break;
                        case 1:
                            paint.setColor(Color.WHITE); // Biały
                            break;
                    }
                    canvas.drawCircle(circle.x, circle.y, circle.radius, paint);
                }

                // Rysuj liczbę żyć i numer poziomu na środku ekranu, jeśli flaga jest ustawiona
                if (show_game_info && System.currentTimeMillis() < show_game_info_until) {
                    paint.setColor(Color.MAGENTA);
                    paint.setFakeBoldText(true);
                    paint.setTextSize(100);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText("ŻYCIA: " + game_lifes, getWidth() / 2f, getWidth() * 5 / 4f, paint);
                    float text_spacing = paint.getTextSize() + 20;
                    canvas.drawText("POZIOM: " + game_level, getWidth() / 2f, getWidth() * 5 / 4f + text_spacing, paint);
                }

                // Rysuj informację o bonusie
                if (show_bonus_info && System.currentTimeMillis() < show_bonus_info_until) {
                    paint.setColor(Color.MAGENTA);
                    paint.setFakeBoldText(true);
                    paint.setTextSize(70);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText("BONUS " + bonus_type, getWidth() / 2f, getWidth() * 5 / 4f, paint);
                }

                // Aktualizuj położenie piłki i paletki
                updateBallPosition();

                surfaceHolder.unlockCanvasAndPost(canvas);
            }

            try {
                Thread.sleep(8); // Około 120 klatek na sekundę
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateBallPosition() {
        for (Ball ball : balls_list) {
            ball.x += ball.speedX;
            ball.y += ball.speedY;

            // Odbij piłkę od ścian ekranu
            if (ball.x - ball.radius < 0 || ball.x + ball.radius > getWidth()) {
                ball.speedX = -ball.speedX;
                playSound(R.raw.bloop);
            }
            if (ball.y - ball.radius < 0 || ball.y + ball.radius > getHeight()) {
                ball.speedY = -ball.speedY;
                playSound(R.raw.bloop);
            }

            // Odbij piłkę od paletki
            if (ball.y + ball.radius > paddleY && ball.x > paddleX && ball.x < paddleX + paddleWidth) {
                float hit_position = (ball.x - (paddleX + paddleWidth / 2)) / (paddleWidth / 2);
                float bounce_angle = hit_position * (float) (Math.PI / 3);
                playSound(R.raw.bloop);

                float speed = (float) Math.sqrt(ball.speedX * ball.speedX + ball.speedY * ball.speedY);
                ball.speedX = speed * (float) Math.sin(bounce_angle);
                ball.speedY = -Math.abs(speed * (float) Math.cos(bounce_angle));
            }

            // Sprawdź kolizje z kulami
            Iterator<Circle> iterator = circles_list.iterator();
            while (iterator.hasNext()) {
                Circle circle = iterator.next();
                if (!circle.colision) {
                    float distance = (float) Math.sqrt(Math.pow(ball.x - circle.x, 2) + Math.pow(ball.y - circle.y, 2));
                    if (distance < ball.radius + circle.radius) {
                        circle.lifes--;

                        playSound(R.raw.collect);
                        if (circle.lifes == 0) {
                            iterator.remove();
                            circles_destroyed++;
                        } else {
                            circle.colision = true;
                            // wyłączenie kolizji piłki z kulami w celu uniknięcia multiplikowania kolizji przy jednym kontakcie
                            handler_game_lifes.postDelayed(() -> circle.colision = false, 200);
                        }

                        if (circle.bonus) {
                            activeBonus();
                        }

                        ball.speedY = -ball.speedY;
                        checkWinCondition();
                        break;
                    }
                }
            }
        }
        loosingCondition();
    }

    private void activeBonus() { // Funkcja przydzielająca typ bonusu
        Random random_bonus_function = new Random();
        int actual_bonus = random_bonus_function.nextInt(3) + 1;

        if (game_data.getBoolean("game_with_bonuses", false)) { // flaga sprawdzająca, czy włączony jest tryb gry z bonusami

            switch (actual_bonus) {
                case 0: // rozszerzenie paletki
                    if (paddleWidth <= 400) {
                        paddleWidth += 40;
                        game_data_editor.putFloat("paddle_width", paddleWidth).commit();
                        bonus_type = "SZERSZA PALETKA";
                        showBonusInfo();
                        playSound(R.raw.bonus);
                    }
                    break;

                case 1: // powiększenie średnicy piłki
                    if (ballRadius <= 50) {
                        ballRadius += 5;
                        game_data_editor.putFloat("ball_radius", ballRadius).commit();
                        bonus_type = "WIĘKSZA PIŁKA";
                        showBonusInfo();
                        playSound(R.raw.bonus);
                    }
                    break;

                case 2: // zwiększenie prędkości piłki
                    for (Ball ball : balls_list) {
                        // Oblicz bieżącą całkowitą prędkość piłki
                        float currentSpeed = (float) Math.sqrt(ball.speedX * ball.speedX + ball.speedY * ball.speedY);
                        // Dodaj 5 jednostek do bieżącej prędkości
                        float newSpeed = currentSpeed + 5;

                        // Sprawdź, czy nowa prędkość nie przekracza 25 jednostek
                        if (newSpeed > 25) {
                            newSpeed = 25;
                        }

                        // Oblicz bieżący kąt ruchu piłki
                        float currentAngle = (float) Math.atan2(ball.speedY, ball.speedX);

                        // Przelicz nowe składowe prędkości
                        float newSpeedX = newSpeed * (float) Math.cos(currentAngle);
                        float newSpeedY = newSpeed * (float) Math.sin(currentAngle);

                        // Ogranicz prędkości składowe do maksymalnie 25 jednostek
                        if (Math.abs(newSpeedX) > 25) {
                            newSpeedX = 25 * (newSpeedX / Math.abs(newSpeedX)); // Znak zachowany, wartość ograniczona do 25
                        }
                        if (Math.abs(newSpeedY) > 25) {
                            newSpeedY = 25 * (newSpeedY / Math.abs(newSpeedY)); // Znak zachowany, wartość ograniczona do 25
                        }

                        // Zaktualizuj prędkości piłki
                        ball.speedX = newSpeedX;
                        ball.speedY = newSpeedY;

                        // Zapisz nowe prędkości w danych gry
                        game_data_editor.putFloat("ball_speed_X", ball.speedX).commit();
                        game_data_editor.putFloat("ball_speed_Y", ball.speedY).commit();
                    }
                    bonus_type = "WIĘKSZA PRĘDKOŚĆ";
                    showBonusInfo();
                    playSound(R.raw.bonus);
                    break;
                case 3: // dodawanie bonusowych piłek
                    if (balls_list.size() < 3) { // maksymalnie na planszy mogą być 3 piłki
                        Ball newBall = new Ball(getWidth() / 2f, getHeight() / 2f, 10f, 10f, ballRadius);
                        balls_list.add(newBall);
                        bonus_type = "DODATKOWA PIŁKA";
                        showBonusInfo();
                        playSound(R.raw.bonus);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) { //Funkcja do przesuwania paletki przez gracza
        float touchX = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // Oblicz nowe położenie paletki
                float newPaddleX = touchX - paddleWidth / 2;

                // Sprawdź, czy nowe położenie wychodzi poza granice ekranu
                if (newPaddleX < -paddleWidth / 2) {
                    // Jeśli nowe położenie wychodzi poza lewą krawędź ekranu, ustaw paletkę na początku ekranu
                    paddleX = -paddleWidth / 2;
                } else if (newPaddleX + paddleWidth > getWidth() + paddleWidth / 2) {
                    // Jeśli nowe położenie wychodzi poza prawą krawędź ekranu, ustaw paletkę na końcu ekranu
                    paddleX = getWidth() - paddleWidth + paddleWidth / 2;
                } else {
                    // W przeciwnym razie ustaw paletkę na nowej pozycji
                    paddleX = newPaddleX;
                }
                break;
        }

        return true;
    }

    private void checkWinCondition() {
        if (circles_list.isEmpty()) {
            timer_game.cancel();
            // Wysłanie danych o grze
            game_data_editor.putBoolean("victoria_check", true);
            game_data_editor.putInt("achieved_level", game_level);
            game_data_editor.putInt("time_of_game", time_count);
            game_data_editor.putInt("destroyed_circles", circles_destroyed);

            // Pobranie danych o najlepszych wynikach
            int best_game_level = game_data.getInt("best_achieved_level", 0);
            int all_game_time = game_data.getInt("all_time_of_game", 0);
            int all_game_destroyed_circles = game_data.getInt("all_destroyed_circles", 0);

            // Dodawanie kolejnych rekordów do sumarycznych wyników
            game_data_editor.putInt("all_time_of_game", all_game_time + time_count);
            game_data_editor.putInt("all_destroyed_circles", all_game_destroyed_circles + circles_destroyed);

            // SPrawdzenie, czy nie osiagnięto nowych najlepszych wyników
            if (game_level > best_game_level) {
                game_data_editor.putInt("best_achieved_level", game_level);
            }

            game_data_editor.commit();

            Intent intent = new Intent(GameView.this.getContext(), GameOverActivity.class);
            context.startActivity(intent);
        }
    }

    private void loosingCondition() { // warunek sprawdzający, czy gracz przegrał
        for (Ball ball : balls_list) {
            if (ball.y >= getHeight() - ball.radius) { // jeśli piłka dotknię dna, gracz traci 1 życie
                game_lifes--;

                if (game_lifes == 0) { // jeśli liczba żyć gracza wynosi 0, gracz przegrywa
                    game_data_editor.putBoolean("victoria_check", false);
                    game_data_editor.putInt("time_of_game", time_count);
                    game_data_editor.putInt("destroyed_circles", circles_destroyed);
                    game_data_editor.commit();

                    Intent intent = new Intent(GameView.this.getContext(), GameOverActivity.class);
                    context.startActivity(intent);
                } else {
                    showGameInfo(ball);
                }

            }
        }
    }

    private void showGameInfo(Ball current_ball) {
        initializationBall(current_ball,true);
        show_game_info = true; // Ustawienie flagi do wyświetlania liczby żyć i numeru poziomu i czasu zakończenia wyświetlania
        show_game_info_until = System.currentTimeMillis() + 2000; // Wyświetlaj przez 2 sekundy

        handler_game_lifes.postDelayed(new Runnable() {
            @Override
            public void run() {
                show_game_info = false; // Przestań wyświetlać po 2 sekundach
                initializationBall(current_ball,false);
            }
        }, 2000);
    }

    private void showBonusInfo() {
        show_bonus_info = true; // Ustawienie flagi do wyświetlania informacji o bonusie
        show_bonus_info_until = System.currentTimeMillis() + 1000; // Wyświetlaj przez 1 sekunde

        handler_bonus.postDelayed(new Runnable() {
            @Override
            public void run() {
                show_bonus_info = false; // Przestań wyświetlać po 1 sekundzie
            }
        }, 1000);
    }

    public void pause() {
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // Uruchomienie mierzenia czasu
    private void loadTimer() {
        timer_game = new Timer();
        timer_game.schedule(new TimerTask() {
            @Override
            public void run() {
                time_count++;
                is_timer_active = true;
                //System.out.println(time_count);
            }
        }, 1000, 1000);
    }

    //Funkcja odtwarzająca dźwięki
    private void playSound(int tempSound) {
        try {
            sound_play.reset();
            sound_play = MediaPlayer.create(getContext(), tempSound);
            sound_play.start();
        } catch (Exception temp) {
            temp.printStackTrace();
        }
    }

    //Funkcja wyłączająca dźwięki
    public void stopSound() {
        if (sound_play != null) {
            sound_play.release();
            sound_play = null;
        }
    }

}
