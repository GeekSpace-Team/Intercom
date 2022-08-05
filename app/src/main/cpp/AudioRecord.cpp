//
// Created by User on 04/07/2021.
//
#include "AudioRecord.h"
#include "oboe/Oboe.h"
#include "../../../../oboe/src/common/OboeDebug.h"


void AudioRecord::start(){
    AudioStreamBuilder builder;
    builder.setDirection(Direction::Input);
    builder.setPerformanceMode(PerformanceMode::LowLatency);

    AudioStream *stream;
    Result r=builder.openStream(&stream);
    if(r!=Result::OK){
        LOGE("Error opening stream %s",convertToText(r));
    }
    r=stream->requestStart();
    if(r!=Result::OK){
        LOGE("Error starting stream %s",convertToText(r));
    }

    constexpr int kMillisecondsToRecord=2;

    const int32_t requestedFrames=(int32_t)(kMillisecondsToRecord*(stream->getSampleRate())/kMillisecondsToRecord);

    int16_t myBuffer[requestedFrames];

    constexpr int64_t kTimeoutValue=3*kNanosPerMillisecond;

    int frameRead=0;

    do{
        auto result=stream->read(myBuffer,stream->getBufferSizeInFrames(),0);
        if(result!=Result::OK) break;
        frameRead=result.value();

    } while (frameRead!=0);
   // while (isRecording) {
        auto result = stream->read(myBuffer, requestedFrames, kTimeoutValue);
        if (result == Result::OK) {
            LOGD("Read %d frames", result.value());
        } else {
            LOGE("Error reading stream %s", convertToText(result.error()));
        }
   // }

    stream->close();

}

