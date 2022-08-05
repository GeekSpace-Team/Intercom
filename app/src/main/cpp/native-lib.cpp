//
// Created by User on 04/06/2021.
//
#include <oboe/Oboe.h>
#include <math.h>
#include <jni.h>
#include "AudioEngine.h"
#include "AudioRecord.h"
AudioEngine engine;
AudioRecord record;
extern "C" {
        JNIEXPORT void JNICALL
        Java_com_shageldi_intercom_HomePage_startEngine(JNIEnv *env, jobject instance) {
           engine.start();
               // record.start();
        }

        JNIEXPORT void JNICALL
        Java_com_shageldi_intercom_HomePage_tap(JNIEnv *env, jobject instance,jboolean b) {
            engine.tap(b);
        }

        JNIEXPORT void JNICALL
        Java_com_shageldi_intercom_HomePage_setFrequency(JNIEnv *env, jobject instance,jfloat frequency) {
            //TODO
            engine.setFrequency(frequency);
        }

        JNIEXPORT void JNICALL
        Java_com_shageldi_intercom_HomePage_playStream(JNIEnv *env, jobject instance,
                                                       jbyteArray audio) {

        }
}
