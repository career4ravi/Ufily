//
// Created by rtiragat on 1/26/2019.
//

#include "jni.h"
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
JNIEXPORT jstring JNICALL


Java_freeze_in_co_ufily_MainActivity_stringFromJNI( JNIEnv* env,
                                                  jobject thiz )
{
    return (*env)->NewStringUTF(env, "Hello from JNI !
    Compiled with ABI " ABI ".");
}
