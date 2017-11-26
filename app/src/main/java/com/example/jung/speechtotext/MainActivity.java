package com.example.jung.speechtotext;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.*;
import java.text.DateFormat;
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
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private EditText mVoiceInputTv;
    private ImageButton mSpeakBtn;
    private Button mBtnUndo;
    private Button mBtnSave;
    private TextView mIdentifier;
    private TextView mSave;
    private TextView mSavedAs;

    private FloatingActionButton mBack;

    private String outputCurrent;
    private String output;
    private String undoOutput;

    private Spinner languageSpinner;

    private String language;


    private File file;
    private String patientFile;
    private int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String message = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE);
        String pastTranscript = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE_TWO);

        mIdentifier = (TextView) findViewById(R.id.textView2);
        mIdentifier.append(message);
        mSave = (TextView) findViewById(R.id.textView3);

        mSavedAs = (TextView) findViewById(R.id.textView7);

        mBack = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                try {
                    createFile(v);
                    setContentView(R.layout.activity_login);
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

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

        mBtnUndo = (Button) findViewById(R.id.btnUndo);
        mBtnUndo.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                try {
                    undoText(v);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });

        mBtnUndo.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View v) {
                clearText(v);
                return false;
            }
        });


        languageSpinner = (Spinner) findViewById(R.id.languageSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.language_arrays, android.R.layout.simple_spinner_item);
        languageSpinner.setAdapter(adapter);
        languageSpinner.setOnItemSelectedListener(this);

        mVoiceInputTv.setText(pastTranscript);

    }

    private void clearText(View v) {
        mVoiceInputTv.setText("");
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        switch (position) {
            case 0:
                language = Locale.getDefault().toString();
                break;
            case 1:
                language = "en-US";
                break;
            case 2:
                language = "es-MX";
                break;
            case 3:
                language = "hi-IN";
                break;
            case 4:
                language = "ar-EG";
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        language = Locale.getDefault().toString();
    }

    public void undoText(View v) throws IOException, JSONException {
        mVoiceInputTv.setText(undoOutput);
    }


    public void createFile(View v) throws IOException, JSONException {

        if (!isExternalStorageWritable() || !isExternalStorageReadable())
        {
            mSave.setText("Error");
            return;
        }

        String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());
        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

        File extDir = getExternalFilesDir(null);
//      String path = extDir.getAbsolutePath();

        Intent intent = getIntent();
        String message = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE);

        counter++;

        patientFile = message + "_" + counter;
        String FILENAME = patientFile + ".json";

        file = new File(extDir, FILENAME);

        mSave.setText(time);
        mSavedAs.setText(patientFile);

        output = mVoiceInputTv.getText().toString();

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObj = new JSONObject();


        jsonObj.put("patient ID", message);
        jsonObj.put("Transcript", output);
        jsonObj.put("Time", time);
        jsonObj.put("Date", date);

        jsonArray.put(jsonObj);

        String jsonArraytext = jsonArray.toString();

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(jsonArraytext.getBytes());
        fos.close();

        mVoiceInputTv.setText("");
        mSavedAs.setText(patientFile);
    }

    public void onBackPressed() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Recording...");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        undoOutput = mVoiceInputTv.getText().toString();
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mVoiceInputTv.append(result.get(0));
                    mVoiceInputTv.append(" , ");

                    outputCurrent = result.get(0);
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









