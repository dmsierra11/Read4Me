package com.example.danielsierraf.read4me.classes;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Created by danielsierraf on 4/20/15.
 */
public class OCR {
    private static final String TAG = "OCR";
    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/Read4Me/";

    // You should have the trained data file in assets folder
    // You can get them at:
    // http://code.google.com/p/tesseract-ocr/downloads/list
    private String lang;
    private final int COARSE = 1;
    private final int FINE = 2;

    public OCR(){
        lang = "eng";
    }

    public OCR(String _lang){
        lang = _lang;
    }

    public void setLanguage(String _lang){
        lang = _lang;
    }

    public String getLanguage(){
        return lang;
    }

    public String recognizeText(Bitmap bitmap){
        String recognizedText = "";
        String output = "";
        try {
            Log.d(TAG, "Before baseApi");

            TessBaseAPI baseApi = new TessBaseAPI();
            baseApi.setDebug(true);
            //baseApi.init(DATA_PATH, lang, 2);
            baseApi.init(DATA_PATH, lang);
            //set whitelist
            baseApi.setPageSegMode(TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED);
            baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);
            baseApi.setImage(bitmap);

            recognizedText = baseApi.getUTF8Text();
            Log.d(TAG, "RECOGNIZED TEXT: "+recognizedText);
            //spell check
            String[] tmp = recognizedText.split(" ");
            Log.d(TAG, "Words: "+Arrays.toString(tmp));
            for (int i = 0; i < tmp.length; i++){
                Log.d(TAG, "Spell checking: "+tmp[i]);
                String word = spellCheck(tmp[i], 0);
                Log.d(TAG, "RECOGNIZED TEXT FILTERED: "+word);
                output += word;
            }

            baseApi.end();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, ex.getMessage());
            // TODO: handle exception
        }

        return output;
    }

    public String spellCheck(String str, int method) {
        Log.d(TAG, "string: " + str);
        int letterCount = 0, errorCount = 0, lNoiseCount = 0, digitCount = 0, result_ = COARSE;
        String withoutStrangeMarks = "", output = "";
        float score = 0;
        str = str.trim();
        Log.d(TAG, "Recorriendo palabra");
        for (int i = 0; i < str.length(); i++) {
            //si es una letra
            if (Character.isLetter(str.charAt(i))) {
                Log.d(TAG, "Es letra");
                //se van limpiando símbolos que pueden haber sido confundidos con l, L, i o I
                withoutStrangeMarks += str.charAt(i);
                letterCount++;
                if (str.charAt(i) == 'l' || str.charAt(i) == 'L' || str.charAt(i) == 'I')
                    lNoiseCount++;
            } else if (Character.isDigit(str.charAt(i))) {
                Log.d(TAG, "Es digito");
                digitCount++;
                withoutStrangeMarks += str.charAt(i);
            } else if (str.charAt(i) == '|' || str.charAt(i) == '/' || str.charAt(i) == '\\') {
                //si es un símbolo como |, \ o \ y esta al lado de un dígito, se asume que es un uno
                Log.d(TAG, "Es |, / o \\");
                if ((Character.isDigit(str.charAt(i - 1)))
                        || ((i < str.length() - 1) && Character.isDigit(str.charAt(i + 1)))) {
                    Log.d(TAG, "Es |, / o \\ y tiene numeros al lado");
                    withoutStrangeMarks += '1';
                    //str[i] = '1';
                    str = str.substring(0, i - 1) + '1' + str.substring(i + 1, str.length() - 1);
                    digitCount++;
                } else {
                    Log.d(TAG, "Es una l");
                    withoutStrangeMarks += 'l';
                    errorCount++;
                    letterCount++;
                }
            } else if (str.charAt(i) == '[') {
                Log.d(TAG, "Es una L");
                withoutStrangeMarks += 'L';
                errorCount++;
                letterCount++;
            } else if (str.charAt(i) == ']') {
                Log.d(TAG, "Es una I");
                withoutStrangeMarks += 'I';
                errorCount++;
                letterCount++;
            } else {
                Log.d(TAG, "Es basura");
                withoutStrangeMarks += "";
                //str = str.substring(0, i-1) + ' ' + str.substring(i+1, str.length()-1);
            }
        }

        if (digitCount > 0 && letterCount == 0) {
            if (digitCount <= 5)
                output = str + " ";
        } else if (letterCount < 2) {
            if (result_ == FINE)
                output = str + " ";
        } else if ((errorCount + lNoiseCount) * 2 > letterCount) {
            // do nothing
        } else if (letterCount < str.length() / 2) {
            // don't show up garbbige
        }

        Log.d(TAG, "filtered: " + withoutStrangeMarks);
        output += withoutStrangeMarks + " ";

        return output;
    }
}
