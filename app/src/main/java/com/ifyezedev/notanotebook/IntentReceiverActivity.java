package com.ifyezedev.notanotebook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class IntentReceiverActivity extends AppCompatActivity {
    FirebaseUser user;
    Button cancel;
    Button save;
    TextInputEditText title;
    TextInputEditText content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.activity_intent_receiver);


        user = FirebaseAuth.getInstance().getCurrentUser();
        cancel = findViewById(R.id.cancel_share);
        save = findViewById(R.id.save_share);
        title = findViewById(R.id.shared_title);
        content = findViewById(R.id.shared_content);

        // Get intent and get the content
        Intent shareIntent = getIntent();

        String sharedText = shareIntent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
            content.setText(sharedText);
        }

        //collect the data in the text fields and send it to notebook activity
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String noteTitle = title.getText().toString();
                String noteContent = content.getText().toString();

                //if user is not logged in, notify them that they have to be logged in to create notes
                if(user == null){
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.root_layout), "You need to be signed in to your Nota account to be able to save notes",
                            Snackbar.LENGTH_LONG);

                    snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            //when snackbar disappears close the app/activity
                            finish();
                        }
                    });

                    snackbar.show();
                }

                //else user is signed in so start the saving process
                else{
                    if(noteTitle.trim().length() == 0 || noteTitle.isEmpty()){
                        noteTitle = "untitled";
                    }

                    if(noteContent.trim().length() != 0 && !noteContent.isEmpty()){
                        Intent intent = new Intent(IntentReceiverActivity.this, NotebookActivity.class);
                        intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_TITLE, noteTitle);
                        intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT, noteContent);
                        intent.putExtra(NotebookActivity.EXTRA_FROM_SHARE_ACTIVITY, true);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(IntentReceiverActivity.this, "Can't save empty note", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        //on cancel click, close the app
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}