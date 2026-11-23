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
    }

    public RandomQuestion(List<Vocabulary> vocabList) {
        this.allVocabList = new ArrayList<>(vocabList);
        this.random = new Random();
    }

    public Question generateQuestion() {
        if (allVocabList == null || allVocabList.size() < 2) {
            throw new IllegalStateException("ต้องมีคำศัพท์ในคลังอย่างน้อย 2 คำ");
        }

        int targetIdx = random.nextInt(allVocabList.size());
        Vocabulary targetVocab = allVocabList.get(targetIdx);
        String correctAnswer = targetVocab.getThaiMeaning();

        List<String> choices = new ArrayList<>();
        choices.add(correctAnswer);

        List<Vocabulary> pool = new ArrayList<>(allVocabList);
        pool.remove(targetIdx);
        Collections.shuffle(pool, random);

        for (Vocabulary v : pool) {
            if (!choices.contains(v.getThaiMeaning())) {
                choices.add(v.getThaiMeaning());
            }
            if (choices.size() == 2) break; // เอาแค่ 2 ช้อยส์
        }

        Collections.shuffle(choices, random);
        int correctIndex = choices.indexOf(correctAnswer);

        return new Question(targetVocab, choices, correctIndex);
    }
}