package com.example.englisheatleak;

public class AnswerChecker {
    private int score;
    private int streak;
    private int totalAnswered;
    private int correctCount;

    public AnswerChecker() {
        reset();
    }

    public void reset() {
        this.score = 0;
        this.streak = 0;
        this.totalAnswered = 0;
        this.correctCount = 0;
    }

    public boolean checkAnswer(RandomQuestion.Question question, int selectedIndex) {
        totalAnswered++;
        boolean isCorrect = (selectedIndex == question.getCorrectIndex());

        if (isCorrect) {
            correctCount++;
            streak++;
            score += 10 + (streak > 1 ? (streak - 1) * 2 : 0);
        } else {
            streak = 0;
        }

        return isCorrect;
    }

    public boolean checkAnswer(RandomQuestion.Question question, String selectedMeaning) {
        int index = question.getChoices().indexOf(selectedMeaning);
        return checkAnswer(question, index);
    }

    public int getScore() { return score; }
    public int getStreak() { return streak; }
    public int getTotalAnswered() { return totalAnswered; }
    public int getCorrectCount() { return correctCount; }
}