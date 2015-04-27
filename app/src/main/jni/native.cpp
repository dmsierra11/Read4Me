#include <jni.h>
#include "com_example_danielsierraf_read4me_MainActivity.h"

JNIEXPORT jstring JNICALL Java_com_example_danielsierraf_read4me_MainActivity_hello
  (JNIEnv * env, jobject obj){
    return (env)->NewStringUTF("Hello from JNI finally");
  }

