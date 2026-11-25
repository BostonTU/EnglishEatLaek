package com.example.englisheatleak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomQuestion {
    private final List<Vocabulary> allVocabList;
    private final Random random;

    // เก็บ id คำที่ถูกถามไปแล้วในรอบนี้ (กันข้อซ้ำ)
    private final Set<Integer> usedIds = new HashSet<>();
    private int lastTargetId = -1;

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

    /** เรียกตอนเริ่มเกมใหม่ เพื่อล้างประวัติคำที่เคยถามแล้ว */
    public void resetHistory() {
        usedIds.clear();
        lastTargetId = -1;
    }

    public Question generateQuestion() {
        return generateQuestionByDifficulty(null);
    }

    /** Rebuilds a question for the saved vocabulary item after rotation. */
    public Question generateQuestionForWordId(int wordId) {
        Vocabulary target = VocabularyDatabase.getWordById(wordId);
        if (target == null) return generateQuestion();

        List<String> choices = new ArrayList<>();
        String correctAnswer = target.getThaiMeaning();
        choices.add(correctAnswer);
        List<Vocabulary> wrongChoicePool = new ArrayList<>(allVocabList);
        wrongChoicePool.remove(target);
        Collections.shuffle(wrongChoicePool, random);
        for (Vocabulary vocabulary : wrongChoicePool) {
            if (!choices.contains(vocabulary.getThaiMeaning())) {
                choices.add(vocabulary.getThaiMeaning());
            }
            if (choices.size() == 2) break;
        }
        Collections.shuffle(choices, random);
        return new Question(target, choices, choices.indexOf(correctAnswer));
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

        Vocabulary targetVocab = pickUnusedWord(pool);
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

    /**
     * สุ่มคำที่ "ยังไม่เคยถาม" จาก pool
     * ถ้าหมดแล้วค่อยล้างประวัติของ pool นั้น (แต่ไม่ให้ซ้ำกับข้อล่าสุดติดกัน)
     */
    private Vocabulary pickUnusedWord(List<Vocabulary> pool) {
        List<Vocabulary> candidates = new ArrayList<>();
        for (Vocabulary v : pool) {
            if (!usedIds.contains(v.getId())) {
                candidates.add(v);
            }
        }

        if (candidates.isEmpty()) {
            // ถามครบทุกคำใน pool แล้ว -> เริ่มรอบใหม่เฉพาะ pool นี้
            for (Vocabulary v : pool) {
                usedIds.remove(v.getId());
            }
            for (Vocabulary v : pool) {
                if (v.getId() != lastTargetId) {
                    candidates.add(v);
                }
            }
            if (candidates.isEmpty()) {
                candidates.addAll(pool);
            }
        }

        Vocabulary picked = candidates.get(random.nextInt(candidates.size()));
        usedIds.add(picked.getId());
        lastTargetId = picked.getId();
        return picked;
    }
}
