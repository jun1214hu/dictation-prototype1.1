package com.example.jung.speechtotext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.JsonReader;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via lastname/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {


    /**
     * Id to identity READ_CONTACTS permission request.
     */
    // public static final String EXTRA_MESSAGE = "com.example.jung.speechtotext.MESSAGE";
    // private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */


    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    public static final String EXTRA_MESSAGE = "com.example.jung.speechtotext.MESSAGE";
    public static final String EXTRA_MESSAGE_TWO = "com.example.jung.speechtotext.MESSAGE_TWO";

    // UI references.
    private AutoCompleteTextView mLastnameView;
    private AutoCompleteTextView mPatientIDView;
    private EditText mBirthdateView;
    private View mProgressView;
    private View mLoginFormView;
    private String message;
    private String pastTranscript;
    private File file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mLastnameView = (AutoCompleteTextView) findViewById(R.id.lastname);
        //populateAutoComplete();

        mPatientIDView = (AutoCompleteTextView) findViewById(R.id.identification);

        mBirthdateView = (EditText) findViewById(R.id.birthdate);
        mBirthdateView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.lastname || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mLastnameSignInButton = (Button) findViewById(R.id.access_button);
        mLastnameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mReadButton = (Button) findViewById(R.id.read_button);
        mReadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    readFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    private void readFile() throws IOException, JSONException {
        if (!isExternalStorageWritable() || !isExternalStorageReadable())
        {
            mPatientIDView.setText("Error");
            return;
        }

        File extDir = getExternalFilesDir(null);
//      String path = extDir.getAbsolutePath();

        String patientFile = mPatientIDView.getText().toString();
        String FILENAME = patientFile + ".json";

        file = new File(extDir, FILENAME);

        FileInputStream in = openFileInput(FILENAME);

        InputStreamReader inputStreamReader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder sb = new StringBuilder();

        while ((pastTranscript = bufferedReader.readLine()) != null) {
            sb.append(pastTranscript);
        }

        pastTranscript = sb.toString();

        inputStreamReader.close();


    }






//        File extDir = getExternalFilesDir(null);
//        String filename = "pleaseread_1.json";
//        file = new File(extDir, filename);
//        FileInputStream in = openFileInput(filename);
//
//        InputStreamReader inputStreamReader = new InputStreamReader(in);
//        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//        StringBuilder sb = new StringBuilder();
//
//        while ((pastTranscript = bufferedReader.readLine()) != null) {
//            sb.append(pastTranscript);
//        }
//        inputStreamReader.close();
//        pastTranscript = sb.toString();
//    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mLastnameView.setError(null);
        mBirthdateView.setError(null);
        mPatientIDView.setError(null);

        // Store values at the time of the login attempt.
        String lastname = mLastnameView.getText().toString();
        String birthdate = mBirthdateView.getText().toString();
        String identification = mPatientIDView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(birthdate) && TextUtils.isEmpty(identification)) {
            mBirthdateView.setError(getString(R.string.error_field_required));
            focusView = mBirthdateView;
            cancel = true;
        }

        else if (!isbirthdateValid(birthdate) && TextUtils.isEmpty(identification)) {
            mBirthdateView.setError(getString(R.string.error_invalid_birthdate));
            focusView = mBirthdateView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(lastname) && TextUtils.isEmpty(identification)) {
            mLastnameView.setError(getString(R.string.error_field_required));
            focusView = mLastnameView;
            cancel = true;
        }

        else if (!islastnameValid(lastname) && TextUtils.isEmpty(identification)) {
            mLastnameView.setError(getString(R.string.error_invalid_lastname));
            focusView = mLastnameView;
            cancel = true;
        }

        if (!TextUtils.isEmpty(identification) && (!TextUtils.isEmpty(lastname) || !TextUtils.isEmpty(birthdate)))
        {
            message = mPatientIDView.getText().toString();
        }
        else
        {
            message = mLastnameView.getText().toString() + mBirthdateView.getText().toString() + mPatientIDView.getText().toString();
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(lastname, birthdate);
            mAuthTask.execute((Void) null);

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra(EXTRA_MESSAGE, message);
            intent.putExtra(EXTRA_MESSAGE_TWO, pastTranscript);
            startActivity(intent);
        }
    }



    private boolean islastnameValid(String lastname) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isbirthdateValid(String birthdate) {
        //TODO: Replace this with your own logic
        return birthdate.length() > 4;
    }

    private boolean isidentificationValid(String identification) {
        //TODO: Replace this with your own logic
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);

                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> lastnames = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            //emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addLastnamesToAutoComplete(lastnames);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addLastnamesToAutoComplete(List<String> LastnameAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, LastnameAddressCollection);

        mLastnameView.setAdapter(adapter);
    }


    interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Identity.NAMESPACE,
                ContactsContract.CommonDataKinds.Identity.IS_USER_PROFILE,
        };

        int NAMESPACE = 0;
        int IS_USER_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mLastname;
        private final String mBirthdate;

        UserLoginTask(String lastname, String birthdate) {
            mLastname = lastname;
            mBirthdate = birthdate;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mLastname)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mBirthdate);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mBirthdateView.setError(getString(R.string.error_invalid_birthdate));
                mBirthdateView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
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


