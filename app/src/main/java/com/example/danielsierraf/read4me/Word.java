package com.example.danielsierraf.read4me;

/**
 * Created by danielsierraf on 7/9/15.
 */
public class Word {

    private String word;
    private float score;

    public Word(){
        this.word = "";
        this.score = 0;
    }

    public Word(String word, float score){
        this.word = word;
        this.score = score;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
