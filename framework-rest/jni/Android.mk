LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := crypto

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
    LOCAL_SRC_FILES := $(LOCAL_PATH)/../libraries/boringssl/lib/libcrypto_arm64-v8a.a
  else
    ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
        LOCAL_SRC_FILES := $(LOCAL_PATH)/../libraries/boringssl/lib/libcrypto_armeabi-v7a.a
    else
        ifeq ($(TARGET_ARCH_ABI),armeabi)
          LOCAL_SRC_FILES := $(LOCAL_PATH)/../libraries/boringssl/lib/libcrypto_armeabi.a
        else
            ifeq ($(TARGET_ARCH_ABI),x86)
              LOCAL_SRC_FILES := $(LOCAL_PATH)/../libraries/boringssl/lib/libcrypto_x86.a
            else
              ifeq ($(TARGET_ARCH_ABI),x86_64)
                LOCAL_SRC_FILES := $(LOCAL_PATH)/../libraries/boringssl/lib/libcrypto_x86_64.a
              endif
            endif
        endif
    endif
endif

include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := fmrest
LOCAL_CFLAGS += -fno-exceptions -fvisibility=hidden -ffunction-sections -fdata-sections \
    -DFM_URL_SIGN_NO_ENCODING \

LOCAL_LDFLAGS := -Wl,--build-id,--gc-sections
LOCAL_STATIC_LIBRARIES := crypto
LOCAL_LDLIBS := \
  -llog \

LOCAL_C_INCLUDES += \
    $(LOCAL_PATH)/../libraries/boringssl/include \

LOCAL_SRC_FILES += \
  ./fmrest.cpp \
  ./URLSignature.cpp \

LOCAL_MULTILIB := both

include $(BUILD_SHARED_LIBRARY)
