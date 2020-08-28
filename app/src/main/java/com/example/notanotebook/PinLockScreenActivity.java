package com.example.notanotebook;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.beautycoder.pflockscreen.PFFLockScreenConfiguration;
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment;
import com.beautycoder.pflockscreen.security.PFResult;
import com.beautycoder.pflockscreen.viewmodels.PFPinCodeViewModel;
import com.google.android.gms.actions.NoteIntents;

public class PinLockScreenActivity extends AppCompatActivity {
    boolean from_view_activity;

    String notebookId;
    String notebookName;
    String notebookContentId;
    String notebookContentTitle;
    String notebookContent;
    boolean pinned;
    boolean locked;
    boolean note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock_screen);

        Intent intent = getIntent();
        from_view_activity = intent.getBooleanExtra(NotebookActivity.EXTRA_FROM_VIEW_ACTIVITY,false);

        if(!from_view_activity){
            notebookId = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_ID);
            notebookName = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_NAME);
            notebookContentId = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_ID);
            notebookContentTitle = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_TITLE);
            notebookContent = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT);
            pinned = intent.getBooleanExtra(NotebookActivity.EXTRA_PINNED_STATUS, false);
            locked = intent.getBooleanExtra(NotebookActivity.EXTRA_LOCKED_STATUS, false);
            note = intent.getBooleanExtra(NotebookActivity.EXTRA_IS_NOTE, true);
        }

        showLockScreenFragment();
    }

    //this checks if pin exists, and then passes the boolean result to the main showLockScreenFragment
    private void showLockScreenFragment() {
        new PFPinCodeViewModel().isPinCodeEncryptionKeyExist().observe(
                this,
                new Observer<PFResult<Boolean>>() {
                    @Override
                    public void onChanged(@Nullable PFResult<Boolean> result) {
                        if (result == null) {
                            return;
                        }
                        if (result.getError() != null) {
                            Toast.makeText(PinLockScreenActivity.this, "Can not get pin code info", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        showLockScreenFragment(result.getResult());
                    }
                }
        );
    }

    //this builds the lock screen based on if user already created a pin or not
    private void showLockScreenFragment(boolean isPinExist) {
        final PFFLockScreenConfiguration.Builder builder = new PFFLockScreenConfiguration.Builder(this)
                .setTitle(isPinExist ? "Enter your pin code or fingerprint" : "Create Pin")
                .setCodeLength(4)
//                .setLeftButton("Can't remember")
                .setNewCodeValidation(true)
                .setNewCodeValidationTitle("Verify pin")
                .setUseFingerprint(true);
        final PFLockScreenFragment fragment = new PFLockScreenFragment();

        //set action for your left button(usually used for directing user to reset password, when they've forgotten)
//        fragment.setOnLeftButtonClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(PinLockScreenActivity.this, "Left button pressed", Toast.LENGTH_LONG).show();
//            }
//        });

        //if pin exists we authenticate the user, if not we create new pin
        builder.setMode(isPinExist
                ? PFFLockScreenConfiguration.MODE_AUTH
                : PFFLockScreenConfiguration.MODE_CREATE);

        //if pin exists we want to get the pin the users entry will be compared to
        //we also want to attach the LoginListener-> this contains the actions that will happen on successful or failed login
        if (isPinExist) {
            //we are getting the pin from shared preference
            fragment.setEncodedPinCode(PreferencesSettings.getPin(this));
            fragment.setLoginListener(mLoginListener);
        }

        //build the lockscreen
        fragment.setConfiguration(builder.build());

        //set CodeCreateListener-> actions that will happen after user creates a new code
        fragment.setCodeCreateListener(mCodeCreateListener);

        //show the LockScreen Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_view, fragment).commit();

    }


    //CodeCreateListener-> actions that will happen after user creates a new code
    private final PFLockScreenFragment.OnPFLockScreenCodeCreateListener mCodeCreateListener =
            new PFLockScreenFragment.OnPFLockScreenCodeCreateListener() {
                @Override
                public void onCodeCreated(String encodedCode) {
                    Toast.makeText(PinLockScreenActivity.this, "Pin created", Toast.LENGTH_SHORT).show();

                    //save the pin code in Shared Preference
                    PreferencesSettings.savePinToPref(PinLockScreenActivity.this, encodedCode);

                    if(from_view_activity){
                        ViewActivityIntent();
                    }
                    //if intent is not from view activity and is a note send intent to note view activity
                    else if(note){
                        NoteActivityIntent();
                    }
                    //if intent is not from view activity and is a checklist send intent to checklist edit activity
                    else {
                        ChecklistActivityIntent();
                    }

                }

                @Override
                public void onNewCodeValidationFailed() {
                    Toast.makeText(PinLockScreenActivity.this, "Pin verification error", Toast.LENGTH_SHORT).show();
                }
            };

    //LoginListener-> this contains the actions that will happen on successful or failed login
    private final PFLockScreenFragment.OnPFLockScreenLoginListener mLoginListener =
            new PFLockScreenFragment.OnPFLockScreenLoginListener() {

                @Override
                public void onCodeInputSuccessful() {
                    Toast.makeText(PinLockScreenActivity.this, "Pin successful", Toast.LENGTH_SHORT).show();

                    //if intent is from view activity we want to send results back
                    if(from_view_activity){
                        ViewActivityIntent();
                    }
                    //if intent is not from view activity and is a note send intent to note view activity
                    else if(note){
                        NoteActivityIntent();
                    }
                    else {
                        ChecklistActivityIntent();
                    }
                }

                @Override
                public void onFingerprintSuccessful() {
                    Toast.makeText(PinLockScreenActivity.this, "Fingerprint successful", Toast.LENGTH_SHORT).show();

                    //if intent is from note view activity we want to send results back
                    if(from_view_activity){
                        ViewActivityIntent();
                    }
                    //if intent is not from view activity and is a note send intent to note view activity
                    else if(note){
                        NoteActivityIntent();
                    }
                    else{
                        ChecklistActivityIntent();
                    }
                }

                @Override
                public void onPinLoginFailed() {
                    Toast.makeText(PinLockScreenActivity.this, "Wrong Pin", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFingerprintLoginFailed() {
                    Toast.makeText(PinLockScreenActivity.this, "Fingerprint failed", Toast.LENGTH_SHORT).show();
                }
            };


    private void ViewActivityIntent() {
        //send result back to calling activity
        Intent intent = new Intent();
        setResult(RESULT_OK);
        finish();
    }

    private void NoteActivityIntent() {
        Intent intent = new Intent(PinLockScreenActivity.this, NoteViewActivity.class);
        intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_ID, notebookId);
        intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_NAME, notebookName);
        intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_ID, notebookContentId);
        intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_TITLE, notebookContentTitle);
        intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT, notebookContent);
        intent.putExtra(NotebookActivity.EXTRA_PINNED_STATUS, pinned);
        intent.putExtra(NotebookActivity.EXTRA_LOCKED_STATUS, locked);
        startActivity(intent);
    }

    private void ChecklistActivityIntent() {
        Intent intent = new Intent(PinLockScreenActivity.this, ChecklistEditActivity.class);
        intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_ID, notebookId);
        intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_ID, notebookContentId);
        intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_TITLE, notebookContentTitle);
        intent.putExtra(NotebookActivity.EXTRA_PINNED_STATUS, pinned);
        intent.putExtra(NotebookActivity.EXTRA_LOCKED_STATUS, locked);
        startActivity(intent);
    }

}