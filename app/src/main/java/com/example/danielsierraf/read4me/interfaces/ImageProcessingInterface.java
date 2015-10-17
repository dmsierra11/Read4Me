package com.example.danielsierraf.read4me.interfaces;

import android.graphics.Bitmap;

import com.example.danielsierraf.read4me.classes.DetectTextNative;
import com.example.danielsierraf.read4me.classes.ImageProcessing;

/**
 * Created by danielsierraf on 8/7/15.
 */
public interface ImageProcessingInterface {
    public ImageProcessing getImageProcObject();
    public DetectTextNative getDetectTextObject();
    public void notifyDetectionFinished(ImageProcessing imageProcessing, DetectTextNative textDetector);
    public void notifyOCRFinished(String text);
}
