package com.example.notanotebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.styles.IARE_Style;
import com.chinalwb.are.styles.toolbar.ARE_ToolbarDefault;
import com.chinalwb.are.styles.toolbar.IARE_Toolbar;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Abstract;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_AlignmentCenter;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_AlignmentLeft;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_AlignmentRight;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_BackgroundColor;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Bold;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_FontColor;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_FontSize;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Hr;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Image;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Italic;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Link;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_ListBullet;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_ListNumber;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Quote;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Strikethrough;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Subscript;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Superscript;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Underline;
import com.chinalwb.are.styles.toolitems.IARE_ToolItem;
import com.chinalwb.are.styles.toolitems.IARE_ToolItem_Updater;
import com.google.android.material.textfield.TextInputEditText;

public class NoteEditActivity extends AppCompatActivity {
    FirestoreRepository firestoreRepository;
    TextInputEditText noteTitleEdit;
    AREditText noteContentEdit;
    IARE_Toolbar noteToolBar;
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

        noteTitleEdit = findViewById(R.id.noteTitleEdit);
        noteContentEdit = findViewById(R.id.are_edittext);
        firestoreRepository = FirestoreRepository.getInstance();

        intialiseToolBar();
        Intent intent = getIntent();
        notebookId = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_ID);
        notebookColor = Integer.parseInt(intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_COLOR));
    }

    private void intialiseToolBar() {
        noteToolBar = this.findViewById(R.id.are_toolbar);
        IARE_ToolItem bold = new ARE_ToolItem_Bold();
        IARE_ToolItem italic = new ARE_ToolItem_Italic();
        IARE_ToolItem underline = new ARE_ToolItem_Underline();
        IARE_ToolItem strikethrough = new ARE_ToolItem_Strikethrough();
        IARE_ToolItem highlighter = new ARE_ToolItem_BackgroundColor();
        IARE_ToolItem fontColor = new ARE_ToolItem_FontColor();
        IARE_ToolItem fontSize = new ARE_ToolItem_FontSize();
        IARE_ToolItem quote = new ARE_ToolItem_Quote();
        IARE_ToolItem listNumber = new ARE_ToolItem_ListNumber();
        IARE_ToolItem listBullet = new ARE_ToolItem_ListBullet();
        IARE_ToolItem divider = new ARE_ToolItem_Hr();
        IARE_ToolItem link = new ARE_ToolItem_Link();
        IARE_ToolItem subscript = new ARE_ToolItem_Subscript();
        IARE_ToolItem superscript = new ARE_ToolItem_Superscript();
        IARE_ToolItem left = new ARE_ToolItem_AlignmentLeft();
        IARE_ToolItem center = new ARE_ToolItem_AlignmentCenter();
        IARE_ToolItem right = new ARE_ToolItem_AlignmentRight();



        noteToolBar.addToolbarItem(bold);
        noteToolBar.addToolbarItem(italic);
        noteToolBar.addToolbarItem(underline);
        noteToolBar.addToolbarItem(strikethrough);
        noteToolBar.addToolbarItem(highlighter);
        noteToolBar.addToolbarItem(fontSize);
        noteToolBar.addToolbarItem(fontColor);
        noteToolBar.addToolbarItem(quote);
        noteToolBar.addToolbarItem(listNumber);
        noteToolBar.addToolbarItem(listBullet);
        noteToolBar.addToolbarItem(divider);
        noteToolBar.addToolbarItem(link);
        noteToolBar.addToolbarItem(subscript);
        noteToolBar.addToolbarItem(superscript);
        noteToolBar.addToolbarItem(left);
        noteToolBar.addToolbarItem(center);
        noteToolBar.addToolbarItem(right);

        noteContentEdit.setToolbar(noteToolBar);
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
        String content = noteContentEdit.getHtml();

        //if nothing is entered we don't want to save the note
        if(title.trim().length() == 0 && content.trim().length() == 0){
            return;
        }

        //if there is no title we want to give a default one
        if(title.trim().length() == 0){
            title = "untitled";
        }

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