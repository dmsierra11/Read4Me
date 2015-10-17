package com.example.danielsierraf.read4me.utils;

import android.os.Environment;

import java.util.Arrays;
import java.util.List;

/**
 * Created by danielsierraf on 8/15/15.
 */
public class AppConstant {
    // Number of columns of Grid View
    public static final int NUM_OF_COLUMNS = 3;

    // Gridview image padding
    public static final int GRID_PADDING = 8; // in dp

    // SD card image directory
    public static final String APP_NAME = "Read4Me";
    public static final String TEST_PATH = new FileHandler().getExternalStorageDir(APP_NAME)
            .getAbsolutePath();
    public static final String PHOTO_PATH = new FileHandler().getAlbumPublicStorageDir(APP_NAME,
            "pictures").getAbsolutePath();


    // supported file formats
    public static final List<String> FILE_EXTN = Arrays.asList("jpg", "jpeg",
            "png");

    public static final int SELECT_PICTURE = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 2;
}
