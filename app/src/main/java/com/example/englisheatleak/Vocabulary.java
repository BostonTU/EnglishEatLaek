
package com.example.englisheatleak;

public class Vocabulary {

    private int id;
    private String englishWord;
    private String thaiMeaning;
    private String difficulty;

    public Vocabulary(int id, String englishWord,
                      String thaiMeaning, String difficulty) {
        this.id = id;
        this.englishWord = englishWord;
        this.thaiMeaning = thaiMeaning;
        this.difficulty = difficulty;
    }

    public int getId() {
        return id;
    }

    public String getEnglishWord() {
        return englishWord;
    }

    public String getThaiMeaning() {
        return thaiMeaning;
    }

    public String getDifficulty() {
        return difficulty;
    }
}
