#include "text_detect.h"
#include <jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>

#define APPNAME "DetectText"
#define LOG_TAG "NativeTextDetection"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

namespace {
	DetectText* toDetectTextNative(jlong detectPtr) {
		return reinterpret_cast<DetectText*>(detectPtr);
	}
}

extern "C" {

	JNIEXPORT jlong JNICALL Java_com_example_danielsierraf_read4me_DetectTextNative_create
	    (JNIEnv* env, jobject jobj) {
	    LOGD("Create native");
		DetectText* dt = new DetectText();
		return reinterpret_cast<jlong>(dt);
	}

	JNIEXPORT void JNICALL Java_com_example_danielsierraf_read4me_DetectTextNative_destroy
	    (JNIEnv* env, jobject jobj, jlong detectPtr) {
		delete toDetectTextNative(detectPtr);
	}

	JNIEXPORT jintArray JNICALL Java_com_example_danielsierraf_read4me_DetectTextNative_getBoundingBoxes
        	    (JNIEnv* env, jobject jobj, jlong detectPtr) {

        vector<Rect> boundingBoxes = toDetectTextNative(detectPtr)->getBoundingBoxes();
        LOGD("Create new int array");
        jintArray result = env->NewIntArray(boundingBoxes.size() * 4);

        if (result == NULL) {
            return NULL;
        }

        LOGD("bounding boxes");
        jint tmp_arr[boundingBoxes.size() * 4];

        int idx = 0;
        for (int i = 0; i < boundingBoxes.size(); i++) {
            tmp_arr[idx++] = boundingBoxes[i].x;
        	tmp_arr[idx++] = boundingBoxes[i].y;
        	tmp_arr[idx++] = boundingBoxes[i].width;
        	tmp_arr[idx++] = boundingBoxes[i].height;
        }

        env->SetIntArrayRegion(result, 0, boundingBoxes.size() * 4, tmp_arr);
        LOGD("Return result");
        return result;
    }

	/*JNIEXPORT jintArray JNICALL Java_com_example_danielsierraf_read4me_DetectTextNative_getBoundingBoxes
	    (JNIEnv* env, jobject jobj, jlong detectPtr, jlong matAddress) {

        LOGD("Creating native mat address");
		Mat* nativeMat =(Mat*)matAddress;
		LOGD("Getting bounding boxes");
		vector<Rect> boundingBoxes = toDetectTextNative(detectPtr)->getBoundingBoxes(*nativeMat);
        LOGD("Create new int array");
		jintArray result = env->NewIntArray(boundingBoxes.size() * 4);

		if (result == NULL) {
			return NULL;
		}

        LOGD("bounding boxes");
		jint tmp_arr[boundingBoxes.size() * 4];

		int idx = 0;
		for (int i = 0; i < boundingBoxes.size(); i++) {
			tmp_arr[idx++] = boundingBoxes[i].x;
			tmp_arr[idx++] = boundingBoxes[i].y;
			tmp_arr[idx++] = boundingBoxes[i].width;
			tmp_arr[idx++] = boundingBoxes[i].height;
		}

		env->SetIntArrayRegion(result, 0, boundingBoxes.size() * 4, tmp_arr);
        LOGD("Return result");
	    return result;
	}*/

	JNIEXPORT void JNICALL Java_com_example_danielsierraf_read4me_DetectTextNative_detect
    	    (JNIEnv* env, jobject jobj, jlong detectPtr, jlong matAddress){
    	LOGD("Detecting...");
        Mat& mRgb = *(Mat*)matAddress;
        toDetectTextNative(detectPtr)->detect(mRgb);
        LOGD("Finish detecting");
    }

    JNIEXPORT jstring JNICALL Java_com_example_danielsierraf_read4me_DetectTextNative_read
        	    (JNIEnv* env, jobject jobj, jlong detectPtr, jstring lang){
        const char *nativeString = (env)->GetStringUTFChars(lang, 0);
        // use your string
        LOGD("Reading...");
        const char* str = toDetectTextNative(detectPtr)->read(nativeString);
        LOGD("Finish reading");
        (env)->ReleaseStringUTFChars(lang, nativeString);
        LOGD("UTF realeased");
        return (env)->NewStringUTF(str);
    }

}
