package com.example.notanotebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class NoteEditActivity extends AppCompatActivity {
    FirestoreRepository firestoreRepository;
    TextInputEditText noteTitleEdit;
    TextInputEditText noteContentEdit;
    String notebookId;
    int notebookColor;
    boolean saved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set the title in action bar to nothing
        setTitle("");

        //make status bar white with black icons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_note_edit);

        firestoreRepository = FirestoreRepository.getInstance();

        noteTitleEdit = findViewById(R.id.noteTitleEdit);
        noteContentEdit = findViewById(R.id.noteContentEdit);

        Intent intent = getIntent();
        notebookId = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_ID);
        notebookColor = Integer.parseInt(intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_COLOR));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_note_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.done_button:
            case android.R.id.home:
                //todo: save note or UPDATE NOTE
                saved = true;
                saveNote();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveNote() {
        String title = noteTitleEdit.getText().toString();

        if(title.trim().length() == 0){
            title = "untitled";
        }

        String content = noteContentEdit.getText().toString();

        firestoreRepository.createNewNote(notebookId, notebookColor, title, content);
    }

    //todo: on back pressed: save note or UPDATE NOTE
    @Override
    protected void onDestroy() {
        if(!saved){
            saveNote();
        }
        super.onDestroy();
    }
}