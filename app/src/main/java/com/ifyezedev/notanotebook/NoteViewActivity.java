package com.ifyezedev.notanotebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import net.dankito.richtexteditor.android.RichTextEditor;
import net.dankito.richtexteditor.callback.GetCurrentHtmlCallback;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.lucasfsc.html2pdf.Html2Pdf;

public class NoteViewActivity extends AppCompatActivity {

    String notebookId;
    String notebookName;
    String notebookContentId;
    String notebookContentTitle;
    String notebookContent;
    boolean pinned;
    boolean locked;

    Menu activityMenu;
    TextInputEditText notebookTitleView;
    TextInputEditText noteTitleView;
    RichTextEditor noteContentView;
    TextView modifiedDateView;

    FirestoreRepository firestoreRepository;
    DocumentReference noteDocRef;
    public static final int NOTE_EDIT_REQUEST_CODE = 111;
    public static final int LOCK_NOTE_REQUEST_CODE = 222;


    @RequiresApi(api = Build.VERSION_CODES.N)
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

        noteContentView = (RichTextEditor) findViewById(R.id.note_content_view);

        //initialize necessary attributes of rich text editor
        noteContentView.setEditorFontSize(17);
        noteContentView.setPadding((4 * (int) getResources().getDisplayMetrics().density));
        noteContentView.setEditorBackgroundColor(Color.WHITE);
        noteContentView.setEditorFontColor(Color.BLACK);


