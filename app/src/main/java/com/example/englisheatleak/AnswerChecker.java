package com.example.englisheatleak;

public class AnswerChecker {
    private int score;
    private int streak;
    private int totalAnswered;
    private int correctCount;
    private int lastEarnedPoints;

    public AnswerChecker() {
        reset();
    }

    public void reset() {
        this.score = 0;
        this.streak = 0;
        this.totalAnswered = 0;
        this.correctCount = 0;
        this.lastEarnedPoints = 0;
    }

    public boolean checkAnswer(RandomQuestion.Question question, int selectedIndex) {
        return checkAnswer(question, selectedIndex, 0);
    }

    public boolean checkAnswer(RandomQuestion.Question question, int selectedIndex, long timeRemainingMs) {
        totalAnswered++;
        boolean isCorrect = (question != null && selectedIndex == question.getCorrectIndex());

        if (isCorrect) {
            correctCount++;
            streak++;
            // score = จำนวนคำที่ตอบถูก (ตอบถูก 1 คำ = +1) ไม่มีโบนัสคอมโบ/เวลาแล้ว
            lastEarnedPoints = 1;
            score += lastEarnedPoints;
        } else {
            streak = 0;
            lastEarnedPoints = 0;
        }

        return isCorrect;
    }

    public boolean checkAnswer(RandomQuestion.Question question, String selectedMeaning) {
        int index = question != null && question.getChoices() != null ? question.getChoices().indexOf(selectedMeaning) : -1;
        return checkAnswer(question, index, 0);
    }

    public int getScore() { return score; }
    public int getStreak() { return streak; }
    public int getTotalAnswered() { return totalAnswered; }
    public int getCorrectCount() { return correctCount; }
    public int getLastEarnedPoints() { return lastEarnedPoints; }
}