package com.example.englisheatleak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomQuestion {
    private final List<Vocabulary> allVocabList;
    private final Random random;

    public static class Question {
        private final Vocabulary targetVocab;
        private final List<String> choices;
        private final int correctIndex;

        public Question(Vocabulary targetVocab, List<String> choices, int correctIndex) {
            this.targetVocab = targetVocab;
            this.choices = choices;
            this.correctIndex = correctIndex;
        }

        public Vocabulary getTargetVocab() { return targetVocab; }
        public List<String> getChoices() { return choices; }
        public int getCorrectIndex() { return correctIndex; }
        public String getDifficulty() { return targetVocab != null ? targetVocab.getDifficulty() : "Easy"; }
    }

    public RandomQuestion(List<Vocabulary> vocabList) {
        this.allVocabList = new ArrayList<>(vocabList);
        this.random = new Random();
    }

    public Question generateQuestion() {
        return generateQuestionByDifficulty(null);
    }

    public Question generateQuestionByFatLevel(int fatLevel) {
        String difficulty;
        if (fatLevel <= 1) {
            difficulty = "Easy";
        } else if (fatLevel <= 4) {
            difficulty = "Medium";
        } else {
            difficulty = "Hard";
        }
        return generateQuestionByDifficulty(difficulty);
    }

    public Question generateQuestionByDifficulty(String difficulty) {
        List<Vocabulary> pool;
        if (difficulty != null) {
            pool = VocabularyDatabase.getWordsByDifficulty(difficulty);
        } else {
            pool = new ArrayList<>(allVocabList);
        }

        if (pool == null || pool.size() < 2) {
            pool = new ArrayList<>(allVocabList);
        }

        int targetIdx = random.nextInt(pool.size());
        Vocabulary targetVocab = pool.get(targetIdx);
        String correctAnswer = targetVocab.getThaiMeaning();

        List<String> choices = new ArrayList<>();
        choices.add(correctAnswer);

        List<Vocabulary> wrongChoicePool = new ArrayList<>(allVocabList);
        wrongChoicePool.remove(targetVocab);
        Collections.shuffle(wrongChoicePool, random);

        for (Vocabulary v : wrongChoicePool) {
            if (!choices.contains(v.getThaiMeaning())) {
                choices.add(v.getThaiMeaning());
            }
            if (choices.size() == 2) break;
        }

        Collections.shuffle(choices, random);
        int correctIndex = choices.indexOf(correctAnswer);

        return new Question(targetVocab, choices, correctIndex);
    }
}