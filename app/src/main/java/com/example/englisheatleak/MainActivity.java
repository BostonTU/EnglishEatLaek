package com.example.englisheatleak;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private CharacterController characterController;
    private RandomQuestion randomQuestion;
    private AnswerChecker answerChecker;
    private RandomQuestion.Question currentQuestion;

    private TextView tvWord;
    private TextView tvScore;
    private Button btnRestart;
    private Button[] choiceButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ImageView characterView = findViewById(R.id.characterView);
        characterController = new CharacterController(characterView, findViewById(R.id.shadowView));

        tvWord = findViewById(R.id.tvWord);
        tvScore = findViewById(R.id.tvScore);
        btnRestart = findViewById(R.id.btnRestart);
        choiceButtons = new Button[]{
                findViewById(R.id.btnChoice1),
                findViewById(R.id.btnChoice2)
        };

        randomQuestion = new RandomQuestion(VocabularyDatabase.getAllWords());
        answerChecker = new AnswerChecker();

        characterController.setInputStateListener(enabled -> {
            for (Button btn : choiceButtons) {
                btn.setEnabled(enabled);
            }
        });

        for (int i = 0; i < choiceButtons.length; i++) {
            final int choiceIndex = i;
            choiceButtons[i].setOnClickListener(v -> handleAnswer(choiceIndex));
        }

        btnRestart.setOnClickListener(v -> restartGame());

        loadNextQuestion();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadNextQuestion() {
        currentQuestion = randomQuestion.generateQuestion();
        tvWord.setText(currentQuestion.getTargetVocab().getEnglishWord());

        List<String> choices = currentQuestion.getChoices();
        for (int i = 0; i < choiceButtons.length; i++) {
            choiceButtons[i].setText(choices.get(i));
        }
        updateScoreUI();
    }

    private void handleAnswer(int selectedIndex) {
        boolean isCorrect = answerChecker.checkAnswer(currentQuestion, selectedIndex);

        if (isCorrect) {
            characterController.onCorrectAnswer();
            updateScoreUI();
            loadNextQuestion();
        } else {
            characterController.onWrongAnswer();
            updateScoreUI();
            String correctMeaning = currentQuestion.getChoices().get(currentQuestion.getCorrectIndex());
            tvWord.setText("Game Over!\nคำตอบที่ถูกคือ: " + correctMeaning);
            btnRestart.setVisibility(View.VISIBLE);
        }
    }

    private void restartGame() {
        btnRestart.setVisibility(View.GONE);
        answerChecker.reset();
        characterController.reset();
        loadNextQuestion();
    }

    private void updateScoreUI() {
        tvScore.setText("Score: " + answerChecker.getScore() + " | Combo: " + answerChecker.getStreak());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        characterController.release();
    }
}