        //unpack the intent
        Intent intent = getIntent();
        notebookId = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_ID);
        notebookName = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_NAME);
        notebookContentId = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_ID);
        notebookContentTitle = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_TITLE);
        notebookContent = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT);
        pinned = intent.getBooleanExtra(NotebookActivity.EXTRA_PINNED_STATUS, false);
        locked = intent.getBooleanExtra(NotebookActivity.EXTRA_LOCKED_STATUS, false);

        firestoreRepository = FirestoreRepository.getInstance();
        notebookTitleView = findViewById(R.id.notebook_titleView);
        noteTitleView = findViewById(R.id.note_titleView);
        modifiedDateView = findViewById(R.id.modifiedDateTxt);


        noteDocRef = firestoreRepository.notebookRef
                .document(notebookId)
                .collection(FirestoreRepository.NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId);

        displayModifiedDate();

        //make the edit text views unresponsive to keyboard events
        noteTitleView.setKeyListener(null);
        notebookTitleView.setKeyListener(null);
        noteContentView.setInputEnabled(false);


        notebookTitleView.setText(notebookName);
        noteTitleView.setText(notebookContentTitle);

        noteContentView.setHtml(notebookContent);

    }

    //displays lst modified date on assigned textview
    private void displayModifiedDate() {
        noteDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        NotebookContent notebookContent = task.getResult().toObject(NotebookContent.class);

                        Date modifiedDate = notebookContent.getLatestUpdateTime();

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

                        String modifiedStr ="Edited on " + dateFormat.format(modifiedDate);

                        modifiedDateView.setText(modifiedStr);
                    }
                });
    }


    private void setPinnedIcon(boolean pinned, Menu menu){
        //get the pin menu item and set icon based on pinned status in firestore
        MenuItem menuItem = menu.findItem(R.id.pin_note);
        if(pinned){
            menuItem.setIcon(R.drawable.ic_pinned);
            menuItem.setTitle("Unpin");
        }
        else{
            menuItem.setIcon(R.drawable.ic_not_pinned);
            menuItem.setTitle("Pin");
        }
    }

    private void setLockedIcon(boolean locked, Menu menu){
        //get the lock menu item and set icon based on pinned status in firestore
        MenuItem menuItem = menu.findItem(R.id.lock_note);
        if(locked){
            menuItem.setIcon(R.drawable.ic_locked);
            menuItem.setTitle("Unlock");
        }
        else{
            menuItem.setIcon(R.drawable.ic_unlocked);
            menuItem.setTitle("Lock");
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //save the menu reference to global menu variable
        activityMenu = menu;

        //inflate the activity's menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_note_view, menu);

        //show icon for items in menu overflow
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        //on creation of menu we want to set pin and locked icon to reflect current pin status
        setPinnedIcon(pinned, menu);
        setLockedIcon(locked, menu);

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
                //done: open note Edit activity and current note content
                openNoteEditActivity();
                return true;
            case R.id.note_info:
                //done: display alert dialog with creation date and latest update date and OK button
                //(normal alert dialog with message set to display time)
                getNoteInfo();
                return true;
            case R.id.pin_note:
                //done: pin note or unpin note and change the icon too
                pinNoteAction();
                return true;
            case R.id.lock_note:
                //done: lock or unlock note on click
                lockNoteAction();
                return true;
            case R.id.share_note:
                //done: share note as text
                shareNoteAction();
                return true;
            case android.R.id.home:
                //done: return to notebook content activity
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private void shareNoteAction() {
        String content = notebookContentTitle + "\n" + notebookContent;
        String mimetype = "text/plain";

//        Using Intent:
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(content).toString());
        sendIntent.setType(mimetype);

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);

//        Using ShareCompat:
//        ShareCompat.IntentBuilder
//                .from(this)
//                .setType(mimetype)
//                .setChooserTitle("Share this note:")
//                .setHtmlText(content)
//                .startChooser();
    }

    private void lockNoteAction() {
        if(locked){
            //if note is locked we want to UNLOCK
            showUnlockWarningDialog();
        }
        else{
            //if note is unlocked we want to LOCK
            Intent intent = new Intent(NoteViewActivity.this, PinLockScreenActivity.class);
            intent.putExtra(NotebookActivity.EXTRA_FROM_VIEW_ACTIVITY, true);
            startActivityForResult(intent, LOCK_NOTE_REQUEST_CODE);
        }
    }

    private void pinNoteAction() {
        //make pinned equal to the opposite of its current value
        pinned = !pinned;
        //update pinned status in firestore
        firestoreRepository.updatePinnedStatus(notebookId, notebookContentId, pinned);
        //update pinned icon
        setPinnedIcon(pinned, activityMenu);
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

        if(requestCode == NOTE_EDIT_REQUEST_CODE && resultCode == RESULT_OK){
            if(data != null){
                //done: update the variables and ui
                notebookContentTitle = data.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_TITLE);
                notebookContent = data.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT);

                noteTitleView.setText(notebookContentTitle);

                noteContentView.setHtml("");
                noteContentView.setHtml(notebookContent);
            }
        }

        if(requestCode == LOCK_NOTE_REQUEST_CODE && resultCode == RESULT_OK){
            //done: update global variable, firestore and icon
            locked = true;
            firestoreRepository.updateLockedStatus(notebookId, notebookContentId, locked);
            setLockedIcon(locked, activityMenu);
        }
    }

    private void getNoteInfo(){

        noteDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        //get the text in the editor
                        noteContentView.getCurrentHtmlAsync(new GetCurrentHtmlCallback() {
                            @Override
                            public void htmlRetrieved(@NotNull String s) {

                                //get the word and char count
                                int[] counts = getNumOfCharsAndWords(s);

                                //get the dates
                                NotebookContent note = task.getResult().toObject(NotebookContent.class);
                                Date creation = note.getCreatedTime();
                                Date modified = note.getLatestUpdateTime();

                                //pattern is:  Tue, 13 May 2011 14:23
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm E, dd MMM yyyy");

                                String creationStr = simpleDateFormat.format(creation);
                                String modifiedStr = simpleDateFormat.format(modified);

                                //build the string to display
                                String dialogMessage = "Created: " + creationStr + "\n\nModified: " + modifiedStr
                                        + "\n\nWord Count: " + counts[1] + "\n\nCharacters: " + counts[0];

                                showNoteInfoDialog(dialogMessage);
                            }

                            });

                        };

                });

    }

    private int[] getNumOfCharsAndWords(String totalText)
    {
        String plain;

        //convert html text to plain text
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            plain = Html.fromHtml(totalText, Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH).toString();
        }
        else {
            plain = Html.fromHtml(totalText).toString();
        }

        //arraylist to store matches
        ArrayList<String> listOfWords = new ArrayList<>();
        ArrayList<String> listOfChars = new ArrayList<>();

        //pattern to match words and characters
        Pattern wordSplitter = Pattern.compile("[A-Za-z]+");
        Pattern charSplitter = Pattern.compile("[A-Za-z]");

        //matching words
        Matcher wordMatcher = wordSplitter.matcher(plain);

        while (wordMatcher.find()) {
            listOfWords.add(wordMatcher.group());
        }

        //matching characters
        Matcher charMatcher = charSplitter.matcher(plain);

        while (charMatcher.find()) {
            listOfChars.add(charMatcher.group());
        }

        return new int[]{listOfChars.size(), listOfWords.size()};
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

    private void showUnlockWarningDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to remove lock?")
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        locked = false;
                        firestoreRepository.updateLockedStatus(notebookId, notebookContentId, false);
                        setLockedIcon(locked, activityMenu);
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


}