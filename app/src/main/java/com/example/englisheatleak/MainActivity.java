package com.example.englisheatleak;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
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

    private static final long WARNING_MS = 3000; // เตือนเมื่อเหลือ 3 วินาที
    private boolean timerWarning = false;
    private long lastWarnSecond = -1;
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
    private ImageView imgMenuArrow;
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
        ThemeManager.applySavedTheme(this);

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
        imgMenuArrow = findViewById(R.id.imgMenuArrow);

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
        setupMenuArrow();
        showStartScreen();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            float d = getResources().getDisplayMetrics().density;
            int pad = (int) (16 * d);
            int extraTop = (int) (12 * d);

            // เนื้อหาเกม: หลบแถบระบบ + ระยะขอบ
            findViewById(R.id.gameContent).setPadding(
                    sb.left + pad, sb.top + pad + extraTop,
                    sb.right + pad, sb.bottom + pad);

            // หน้าแรก: พื้นหลังเต็มจอ เนื้อหาหลบแถบระบบ
            findViewById(R.id.layoutStartScreen).setPadding(
                    sb.left + (int) (24 * d), sb.top + (int) (48 * d),
                    sb.right + (int) (24 * d), sb.bottom + (int) (48 * d));

            // pause overlay: พื้นดำเต็มจอ เนื้อหาหลบแถบระบบ
            findViewById(R.id.layoutPauseOverlay).setPadding(
                    sb.left + (int) (32 * d), sb.top,
                    sb.right + (int) (32 * d), sb.bottom);
            return insets;
        });
    }

    private void fadeInView(View view) {
        view.setAlpha(0f);
        view.setScaleX(0.95f);
        view.setScaleY(0.95f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(220)
                .setInterpolator(new DecelerateInterpolator())
                .start();
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

    /** ลูกศรเมนูกะพริบแบบพิกเซล (เปิด 0.5 วิ ปิด 0.5 วิ) */
    private void startArrowBlink() {
        imgMenuArrow.clearAnimation();
        AlphaAnimation blink = new AlphaAnimation(1f, 0f);
        blink.setDuration(1000);
        blink.setRepeatCount(Animation.INFINITE);
        blink.setInterpolator(t -> t < 0.5f ? 0f : 1f);
        imgMenuArrow.startAnimation(blink);
    }

    private void stopArrowBlink() {
        imgMenuArrow.clearAnimation();
    }

    /** ลูกศรเมนูย้ายไปชี้ปุ่มที่ hover / โฟกัส / แตะ */
    private void setupMenuArrow() {
        Button[] rows = {btnStartGame, btnSettings, btnResetHighScore};
        for (Button b : rows) {
            b.setOnHoverListener((v, e) -> {
                if (e.getAction() == MotionEvent.ACTION_HOVER_ENTER) pointArrowAt(v);
                return false;
            });
            b.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) pointArrowAt(v); });
            b.setOnTouchListener((v, e) -> {
                if (e.getAction() == MotionEvent.ACTION_DOWN) pointArrowAt(v);
                return false;   // ให้ click ทำงานต่อ
            });
        }
    }

    private void pointArrowAt(View target) {
        int[] a = new int[2], t = new int[2];
        imgMenuArrow.getLocationInWindow(a);
        target.getLocationInWindow(t);
        float delta = (t[1] + target.getHeight() / 2f) - (a[1] + imgMenuArrow.getHeight() / 2f);
        imgMenuArrow.animate().cancel();
        imgMenuArrow.animate().translationY(imgMenuArrow.getTranslationY() + delta)
                .setDuration(90).start();
    }

    private void showStartScreen() {
        stopTimer();
        currentState = GameState.START_MENU;
        isProcessingAnswer = false;
        dataManager.clearSavedGameState();
        fadeInView(layoutStartScreen);
        layoutPauseOverlay.setVisibility(View.GONE);
        layoutGameOver.setVisibility(View.GONE);
        imgMenuArrow.animate().cancel();
        imgMenuArrow.setTranslationY(0f);
        startArrowBlink();
        loadHighScore();
    }

    private void startGame() {
        dataManager.clearSavedGameState();
        stopArrowBlink();
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
                updateTimerWarning(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                remainingTimeMs = 0;
                progressBarTimer.setProgress(0);
                tvTimer.setText("0s");
                setTimerWarning(false);
                handleTimeout();
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        setTimerWarning(false);
    }

    private void updateTimerWarning(long msLeft) {
        boolean warn = msLeft <= WARNING_MS;
        if (warn != timerWarning) setTimerWarning(warn);
        if (!warn) return;

        // หลอดกะพริบแดงสลับสีเดิม ทุก 250 ms (เป็นขั้นแบบ pixel)
        boolean red = ((msLeft / 250) % 2) == 0;
        progressBarTimer.setProgressTintList(android.content.res.ColorStateList.valueOf(
                red ? androidx.core.content.ContextCompat.getColor(this, R.color.px_red)
                        : androidx.core.content.ContextCompat.getColor(this, R.color.px_yellow)));

        // เด้งตัวเลข + สั่น ทุกครั้งที่ขึ้นวินาทีใหม่
        long sec = msLeft / 1000;
        if (sec != lastWarnSecond) {
            lastWarnSecond = sec;
            float d = getResources().getDisplayMetrics().density;
            tvTimer.animate().cancel();
            tvTimer.setPivotX(tvTimer.getWidth());
            tvTimer.setPivotY(tvTimer.getHeight() / 2f);
            tvTimer.setScaleX(1.4f);
            tvTimer.setScaleY(1.4f);
            tvTimer.setTranslationX(3 * d);
            tvTimer.animate().scaleX(1f).scaleY(1f).translationX(0f).setDuration(250).start();
        }
    }

    private void setTimerWarning(boolean on) {
        timerWarning = on;
        lastWarnSecond = -1;
        if (!on) {
            progressBarTimer.setProgressTintList(null);
            tvTimer.animate().cancel();
            tvTimer.setScaleX(1f);
            tvTimer.setScaleY(1f);
            tvTimer.setTranslationX(0f);
        }
    }

    private void pauseGame() {
        if (currentState == GameState.PLAYING) {
            stopTimer();
            currentState = GameState.PAUSED;
            saveCurrentGameState();
            fadeInView(layoutPauseOverlay);
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

        boolean canClick = currentState == GameState.PLAYING && characterController.canAnswer();
        for (Button btn : choiceButtons) {
            btn.setEnabled(canClick);
        }

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
        triggerGameOver("คำตอบที่ถูกคือ: " + currentQuestion.getChoices().get(currentQuestion.getCorrectIndex()), getString(R.string.times_up_title));
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
        String stats = "Words: " + score + " | Best: " + highScore;
        tvFinalStats.setText(stats);
        fadeInView(layoutGameOver);
    }

    private void restartGame() {
        currentState = GameState.PLAYING;
        isProcessingAnswer = false;
        dataManager.clearSavedGameState();
        layoutStartScreen.setVisibility(View.GONE);
        layoutPauseOverlay.setVisibility(View.GONE);
        layoutGameOver.setVisibility(View.GONE);
        answerChecker.reset();
        characterController.reset();
        randomQuestion.resetHistory();
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
        stopArrowBlink();
        pauseGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHighScore();
        if (currentState == GameState.START_MENU) {
            startArrowBlink();
        }
        if (currentState == GameState.PAUSED) {
            layoutPauseOverlay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        stopArrowBlink();
        characterController.release();
    }
}
