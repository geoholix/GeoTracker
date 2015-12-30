/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.mstein.geotracker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class VoiceInputActivity extends Activity implements
        DelayedConfirmationView.DelayedConfirmationListener {

    private static final int NUM_SECONDS = 5;
    private DelayedConfirmationView mDelayedConfirmationView;
    private TextView mNameTextView;
    private TextView mDescriptionTextView;
    private TextView mNameTitleTextView;
    private TextView mDescriptionTitleTextView;
    private TextView mSaveText;
    private static final int NAME_SPEECH_REQUEST_CODE = 0;
    private static final int DESCRIPTION_SPEECH_REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        setContentView(R.layout.voice_input_activity);
        //mNameTextView = (TextView) findViewById(R.id.name_text);
        mDescriptionTextView = (TextView) findViewById(R.id.description_text);
        //mNameTitleTextView = (TextView) findViewById(R.id.name_title);
        mDescriptionTitleTextView = (TextView) findViewById(R.id.description_title);
        mSaveText = (TextView) findViewById(R.id.save_text);

        mDelayedConfirmationView = (DelayedConfirmationView) findViewById(R.id.delayed_confirmation);
        mDelayedConfirmationView.setListener(this);
        mDelayedConfirmationView.setTotalTimeMs(NUM_SECONDS * 1000);
        showGUI(false);
        displaySpeechRecognizer("description");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onTimerSelected(View v) {
        mDelayedConfirmationView.reset();
        finish();
    }

    @Override
    public void onTimerFinished(View v) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        //pref.edit().putString(WearMainActivity.PREFS_NAME_KEY, (String) mNameTextView.getText()).apply();
        pref.edit().putString(WearMainActivity.PREFS_DESC_KEY, (String) mDescriptionTextView.getText()).apply();

        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                getString(R.string.saving_done));
        startActivity(intent);
        finish();
    }

    // Create an intent that can start the Speech Recognizer activity
    public void displaySpeechRecognizer(String type) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        switch (type) {
            case "name":
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Name?");
                startActivityForResult(intent, NAME_SPEECH_REQUEST_CODE);
                break;
            case "description":
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Description?");
                startActivityForResult(intent, DESCRIPTION_SPEECH_REQUEST_CODE);
                break;
        }
    }

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            switch (requestCode) {
                case NAME_SPEECH_REQUEST_CODE:
                    displaySpeechRecognizer("description");
                    mNameTextView.setText(spokenText);
                    break;
                case DESCRIPTION_SPEECH_REQUEST_CODE:
                    mDescriptionTextView.setText(spokenText);
                    showGUI(true);
                    mDelayedConfirmationView.start();
                    break;
            }
        } else {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showGUI(boolean b) {
        int view = View.GONE;
        if(b)
            view = View.VISIBLE;
        //mNameTitleTextView.setVisibility(view);
        //mNameTextView.setVisibility(view);
        mDescriptionTitleTextView.setVisibility(view);
        mDescriptionTextView.setVisibility(view);
        mSaveText.setVisibility(view);
        mDelayedConfirmationView.setVisibility(view);
    }
}
