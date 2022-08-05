//
// Created by User on 04/06/2021.
//

#ifndef INTERCOM_AUDIOENGINE_H
#define INTERCOM_AUDIOENGINE_H

#include <oboe/Oboe.h>
#include "Oscillator.h"

using namespace oboe;
class AudioEngine:public AudioStreamCallback{
public:
    void start();

    DataCallbackResult
    onAudioReady(AudioStream *oboeStream, void *audioData, int32_t numFrames) override;

    AudioStream *stream;

    Oscillator osc;

    void tap(bool i);

    void setFrequency(float d);
};

#endif //INTERCOM_AUDIOENGINE_H
