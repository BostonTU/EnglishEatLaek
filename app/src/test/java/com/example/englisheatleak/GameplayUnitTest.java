package com.example.englisheatleak;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class GameplayUnitTest {

    private AnswerChecker answerChecker;
    private RandomQuestion randomQuestion;

    @Before
    public void setUp() {
        answerChecker = new AnswerChecker();
        List<Vocabulary> vocabList = VocabularyDatabase.getAllWords();
        randomQuestion = new RandomQuestion(vocabList);
    }

    @Test
    public void testCorrectAnswerCalculation() {
        RandomQuestion.Question question = randomQuestion.generateQuestion();
        boolean result = answerChecker.checkAnswer(question, question.getCorrectIndex(), 5000);

        assertTrue(result);
        assertEquals(1, answerChecker.getStreak());
        assertEquals(1, answerChecker.getCorrectCount());
        assertEquals(1, answerChecker.getTotalAnswered());
        // Base score 10 + Time bonus 10 = 20
        assertEquals(20, answerChecker.getScore());
        assertEquals(20, answerChecker.getLastEarnedPoints());
        assertEquals(100, answerChecker.getAccuracyPercentage());
    }

    @Test
    public void testComboMultiplier() {
        RandomQuestion.Question q1 = randomQuestion.generateQuestion();
        RandomQuestion.Question q2 = randomQuestion.generateQuestion();

        answerChecker.checkAnswer(q1, q1.getCorrectIndex(), 0); // 10 pts
        assertEquals(1, answerChecker.getStreak());

        answerChecker.checkAnswer(q2, q2.getCorrectIndex(), 0); // 10 base + 3 combo = 13 pts
        assertEquals(2, answerChecker.getStreak());
        assertEquals(23, answerChecker.getScore());
    }

    @Test
    public void testWrongAnswerResetsStreak() {
        RandomQuestion.Question q1 = randomQuestion.generateQuestion();
        answerChecker.checkAnswer(q1, q1.getCorrectIndex(), 0);
        assertEquals(1, answerChecker.getStreak());

        int wrongIndex = (q1.getCorrectIndex() == 0) ? 1 : 0;
        boolean result = answerChecker.checkAnswer(q1, wrongIndex, 0);

        assertFalse(result);
        assertEquals(0, answerChecker.getStreak());
        assertEquals(1, answerChecker.getCorrectCount());
        assertEquals(2, answerChecker.getTotalAnswered());
        assertEquals(50, answerChecker.getAccuracyPercentage());
    }

    @Test
    public void testQuestionLevelScaling() {
        RandomQuestion.Question easyQ = randomQuestion.generateQuestionByFatLevel(0);
        assertEquals("Easy", easyQ.getDifficulty());

        RandomQuestion.Question medQ = randomQuestion.generateQuestionByFatLevel(3);
        assertEquals("Medium", medQ.getDifficulty());

        RandomQuestion.Question hardQ = randomQuestion.generateQuestionByFatLevel(6);
        assertEquals("Hard", hardQ.getDifficulty());
    }
}