package com.example.notanotebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.chinalwb.are.AREditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NoteViewActivity extends AppCompatActivity {
    String notebookId;
    String notebookName;
    String notebookContentId;
    String notebookContentTitle;
    String notebookContent;

    TextInputEditText notebookTitleView;
    TextInputEditText noteTitleView;
    AREditText noteContentView;

    FirestoreRepository firestoreRepository;
    DocumentReference noteDocRef;
    public static final int NOTE_EDIT_REQUEST_CODE = 1;

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

        firestoreRepository = FirestoreRepository.getInstance();
        notebookTitleView = findViewById(R.id.notebook_titleView);
        noteTitleView = findViewById(R.id.note_titleView);
        noteContentView = findViewById(R.id.note_content_view);

        //make the edit text views unresponsive to keyboard events
        noteTitleView.setKeyListener(null);
        notebookTitleView.setKeyListener(null);
        noteContentView.setKeyListener(null);

        notebookTitleView.setText(notebookName);
        noteTitleView.setText(notebookContentTitle);

        noteContentView.fromHtml(notebookContent);

        noteDocRef = firestoreRepository.notebookRef
                .document(notebookId)
                .collection(FirestoreRepository.NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId);
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
                //done: show warning alert dialog, delete note and return to notebook content Activity
                showDeleteWarningDialog();
                return true;
            case R.id.edit_note:
                //todo: open note Edit activity and current note content
                openNoteEditActivity();
                return true;
            case R.id.note_info:
                //done: display alert dialog with creation date and latest update date and OK button
                //(normal alert dialog with message set to display time)
                getNoteInfo();
                return true;
            case R.id.pin_note:
                //todo: pin note or unpin note and change the icon too
                return true;
            case android.R.id.home:
                //done: return to notebook content activity
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openNoteEditActivity() {
        Intent intent = new Intent(this, NoteEditActivity.class);
        intent.putExtra(NotebookActivity.EXTRA_FROM_VIEW_ACTIVITY, true);
        intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_ID, notebookId);
        intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_ID, notebookContentId);
        intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_TITLE, notebookContentTitle);
        intent.putExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT, notebookContent);
        startActivityForResult(intent, NOTE_EDIT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == NOTE_EDIT_REQUEST_CODE){
            if(data == null){
                return;
            }

            //todo: update the variables and ui
            notebookContentTitle = data.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_TITLE);
            notebookContent = data.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT);

            noteTitleView.setText(notebookContentTitle);

            noteContentView.setText("");
            noteContentView.fromHtml(notebookContent);
        }
    }

    private void getNoteInfo(){

        noteDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        NotebookContent note = task.getResult().toObject(NotebookContent.class);
                        Date creation = note.getCreatedTime();
                        Date modified = note.getLatestUpdateTime();

                        //pattern is:  Tue, 13 May 2011 14:23
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm");

                        String creationStr = simpleDateFormat.format(creation);
                        String modifiedStr = simpleDateFormat.format(modified);

                        String dialogMessage = "Created: " + creationStr + "\n\nModified: " + modifiedStr;

                        showNoteInfoDialog(dialogMessage);
                    }
                });

    }

    private void showDeleteWarningDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to delete?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        firestoreRepository.deleteNote(noteDocRef);
                        firestoreRepository.decreaseNotebookContentCount(notebookId);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(dialogInterface != null){
                            dialogInterface.dismiss();
                        }
                    }
                })
                .create()
                .show();
    }

    private void showNoteInfoDialog(String dialogMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Note Information")
                .setMessage(dialogMessage)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(dialogInterface != null){
                            dialogInterface.dismiss();
                        }
                    }
                })
                .create()
                .show();
    }
}