/*
 *
 *   Copyright 2020. Explore in HMS. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   You may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.hms.explorehms.huawei.feature_mlkit.ui.mlServices.languageRelated.speechRecognition;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;

import com.huawei.hms.mlsdk.asr.MLAsrConstants;
import com.huawei.hms.mlsdk.asr.MLAsrListener;
import com.huawei.hms.mlsdk.asr.MLAsrRecognizer;

import java.util.ArrayList;

public class SpeechRecognizerManager {

    private static final String TAG = SpeechRecognizerManager.class.getSimpleName();

    protected AudioManager mAudioManager;
    protected MLAsrRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;

    private AudioRecordResultOnReady mListener;
    ArrayList<String> mResultsList = new ArrayList<>();

    /**
     * SpeechRecognizerManager constructor
     *
     * @param context
     * @param language
     * @param listener : AudioRecordResultOnReady interface
     */
    public SpeechRecognizerManager(Context context, String language, AudioRecordResultOnReady listener) {
        try {
            mListener = listener;
        } catch (ClassCastException e) {
            Log.e(TAG, e.toString());
        }
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = MLAsrRecognizer.createAsrRecognizer(context);
        mSpeechRecognizer.setAsrListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(MLAsrConstants.ACTION_HMS_ASR_SPEECH);

        mSpeechRecognizerIntent.putExtra(MLAsrConstants.LANGUAGE, language).putExtra(MLAsrConstants.FEATURE, MLAsrConstants.FEATURE_WORDFLUX);
    }


    public void startListening() {
        mSpeechRecognizer.startRecognizing(mSpeechRecognizerIntent);
    }

    public void destroy() {
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
        }

    }

    protected class SpeechRecognitionListener implements MLAsrListener {
        @Override
        public void onStartListening() {
            Log.d(TAG, "onStartListening...");
        }

        @Override
        public void onStartingOfSpeech() {
            Log.d(TAG, "onStartingOfSpeech...-");
        }

        @Override
        public void onVoiceDataReceived(byte[] data, float energy, Bundle bundle) {
            int length = data == null ? 0 : data.length;
            Log.d(TAG, "onVoiceDataReceived... data.length = " + length);
        }

        @Override
        public void onRecognizingResults(Bundle partialResults) {
            Log.e(TAG, "onRecognizingResults");
            if (partialResults != null && mListener != null) {
                mResultsList.clear();
                mResultsList.add(partialResults.getString(MLAsrRecognizer.RESULTS_RECOGNIZING));
                mListener.onResults(mResultsList);
                Log.d(TAG, "onRecognizingResults is " + partialResults);
            }
        }

        @Override
        public void onResults(Bundle results) {
            Log.e(TAG, "onResults");
            if (results != null && mListener != null) {
                mResultsList.clear();
                mResultsList.add(results.getString(MLAsrRecognizer.RESULTS_RECOGNIZED));
                mListener.onFinish();
                Log.d(TAG, "onResults is " + results.toString());
            }
        }

        @Override
        public void onError(int error, String errorMessage) {
            Log.e(TAG, "onError : " + error + " : errorMessage : " + errorMessage);
            // If you don't add this, there will be no response after you cut the network
            if (mListener != null) {
                mListener.onError(error);
            }
        }

        @Override

        public void onState(int state, Bundle params) {
            Log.e(TAG, "onState :" + state);
            if (state == MLAsrConstants.STATE_NO_SOUND_TIMES_EXCEED && mListener != null) {
                mListener.onFinish();
            }
        }
    }


    public interface AudioRecordResultOnReady {

        void onResults(ArrayList<String> results);

        void onFinish();

        void onError(int error);

    }

}
