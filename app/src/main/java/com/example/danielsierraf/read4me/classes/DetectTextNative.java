package com.example.danielsierraf.read4me.classes;

import android.content.res.AssetManager;

public class DetectTextNative {
	
    static {
        System.loadLibrary("opencv_java");
        System.loadLibrary("lept");
        System.loadLibrary("tess");
		System.loadLibrary("run_detection");
	}
	
	private long detectPtr = 0;
	
    public DetectTextNative(AssetManager am) {
    	detectPtr = create();
    }

	private native long create();
	private native void destroy(long detectPtr);
    private native int[] getBoundingBoxes(long detectPtr);
    private native int[] getBoxesWords(long detectPtr);
    private native void detect(long detectPtr, long matAddress);
    private native void read(long detectPtr, String lang);

	@Override
    public void finalize() throws Throwable {
		if(detectPtr != 0) {
			destroy(detectPtr);
		}
		super.finalize();
	}

    public int[] getBoundingBoxes() {
        return getBoundingBoxes(detectPtr);
    }

    public int[] getBoxesWords() {
        return getBoxesWords(detectPtr);
    }

    public void detect(long matAddress){
        detect(detectPtr, matAddress);
    }

    public void read(String lang){  read(detectPtr, lang); }

}