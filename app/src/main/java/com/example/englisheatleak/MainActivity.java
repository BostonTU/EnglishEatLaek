package com.example.englisheatleak;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "EnglishEatLaekPrefs";
    private static final String KEY_HIGH_SCORE = "high_score";
    private static final long QUESTION_TIME_MS = 10000; // 10 วินาทีต่อคำถาม

    private CharacterController characterController;
    private RandomQuestion randomQuestion;
    private AnswerChecker answerChecker;
    private RandomQuestion.Question currentQuestion;

    private TextView tvScore;
    private TextView tvCombo;
    private TextView tvHighScore;

    private ProgressBar progressBarTimer;
    private TextView tvTimer;

    private TextView tvDifficulty;
    private TextView tvWord;
    private TextView tvFatStatus;

    private LinearLayout layoutGameOver;
    private TextView tvGameOverTitle;
    private TextView tvGameOverReason;
    private TextView tvFinalStats;
    private Button btnRestart;
    private Button[] choiceButtons;

    private CountDownTimer countDownTimer;
    private long remainingTimeMs = QUESTION_TIME_MS;
    private int highScore = 0;
    private boolean isGameOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ImageView characterView = findViewById(R.id.characterView);
        characterController = new CharacterController(characterView, findViewById(R.id.shadowView));

        tvScore = findViewById(R.id.tvScore);
        tvCombo = findViewById(R.id.tvCombo);
        tvHighScore = findViewById(R.id.tvHighScore);

        progressBarTimer = findViewById(R.id.progressBarTimer);
        tvTimer = findViewById(R.id.tvTimer);

        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvWord = findViewById(R.id.tvWord);
        tvFatStatus = findViewById(R.id.tvFatStatus);

        layoutGameOver = findViewById(R.id.layoutGameOver);
        tvGameOverTitle = findViewById(R.id.tvGameOverTitle);
        tvGameOverReason = findViewById(R.id.tvGameOverReason);
        tvFinalStats = findViewById(R.id.tvFinalStats);
        btnRestart = findViewById(R.id.btnRestart);

        choiceButtons = new Button[]{
                findViewById(R.id.btnChoice1),
                findViewById(R.id.btnChoice2)
        };

        randomQuestion = new RandomQuestion(VocabularyDatabase.getAllWords());
        answerChecker = new AnswerChecker();

        loadHighScore();

        characterController.setInputStateListener(enabled -> {
            for (Button btn : choiceButtons) {
                btn.setEnabled(enabled && !isGameOver);
            }
        });

        for (int i = 0; i < choiceButtons.length; i++) {
            final int choiceIndex = i;
            choiceButtons[i].setOnClickListener(v -> handleAnswer(choiceIndex));
        }

        btnRestart.setOnClickListener(v -> restartGame());

        restartGame();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadHighScore() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        tvHighScore.setText("Best: " + highScore + " 🏆");
    }

    private void saveHighScore(int score) {
        if (score > highScore) {
            highScore = score;
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putInt(KEY_HIGH_SCORE, highScore).apply();
            tvHighScore.setText("Best: " + highScore + " 🏆");
        }
    }

    private void startTimer() {
        stopTimer();
        remainingTimeMs = QUESTION_TIME_MS;
        progressBarTimer.setProgress(100);

        countDownTimer = new CountDownTimer(QUESTION_TIME_MS, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTimeMs = millisUntilFinished;
                int progress = (int) ((millisUntilFinished * 100) / QUESTION_TIME_MS);
                progressBarTimer.setProgress(progress);
                long seconds = (millisUntilFinished / 1000) + 1;
                tvTimer.setText(seconds + "s");
            }

            @Override
            public void onFinish() {
                remainingTimeMs = 0;
                progressBarTimer.setProgress(0);
                tvTimer.setText("0s");
                handleTimeout();
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void loadNextQuestion() {
        int fatLevel = characterController.getFatLevel();
        currentQuestion = randomQuestion.generateQuestionByFatLevel(fatLevel);

        tvDifficulty.setText("LEVEL: " + currentQuestion.getDifficulty().toUpperCase());
        tvWord.setText(currentQuestion.getTargetVocab().getEnglishWord());

        List<String> choices = currentQuestion.getChoices();
        for (int i = 0; i < choiceButtons.length; i++) {
            choiceButtons[i].setText(choices.get(i));
        }

        updateFatStatusUI(fatLevel);
        updateScoreUI();
        startTimer();
    }

    private void updateFatStatusUI(int fatLevel) {
        String status;
        if (fatLevel == 0) {
            status = "ร่างตัวละคร: ปกติ (Fat Lv.0)";
        } else if (fatLevel <= 2) {
            status = "ร่างตัวละคร: เริ่มอ้วนขึ้น (Fat Lv." + fatLevel + ")";
        } else if (fatLevel <= 4) {
            status = "ร่างตัวละคร: ตัวโตสมบูรณ์! (Fat Lv." + fatLevel + ")";
        } else {
            status = "ร่างตัวละคร: อ้วนขั้นสุดยอด!! (Fat Lv." + fatLevel + ")";
        }
        tvFatStatus.setText(status);
    }

    private void handleAnswer(int selectedIndex) {
        if (isGameOver || !characterController.canAnswer()) return;

        stopTimer();
        boolean isCorrect = answerChecker.checkAnswer(currentQuestion, selectedIndex, remainingTimeMs);

        if (isCorrect) {
            characterController.onCorrectAnswer();
            int points = answerChecker.getLastEarnedPoints();
            characterController.getEffects().showCustomWord("+" + points + " PTS!", Color.parseColor("#FFE04A"));

            int streak = answerChecker.getStreak();
            if (streak > 1) {
                characterController.getEffects().showCustomWord("COMBO x" + streak + " 🔥", Color.parseColor("#FF6B6B"));
            }

            saveHighScore(answerChecker.getScore());
            updateScoreUI();
            loadNextQuestion();
        } else {
            triggerGameOver("คำตอบที่ถูกคือ: " + currentQuestion.getChoices().get(currentQuestion.getCorrectIndex()), "GAME OVER!");
        }
    }

    private void handleTimeout() {
        if (isGameOver || !characterController.canAnswer()) return;
        answerChecker.checkAnswer(currentQuestion, -1, 0);
        triggerGameOver("เวลาหมด! คำตอบที่ถูกคือ: " + currentQuestion.getChoices().get(currentQuestion.getCorrectIndex()), "TIME'S UP!");
    }

    private void triggerGameOver(String reasonMessage, String title) {
        isGameOver = true;
        stopTimer();
        characterController.onWrongAnswer();

        int score = answerChecker.getScore();
        saveHighScore(score);
        updateScoreUI();

        for (Button btn : choiceButtons) {
            btn.setEnabled(false);
        }

        tvGameOverTitle.setText(title);
        tvGameOverReason.setText(reasonMessage);
        String stats = "Final Score: " + score + " | Best: " + highScore +
                "\nAccuracy: " + answerChecker.getAccuracyPercentage() + "% (" +
                answerChecker.getCorrectCount() + "/" + answerChecker.getTotalAnswered() + ")";
        tvFinalStats.setText(stats);
        layoutGameOver.setVisibility(View.VISIBLE);
    }

    private void restartGame() {
        isGameOver = false;
        layoutGameOver.setVisibility(View.GONE);
        answerChecker.reset();
        characterController.reset();
        loadNextQuestion();
    }

    private void updateScoreUI() {
        tvScore.setText("Score: " + answerChecker.getScore());
        tvCombo.setText("Combo: " + answerChecker.getStreak() + " 🔥");
        tvHighScore.setText("Best: " + highScore + " 🏆");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        characterController.release();
    }
}