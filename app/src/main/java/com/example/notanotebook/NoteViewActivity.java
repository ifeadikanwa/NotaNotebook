package com.example.notanotebook;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.chinalwb.are.AREditText;
import com.google.android.material.textfield.TextInputEditText;

public class NoteViewActivity extends AppCompatActivity {
    String notebookId;
    String notebookName;
    String notebookContentId;
    String notebookContentTitle;
    String notebookContent;

    TextInputEditText notebookTitleView;
    TextInputEditText noteTitleView;
    AREditText noteContentView;

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


        setContentView(R.layout.activity_note_view);

        Intent intent = getIntent();
        notebookId = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_ID);
        notebookName = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_NAME);
        notebookContentId = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_ID);
        notebookContentTitle = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_TITLE);
        notebookContent = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT);


        notebookTitleView = findViewById(R.id.notebook_titleView);
        noteTitleView = findViewById(R.id.note_titleView);
        noteContentView = findViewById(R.id.note_content_view);

        noteTitleView.setKeyListener(null);
        notebookTitleView.setKeyListener(null);
        noteContentView.setKeyListener(null);

        notebookTitleView.setText(notebookName);
        noteTitleView.setText(notebookContentTitle);

        noteContentView.fromHtml(notebookContent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_note_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_note:
                //todo: show warning alert dialog, delete note and return to notebook content Activity
                return true;
            case R.id.edit_note:
                //todo: open note Edit activity and current note content
                return true;
            case R.id.note_info:
                //todo: display alert dialog with creation date and latest update date and OK button
                // (normal alert dialog with message set to display time)
                return true;
            case R.id.pin_note:
                //todo: pin note or unpin note and change the icon too
                return true;
            case android.R.id.home:
                //todo: return to notebook content activity
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}