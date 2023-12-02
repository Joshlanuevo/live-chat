/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.ym.chat.utils.audio;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;

public class AudioRecorder implements AudioManager.OnAudioFocusChangeListener {
    private Context context;
    private AudioManager audioManager;
    private MediaRecorder mediaRecorder;
    private AudioFocusRequest audioFocusRequest;

    public AudioRecorder(Context context) {
        this.context = context;
    }

    /**
     * @param outputAudioFile
     */
    public void startRecord(String outputAudioFile) {
        this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(this, new Handler())
                    .build();
            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);
        }

        try {
            this.audioManager.setMode(0);
            this.mediaRecorder = new MediaRecorder();

            try {
                //*说话声音是模拟信号，需要转换成数字信号
                //*采样频率越高，数据越大，音质越好。
                //*常用频率：8kHz、11.025kHz、22.05kHz、16kHz、
                //37.8kHz、44.1kHz、48kHz、96kHz、192kHz
                this.mediaRecorder.setAudioSamplingRate(16000);
                //setAudioEncodingBitRate
                //*声音编码：码率越大，压缩越小，音质越好
                //*AAC HE（High Efficiency）：32kbps-96kbps，码率低，音质一般。
                //*AAC LC（Low Complexity）：96kbps-192kbps，平衡低码率和高音质。
                this.mediaRecorder.setAudioEncodingBitRate(7950);
            } catch (Resources.NotFoundException var3) {
                var3.printStackTrace();
            }

            this.mediaRecorder.setAudioChannels(1);
            this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            this.mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            this.mediaRecorder.setOutputFile(outputAudioFile);
            this.mediaRecorder.prepare();
            this.mediaRecorder.start();
        } catch (Exception var4) {
            var4.printStackTrace();
        }
    }

    public void stopRecord() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            } else {
                audioManager.abandonAudioFocus(this);
            }
            mediaRecorder.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            stopRecord();
        }
    }

    public int getMaxAmplitude() {
        try {
            return mediaRecorder.getMaxAmplitude();
        } catch (Exception e) {
            // do nothing
        }
        return 0;
    }

    public interface OnRecordError {
        void onError(String msg);
    }
}
