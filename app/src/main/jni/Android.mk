LOCAL_PATH := $(call my-dir)

#leptonica
LEPTONICA_LOCAL := $(LOCAL_PATH)/com_googlecode_leptonica_android
LEPTONICA_PATH := $(LOCAL_PATH)/src

include $(CLEAR_VARS)

LOCAL_MODULE := liblept
LOCAL_SRC_FILES := ../libs/$(TARGET_ARCH_ABI)/liblept.so
LOCAL_EXPORT_C_INCLUDES := \
  $(LEPTONICA_LOCAL) \
  $(LEPTONICA_PATH)/src

include $(PREBUILT_SHARED_LIBRARY)

#tesseract
TESSERACT_LOCAL := $(LOCAL_PATH)/com_googlecode_tesseract_android
TESSERACT_PATH := $(TESSERACT_LOCAL)/src

include $(CLEAR_VARS)

LOCAL_MODULE := libtess
LOCAL_SRC_FILES := ../libs/$(TARGET_ARCH_ABI)/libtess.so
LOCAL_EXPORT_C_INCLUDES := \
  $(LOCAL_PATH) \
  $(TESSERACT_PATH)/api \
  $(TESSERACT_PATH)/ccmain \
  $(TESSERACT_PATH)/ccstruct \
  $(TESSERACT_PATH)/ccutil \
  $(TESSERACT_PATH)/classify \
  $(TESSERACT_PATH)/cube \
  $(TESSERACT_PATH)/cutil \
  $(TESSERACT_PATH)/dict \
  $(TESSERACT_PATH)/opencl \
  $(TESSERACT_PATH)/neural_networks/runtime \
  $(TESSERACT_PATH)/textord \
  $(TESSERACT_PATH)/viewer \
  $(TESSERACT_PATH)/wordrec \
  $(LEPTONICA_PATH)/src \
  $(TESSERACT_LOCAL)
LOCAL_SHARED_LIBRARIES := liblept

include $(PREBUILT_SHARED_LIBRARY)

#opencv
include $(CLEAR_VARS)

OPENCV_PACKAGE_DIR:= /Users/danielsierraf/Documents/OpenCV-2.4.10-android-sdk/sdk
OPENCV_LIB_TYPE:=SHARED
OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES := off
include $(OPENCV_PACKAGE_DIR)/native/jni/OpenCV.mk

LOCAL_MODULE    := run_detection
LOCAL_SRC_FILES := text_detect.cpp android.cpp
LOCAL_LDLIBS    += -ldl -lm -llog -landroid
LOCAL_SHARED_LIBRARIES += libtess liblept

include $(BUILD_SHARED_LIBRARY)