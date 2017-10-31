package com.example.jung.speechtotext;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private EditText mVoiceInputTv;
    private ImageButton mSpeakBtn;
    private Button mBtnSave;
    private TextView mIdentifier;
    private TextView mSave;
    private ArrayList<String> completeOutput = new ArrayList<>(100);

    private String output;

    private File file;
    private static final String FILENAME = "Record Transcript";

    Date dateandtime = Calendar.getInstance().getTime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String message = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE);
        mIdentifier = (TextView) findViewById(R.id.textView2);
        mIdentifier.setText("Patient: ");
        mIdentifier.append(message);
        mSave = (TextView) findViewById(R.id.textView3);


        mVoiceInputTv = (EditText) findViewById(R.id.voiceInput);
        mSpeakBtn = (ImageButton) findViewById(R.id.btnSpeak);
        mSpeakBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startVoiceInput();
            }

        });
        mBtnSave = (Button) findViewById(R.id.btnSave);
        mBtnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    createFile(v);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        File extDir = getExternalFilesDir(null);
        String path = extDir.getAbsolutePath();
        mSave.setText(path);

        file = new File(extDir, FILENAME);


    }

    public void createFile(View v) throws IOException, JSONException {

        if (!isExternalStorageWritable() || !isExternalStorageReadable())
        {
            mSave.setText("Error");
            return;
        }
        mSave.setText("Saved");

        output = mVoiceInputTv.getText().toString();

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObj = new JSONObject();

        Intent intent = getIntent();
        String message = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE);


        jsonObj.put("patient ID", message);
        jsonObj.put("Transcript", output);
        jsonObj.put("Date and Time", dateandtime);

        jsonArray.put(jsonObj);

        String jsonArraytext = jsonArray.toString();

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(jsonArraytext.getBytes());
        fos.close();
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Recording...");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mVoiceInputTv.append(result.get(0));

//                    output = result.get(0);
//                    completeOutput.add(output);

                }
                break;
            }

        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}









