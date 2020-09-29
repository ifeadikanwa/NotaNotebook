package com.ifyezedev.notanotebook;

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
import android.view.MotionEvent;
import android.view.View;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.styles.toolbar.IARE_Toolbar;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_AlignmentCenter;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_AlignmentLeft;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_AlignmentRight;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_BackgroundColor;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Bold;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_FontColor;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_FontSize;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Hr;
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
import com.google.android.material.textfield.TextInputEditText;

import net.dankito.richtexteditor.android.RichTextEditor;
import net.dankito.richtexteditor.android.toolbar.AllCommandsEditorToolbar;
import net.dankito.richtexteditor.callback.GetCurrentHtmlCallback;
import net.dankito.richtexteditor.command.CommandName;

import org.jetbrains.annotations.NotNull;

public class NoteEditActivity extends AppCompatActivity {
    FirestoreRepository firestoreRepository;
    TextInputEditText noteTitleEdit;
    RichTextEditor noteContentEdit;
    AllCommandsEditorToolbar noteToolBar;
    String notebookId;
    int notebookColor;
    String notebookContentId;
    String notebookContentTitle;
    String notebookContent;
    boolean fromNoteViewActivity;
    boolean changesMade = false;

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
        noteContentEdit = (RichTextEditor) findViewById(R.id.richtexteditor);
        firestoreRepository = FirestoreRepository.getInstance();

        //touchlistener for listening to changes
        noteTitleEdit.setOnTouchListener(touchListener);
        noteContentEdit.setOnTouchListener(touchListener);

        initialiseToolBar();
        Intent intent = getIntent();
        fromNoteViewActivity = intent.getBooleanExtra(NotebookActivity.EXTRA_FROM_VIEW_ACTIVITY, false);

        //if intent is from View Activity we want to load previous content and title to screen
        if(fromNoteViewActivity){
            notebookId = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_ID);
            notebookContentId = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_ID);
            notebookContentTitle = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_TITLE);
            notebookContent = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT);

            noteTitleEdit.setText(notebookContentTitle);
            noteContentEdit.setHtml(notebookContent);
        }
        //if intent is not from View Activity we want to create a fresh, new note
        else {
            notebookId = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_ID);
            notebookColor = Integer.parseInt(intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_COLOR));
            noteContentEdit.setHtml("<p>Content</p>");
        }

        //done: initialize editor attributes
        noteContentEdit.setPlaceholder("Content");
        noteContentEdit.setEditorFontSize(17);
        noteContentEdit.setPadding((4 * (int) getResources().getDisplayMetrics().density));

//      some properties you also can set on editor
        noteContentEdit.setEditorBackgroundColor(Color.WHITE);
        noteContentEdit.setEditorFontColor(Color.BLACK);

//      show keyboard right at start up
        noteContentEdit.focusEditorAndShowKeyboardDelayed();

    }


    private void initialiseToolBar() {
        noteToolBar = (AllCommandsEditorToolbar) this.findViewById(R.id.editorToolbar);
        noteToolBar = (AllCommandsEditorToolbar) findViewById(R.id.editorToolbar);
        noteToolBar.removeCommand(CommandName.INSERTIMAGE);
        noteToolBar.removeCommand(CommandName.INSERTLINK); 
        noteToolBar.removeCommand(CommandName.FONTNAME);
        noteToolBar.removeCommand(CommandName.FONTSIZE);
        noteToolBar.removeCommand(CommandName.FORMATBLOCK);
        noteToolBar.removeCommand(CommandName.BLOCKQUOTE);
        noteToolBar.setEditor(noteContentEdit);
    }

    //if any view with this touch listener is touched we want to recognise that as a change being made
    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            changesMade = true;
            return false;
        }
    };

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
                //save, no questions asked
                if(fromNoteViewActivity){
                    updateNote();
                    exitIntent();
                }
                else{
                    createNote();
                    finish();
                }
                return true;

            case android.R.id.home:
                //performs the same actions in onBackPressed
                //done: create note or UPDATE NOTE
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSaveWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to save changes?")
                .setPositiveButton("save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        updateNote();
                        exitIntent();
                    }
                })
                .setNegativeButton("discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(dialogInterface != null){
                            dialogInterface.dismiss();
                            finish();
                        }
                    }
                })
                .create()
                .show();
    }

    //send results to calling activity and exit NoteEditActivity
    private void exitIntent(){

        noteContentEdit.getCurrentHtmlAsync(new GetCurrentHtmlCallback() {
            @Override
            public void htmlRetrieved(@NotNull String s) {
                String title = noteTitleEdit.getText().toString();
                String content = s;

                if(title.trim().length() == 0){
                    title = "untitled";
                }

                //we send the result to the calling activity
                Intent intent = new Intent();
                intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_TITLE, title);
                intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT, content);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    //update note in firestore
    private void updateNote() {

        noteContentEdit.getCurrentHtmlAsync(new GetCurrentHtmlCallback() {
            @Override
            public void htmlRetrieved(@NotNull String s) {
                String title = noteTitleEdit.getText().toString();
                String content = s;

                if(title.trim().length() == 0){
                    title = "untitled";
                }

                //update note in firestore
                firestoreRepository.updateNote(notebookId, notebookContentId, title, content);
            }
        });
    }


    private void createNote() {
        //get html in rich edit text and create a new note
        noteContentEdit.getCurrentHtmlAsync(new GetCurrentHtmlCallback() {
            @Override
            public void htmlRetrieved(@NotNull String s) {

                String title = noteTitleEdit.getText().toString();
                String content = s;

                //if nothing is entered we don't want to save the note
                if(title.trim().length() == 0 && content.equalsIgnoreCase("<p>\u200B</p>") ){
                    return;
                }

                //if there is no title we want to give a default one
                if(title.trim().length() == 0){
                    title = "untitled";
                }

                //create a new note in firestore
                firestoreRepository.createNewNote(notebookId, notebookColor, title, content);
            }
        });
    }

    //ask if they want to save update but save new notes no questions asked
    //done: on back pressed: create note or UPDATE NOTE
    @Override
    public void onBackPressed() {
        // Important: Overwrite onBackPressed and pass it to toolbar.There's no other way that it can get informed of back button presses.
        if(!noteToolBar.handlesBackButtonPress()){
            if(fromNoteViewActivity){
                if(changesMade){
                    openSaveWarningDialog();
                }
                else {
                    finish();
                }
            }
            else{
                createNote();
                finish();
            }
        }

    }
}