/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_stayfprod_utter_service_VoiceController */

#ifndef _Included_com_stayfprod_utter_service_VoiceController
#define _Included_com_stayfprod_utter_service_VoiceController
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_stayfprod_utter_service_VoiceController
 * Method:    isOpusFile
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_stayfprod_utter_service_VoiceController_isOpusFile
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_stayfprod_utter_service_VoiceController
 * Method:    openOpusFile
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_stayfprod_utter_service_VoiceController_openOpusFile
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_stayfprod_utter_service_VoiceController
 * Method:    closeOpusFile
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_stayfprod_utter_service_VoiceController_closeOpusFile
  (JNIEnv *, jobject);

/*
 * Class:     com_stayfprod_utter_service_VoiceController
 * Method:    readOpusFile
 * Signature: (Ljava/nio/ByteBuffer;I)V
 */
JNIEXPORT void JNICALL Java_com_stayfprod_utter_service_VoiceController_readOpusFile
  (JNIEnv *, jobject, jobject, jint);

/*
 * Class:     com_stayfprod_utter_service_VoiceController
 * Method:    getFinished
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_stayfprod_utter_service_VoiceController_getFinished
  (JNIEnv *, jobject);

/*
 * Class:     com_stayfprod_utter_service_VoiceController
 * Method:    getSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_stayfprod_utter_service_VoiceController_getSize
  (JNIEnv *, jobject);

/*
 * Class:     com_stayfprod_utter_service_VoiceController
 * Method:    getPcmOffset
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_stayfprod_utter_service_VoiceController_getPcmOffset
  (JNIEnv *, jobject);

/*
 * Class:     com_stayfprod_utter_service_VoiceController
 * Method:    getTotalPcmDuration
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_stayfprod_utter_service_VoiceController_getTotalPcmDuration
  (JNIEnv *, jobject);

/*
 * Class:     com_stayfprod_utter_service_VoiceController
 * Method:    startRecord
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_stayfprod_utter_service_VoiceController_startRecord
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_stayfprod_utter_service_VoiceController
 * Method:    writeFrame
 * Signature: (Ljava/nio/ByteBuffer;I)I
 */
JNIEXPORT jint JNICALL Java_com_stayfprod_utter_service_VoiceController_writeFrame
  (JNIEnv *, jobject, jobject, jint);

/*
 * Class:     com_stayfprod_utter_service_VoiceController
 * Method:    stopRecord
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_stayfprod_utter_service_VoiceController_stopRecord
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
