package com.example.englisheatleak;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final long QUESTION_TIME_MS = 10000; // 10 วินาทีต่อคำถาม

    private enum GameState {
        START_MENU,
        PLAYING,
        PAUSED,
        GAME_OVER
    }

    private GameState currentState = GameState.START_MENU;

    private DataManager dataManager;
    private SoundManager soundManager;

    private CharacterController characterController;
    private RandomQuestion randomQuestion;
    private AnswerChecker answerChecker;
    private RandomQuestion.Question currentQuestion;

    // Main Game Views
    private TextView tvScore;
    private TextView tvCombo;
    private TextView tvHighScore;
    private ProgressBar progressBarTimer;
    private TextView tvTimer;
    private TextView tvDifficulty;
    private TextView tvWord;
    private TextView tvFatStatus;
    private Button[] choiceButtons;

    // Start Screen Views
    private LinearLayout layoutStartScreen;
    private TextView tvStartHighScore;
    private Button btnStartGame;
    private Button btnSettings;
    private Button btnResetHighScore;

    // Pause Overlay Views
    private LinearLayout layoutPauseOverlay;
    private Button btnResumeGame;
    private Button btnPauseQuitMenu;

    // Game Over Views
    private LinearLayout layoutGameOver;
    private TextView tvGameOverTitle;
    private TextView tvGameOverReason;
    private TextView tvFinalStats;
    private Button btnRestart;
    private Button btnGameOverMenu;

    private CountDownTimer countDownTimer;
    private long remainingTimeMs = QUESTION_TIME_MS;
    private int highScore = 0;
    private boolean isProcessingAnswer = false;
    private long lastBackPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dataManager = DataManager.getInstance(this);
        soundManager = SoundManager.getInstance(this);

        ImageView characterView = findViewById(R.id.characterView);
        characterController = new CharacterController(characterView, findViewById(R.id.shadowView));

        // Game Header & Content Views
        tvScore = findViewById(R.id.tvScore);
        tvCombo = findViewById(R.id.tvCombo);
        tvHighScore = findViewById(R.id.tvHighScore);
        progressBarTimer = findViewById(R.id.progressBarTimer);
        tvTimer = findViewById(R.id.tvTimer);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvWord = findViewById(R.id.tvWord);
        tvFatStatus = findViewById(R.id.tvFatStatus);

        choiceButtons = new Button[]{
                findViewById(R.id.btnChoice1),
                findViewById(R.id.btnChoice2)
        };

        // Start Screen Views
        layoutStartScreen = findViewById(R.id.layoutStartScreen);
        tvStartHighScore = findViewById(R.id.tvStartHighScore);
        btnStartGame = findViewById(R.id.btnStartGame);
        btnSettings = findViewById(R.id.btnSettings);
        btnResetHighScore = findViewById(R.id.btnResetHighScore);

        // Pause Overlay Views
        layoutPauseOverlay = findViewById(R.id.layoutPauseOverlay);
        btnResumeGame = findViewById(R.id.btnResumeGame);
        btnPauseQuitMenu = findViewById(R.id.btnPauseQuitMenu);

        // Game Over Views
        layoutGameOver = findViewById(R.id.layoutGameOver);
        tvGameOverTitle = findViewById(R.id.tvGameOverTitle);
        tvGameOverReason = findViewById(R.id.tvGameOverReason);
        tvFinalStats = findViewById(R.id.tvFinalStats);
        btnRestart = findViewById(R.id.btnRestart);
        btnGameOverMenu = findViewById(R.id.btnGameOverMenu);

        randomQuestion = new RandomQuestion(VocabularyDatabase.getAllWords());
        answerChecker = new AnswerChecker();

        loadHighScore();

        characterController.setInputStateListener(enabled -> {
            boolean canClick = enabled && currentState == GameState.PLAYING && !isProcessingAnswer;
            for (Button btn : choiceButtons) {
                btn.setEnabled(canClick);
            }
        });

        for (int i = 0; i < choiceButtons.length; i++) {
            final int choiceIndex = i;
            choiceButtons[i].setOnClickListener(v -> {
                soundManager.playButtonClickSound();
                handleAnswer(choiceIndex);
            });
        }

        btnStartGame.setOnClickListener(v -> {
            soundManager.playButtonClickSound();
            startGame();
        });

        btnSettings.setOnClickListener(v -> {
            soundManager.playButtonClickSound();
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        btnResetHighScore.setOnClickListener(v -> {
            soundManager.playButtonClickSound();
            showResetHighScoreConfirmationDialog();
        });

        btnResumeGame.setOnClickListener(v -> {
            soundManager.playButtonClickSound();
            resumeGame();
        });

        btnPauseQuitMenu.setOnClickListener(v -> {
            soundManager.playButtonClickSound();
            showStartScreen();
        });

        btnRestart.setOnClickListener(v -> {
            soundManager.playButtonClickSound();
            restartGame();
        });

        btnGameOverMenu.setOnClickListener(v -> {
            soundManager.playButtonClickSound();
            showStartScreen();
        });

        setupBackNavigation();
        showStartScreen();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentState == GameState.START_MENU) {
                    if (System.currentTimeMillis() - lastBackPressedTime < 2000) {
                        finish();
                    } else {
                        lastBackPressedTime = System.currentTimeMillis();
                        Toast.makeText(MainActivity.this, getString(R.string.toast_double_back_exit), Toast.LENGTH_SHORT).show();
                    }
                } else if (currentState == GameState.PLAYING) {
                    pauseGame();
                } else if (currentState == GameState.PAUSED || currentState == GameState.GAME_OVER) {
                    showStartScreen();
                }
            }
        });
    }

    private void loadHighScore() {
        highScore = dataManager.getHighScore();
        updateHighScoreDisplay();
    }

    private void updateHighScoreDisplay() {
        tvHighScore.setText(getString(R.string.best_score_format, highScore));
        tvStartHighScore.setText(getString(R.string.start_best_score_format, highScore));
    }

    private void saveHighScore(int score) {
        if (dataManager.saveHighScore(score)) {
            highScore = dataManager.getHighScore();
            updateHighScoreDisplay();
        }
    }

    private void showResetHighScoreConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_reset_title))
                .setMessage(getString(R.string.confirm_reset_message))
                .setPositiveButton(getString(R.string.confirm_yes), (dialog, which) -> {
                    dataManager.resetHighScore();
                    dataManager.clearSavedGameState();
                    loadHighScore();
                    soundManager.playWrongSound();
                    Toast.makeText(MainActivity.this, getString(R.string.toast_reset_success), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.confirm_cancel), null)
                .show();
    }

    private void showStartScreen() {
        stopTimer();
        currentState = GameState.START_MENU;
        isProcessingAnswer = false;
        dataManager.clearSavedGameState();
        layoutStartScreen.setVisibility(View.VISIBLE);
        layoutPauseOverlay.setVisibility(View.GONE);
        layoutGameOver.setVisibility(View.GONE);
        loadHighScore();
    }

    private void startGame() {
        dataManager.clearSavedGameState();
        layoutStartScreen.setVisibility(View.GONE);
        layoutPauseOverlay.setVisibility(View.GONE);
        layoutGameOver.setVisibility(View.GONE);
        restartGame();
    }

    private void startTimer(long durationMs) {
        stopTimer();
        remainingTimeMs = durationMs;

        countDownTimer = new CountDownTimer(durationMs, 50) {
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

    private void pauseGame() {
        if (currentState == GameState.PLAYING) {
            stopTimer();
            currentState = GameState.PAUSED;
            saveCurrentGameState();
            layoutPauseOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void saveCurrentGameState() {
        int wordId = (currentQuestion != null && currentQuestion.getTargetVocab() != null) ? currentQuestion.getTargetVocab().getId() : -1;
        dataManager.saveGameState(
                answerChecker.getScore(),
                answerChecker.getStreak(),
                characterController.getFatLevel(),
                wordId,
                remainingTimeMs
        );
    }

    private void resumeGame() {
        if (currentState == GameState.PAUSED) {
            layoutPauseOverlay.setVisibility(View.GONE);
            currentState = GameState.PLAYING;
            isProcessingAnswer = false;
            startTimer(remainingTimeMs > 0 ? remainingTimeMs : QUESTION_TIME_MS);
        }
    }

    private void loadNextQuestion() {
        int fatLevel = characterController.getFatLevel();
        currentQuestion = randomQuestion.generateQuestionByFatLevel(fatLevel);

        tvDifficulty.setText(getString(R.string.level_format, currentQuestion.getDifficulty().toUpperCase()));
        tvWord.setText(currentQuestion.getTargetVocab().getEnglishWord());

        List<String> choices = currentQuestion.getChoices();
        for (int i = 0; i < choiceButtons.length; i++) {
            choiceButtons[i].setText(choices.get(i));
        }

        updateFatStatusUI(fatLevel);
        updateScoreUI();
        isProcessingAnswer = false;
        startTimer(QUESTION_TIME_MS);
    }

    private void updateFatStatusUI(int fatLevel) {
        String status;
        if (fatLevel == 0) {
            status = getString(R.string.fat_status_0);
        } else if (fatLevel <= 2) {
            status = getString(R.string.fat_status_low, fatLevel);
        } else if (fatLevel <= 4) {
            status = getString(R.string.fat_status_mid, fatLevel);
        } else {
            status = getString(R.string.fat_status_high, fatLevel);
        }
        tvFatStatus.setText(status);
    }

    private void handleAnswer(int selectedIndex) {
        if (currentState != GameState.PLAYING || isProcessingAnswer || !characterController.canAnswer()) {
            return;
        }

        isProcessingAnswer = true;
        stopTimer();
        for (Button btn : choiceButtons) {
            btn.setEnabled(false);
        }

        boolean isCorrect = answerChecker.checkAnswer(currentQuestion, selectedIndex, remainingTimeMs);

        if (isCorrect) {
            soundManager.playCorrectSound();
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
            soundManager.playWrongSound();
            triggerGameOver("คำตอบที่ถูกคือ: " + currentQuestion.getChoices().get(currentQuestion.getCorrectIndex()), getString(R.string.game_over_title));
        }
    }

    private void handleTimeout() {
        if (currentState != GameState.PLAYING || isProcessingAnswer || !characterController.canAnswer()) {
            return;
        }
        isProcessingAnswer = true;
        soundManager.playWrongSound();
        answerChecker.checkAnswer(currentQuestion, -1, 0);
        triggerGameOver("เวลาหมด! คำตอบที่ถูกคือ: " + currentQuestion.getChoices().get(currentQuestion.getCorrectIndex()), getString(R.string.times_up_title));
    }

    private void triggerGameOver(String reasonMessage, String title) {
        currentState = GameState.GAME_OVER;
        stopTimer();
        dataManager.clearSavedGameState();
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
                "\nCorrect Answers: " + answerChecker.getCorrectCount() + "/" + answerChecker.getTotalAnswered();
        tvFinalStats.setText(stats);
        layoutGameOver.setVisibility(View.VISIBLE);
    }

    private void restartGame() {
        currentState = GameState.PLAYING;
        dataManager.clearSavedGameState();
        layoutStartScreen.setVisibility(View.GONE);
        layoutPauseOverlay.setVisibility(View.GONE);
        layoutGameOver.setVisibility(View.GONE);
        answerChecker.reset();
        characterController.reset();
        loadNextQuestion();
    }

    private void updateScoreUI() {
        tvScore.setText(getString(R.string.score_format, answerChecker.getScore()));
        tvCombo.setText(getString(R.string.combo_format, answerChecker.getStreak()));
        tvHighScore.setText(getString(R.string.best_score_format, highScore));
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHighScore();
        if (currentState == GameState.PAUSED) {
            layoutPauseOverlay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        characterController.release();
    }
}
