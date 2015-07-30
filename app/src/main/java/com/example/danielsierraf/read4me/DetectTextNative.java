package com.example.danielsierraf.read4me;

import android.content.res.AssetManager;

public class DetectTextNative {
	
    static {
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
    private native void detect(long detectPtr, long matAddress);
    private native String read(long detectPtr, String lang);

	@Override
	protected void finalize() throws Throwable {
		if(detectPtr != 0) {
			destroy(detectPtr);
		}
		super.finalize();
	}

    public int[] getBoundingBoxes() {
        return getBoundingBoxes(detectPtr);
    }

    public void detect(long matAddress){
        detect(detectPtr, matAddress);
    }

    public String read(String lang){
        return read(detectPtr, lang);
    }

}