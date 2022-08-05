#include "AudioEngine.h"

void AudioEngine::start() {
    AudioStreamBuilder b;
    b.setFormat(AudioFormat::Float);
    b.setChannelCount(1);
    b.setPerformanceMode(PerformanceMode::LowLatency);
    b.setSharingMode(SharingMode::Exclusive);


    b.setCallback(this);
    b.openStream(&stream);

    osc.setAmplitude(0.3);
    osc.setFrequency(80.0);
    osc.setSampleRate(stream->getSampleRate());

    stream->setBufferSizeInFrames(stream->getFramesPerBurst()*2);

    stream->requestStart();
}

DataCallbackResult
AudioEngine::onAudioReady(AudioStream *oboeStream, void *audioData, int32_t numFrames) {


    osc.renderAudio(static_cast<float *>(audioData),numFrames);
    return DataCallbackResult::Continue;
}

void AudioEngine::tap(bool i) {
    osc.setWaveOn(i);
}

void AudioEngine::setFrequency(float d) {
    osc.setFrequency(d);
}
