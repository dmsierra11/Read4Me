package com.example.danielsierraf.read4me.classes;

import org.opencv.core.Rect;

/**
 * Created by danielsierraf on 10/20/15.
 */
public class Word {
    private String text;
    private Rect box;

    public Word(String text, Rect box) {
        this.text = text;
        this.box = box;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Rect getBox() {
        return box;
    }

    public void setBox(Rect box) {
        this.box = box;
    }
}
