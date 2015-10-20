package com.example.danielsierraf.read4me.views;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;

import com.example.danielsierraf.read4me.interfaces.CustomCameraInterface;

import org.opencv.android.JavaCameraView;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NativeCameraCustomView extends JavaCameraView implements PictureCallback {

    private static final String TAG = "NativeCameraCustomView";
    private String mPictureFileName;
    private CustomCameraInterface mCallback;

    public NativeCameraCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "Native view instantstiated");
    }

    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }

    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }

    public boolean isFocusModeSupported(){
        return (mCamera.getParameters().getFocusMode() != null);
    }

    public boolean isPictureSizeSupported() {
        return (mCamera.getParameters().getSupportedPictureSizes() != null);
    }

    public String getEffect() {
        return mCamera.getParameters().getColorEffect();
    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }

    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public List<String> getFocusModeSupported() {
        return mCamera.getParameters().getSupportedFocusModes();
    }

    public void setFocusMode(String focusMode){
        Camera.Parameters params = mCamera.getParameters();
        Log.d(TAG, "Focus mode: "+focusMode);
        params.setFocusMode(focusMode);
        mCamera.setParameters(params);
    }

    public void setResolution(Size resolution) {
        Camera.Parameters params = mCamera.getParameters();
        List<Size> supported_sizes = params.getSupportedPreviewSizes();
        List<String> sizes = new ArrayList<>();
        for (Size size: supported_sizes){
            String preview_size = size.width+"x"+size.height;
            sizes.add(preview_size);
        }
        Log.d(TAG, "Supported preview sizes: " + Arrays.toString(sizes.toArray()));

        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }

    public Size findBestSize() {
        Camera.Parameters parameters = mCamera.getParameters();

        List<Camera.Size> supportedSizes = parameters
                .getSupportedPreviewSizes();

        Camera.Size bestSize = supportedSizes.remove(0);

        for (Camera.Size size : supportedSizes) {
            if ((size.width * size.height) > (bestSize.width * bestSize.height)) {
                bestSize = size;
            }
        }

        return bestSize;
    }

    public void setFlash(){
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(params);
        Log.d(TAG, "Flash on");
    }

    public void setPictureSize(){
        Camera.Parameters params = mCamera.getParameters();

        List<Size> supported_sizes = params.getSupportedPictureSizes();
        List<String> sizes = new ArrayList<>();
        for (Size size: supported_sizes){
            String picture_size = size.width+"x"+size.height;
            sizes.add(picture_size);
        }
        Log.d(TAG, "Supported picture sizes: " + Arrays.toString(sizes.toArray()));

        //Size smallest = supported_sizes.get(supported_sizes.size()-2);
        Size biggest = supported_sizes.get(0);
        Log.d(TAG, "Image picture size:" +biggest.width+"x"+biggest.height);
        params.setPictureSize(biggest.width, biggest.height);
        mCamera.setParameters(params);
        setResolution(biggest);
    }

    public void takePicture(final String fileName, CustomCameraInterface mCallbackPic) {
        Log.i(TAG, "Taking picture");
        this.mPictureFileName = fileName;
        mCallback = mCallbackPic;
        // Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
        // Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
        mCamera.setPreviewCallback(null);

        // PictureCallback is implemented by the current class
        mCamera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Saving a bitmap to file");
        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        // Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFileName);

            fos.write(data);
            fos.close();
            mCallback.notifyPictureTaken(mPictureFileName);

        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }

    }
}
