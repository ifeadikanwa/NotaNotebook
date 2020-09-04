package com.example.notanotebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChecklistEditActivity extends AppCompatActivity implements ChecklistCustomDialog.TitleDialogInterface, Checklist_Item_Dialog.TextDialogInterface {
    public static final String TAG = ChecklistEditActivity.class.getSimpleName();
    private static final int LOCK_CHECKLIST_REQUEST_CODE = 333;
    private String notebookId;
    private String notebookContentId;
    private String notebookContentTitle;
    boolean pinned;
    boolean locked;
    private Menu activityMenu;
    DocumentReference ChecklistDocRef;
    CollectionReference ChecklistCollRef;
    FirestoreRepository firestoreRepository;
    RecyclerView recyclerView;
    ChecklistAdapter adapter;
    ImageButton add_item_button;
    TextInputEditText item_edit_text;
    ImageButton edit_title;
    TextInputEditText titleView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //make the status bar white with black icons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setTitle("");

        setContentView(R.layout.activity_checklist_edit);

        Intent intent = getIntent();
        notebookId = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_ID);
        notebookContentId = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_ID);
        notebookContentTitle = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_CONTENT_TITLE);
        pinned = intent.getBooleanExtra(NotebookActivity.EXTRA_PINNED_STATUS, false);
        locked = intent.getBooleanExtra(NotebookActivity.EXTRA_LOCKED_STATUS, false);


        firestoreRepository = FirestoreRepository.getInstance();
        add_item_button = findViewById(R.id.add_checklist_entry);
        item_edit_text = findViewById(R.id.checklist_edittext);
        edit_title = findViewById(R.id.edit_checklist_title);
        titleView = findViewById(R.id.checklist_title_view);

        titleView.setKeyListener(null);

        titleView.setText(notebookContentTitle);

        edit_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //done: open custom dialog to edit title, update action bar and database
                ChecklistCustomDialog checklistCustomDialog = new ChecklistCustomDialog(notebookContentTitle);
                checklistCustomDialog.show(getSupportFragmentManager(), "Edit Title");
            }
        });

        ChecklistDocRef = firestoreRepository.notebookRef
                .document(notebookId)
                .collection(FirestoreRepository.NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId);

        ChecklistCollRef = firestoreRepository.notebookRef
                .document(notebookId)
                .collection(FirestoreRepository.NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId)
                .collection(FirestoreRepository.CHECKLIST_CONTENT_COLLECTION);

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        Query query = firestoreRepository.notebookRef
                .document(notebookId)
                .collection(FirestoreRepository.NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId)
                .collection(FirestoreRepository.CHECKLIST_CONTENT_COLLECTION)
                .orderBy(FirestoreRepository.CHECKED_FIELD);

        FirestoreRecyclerOptions<Checklist_Item> options = new FirestoreRecyclerOptions.Builder<Checklist_Item>()
                .setQuery(query, Checklist_Item.class)
                .build();

        adapter = new ChecklistAdapter(options);

        recyclerView  = findViewById(R.id.checklist_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        LinearLayoutManager VerticalLayout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(VerticalLayout);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ChecklistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                //done: open dialog to edit checklist entry
                Checklist_Item checklistItem = documentSnapshot.toObject(Checklist_Item.class);
                Checklist_Item_Dialog checklist_item_dialog = new Checklist_Item_Dialog(checklistItem.getItem_id(), checklistItem.getItem());
                checklist_item_dialog.show(getSupportFragmentManager(), "Edit Checklist Item");
            }

            @Override
            public void onCheckboxClick(DocumentSnapshot documentSnapshot, int position, boolean checked) {
                //done set checked to true;
                Checklist_Item checklistItem = documentSnapshot.toObject(Checklist_Item.class);
                firestoreRepository.updateCheckedField(notebookId, notebookContentId, checklistItem.getItem_id(), checked);

            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //done: delete checklist entry(DOCUMENT AT THAT POSITION)
                adapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);
    }


    //add checklist item on click of the attached view
    public void add_item(View view){
        String item_text = item_edit_text.getText().toString();
        String text_trimmed = item_text.trim();
        if(!(text_trimmed.length() == 0)){
            firestoreRepository.addChecklistItem(notebookId, notebookContentId, item_text);
            item_edit_text.setText("");
        }
    }




    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //get an instance of activity's menu and save to global variable
        activityMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_checklist_edit, menu);

        //show icon for items in menu overflow
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        //set correct pin and lock icon on menu creation
        setPinnedIcon(pinned, menu);
        setLockedIcon(locked, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.pin_checklist:
                //done: pin or unpin on click
                pinChecklistAction();
                return true;
            case R.id.lock_checklist:
                //done: lock or unlock checklist
                lockChecklistAction();
                return true;
            case R.id.delete_checklist:
                //done: delete checklist
                showDeleteWarningDialog();
                return true;
            case R.id.checklist_info:
                //done: show dates for checklist
                getChecklistInfo();
                return true;
            case R.id.share_checklist:
                //done: share checklist
                shareChecklistAction();
                return true;
            case R.id.done_button:
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //get and share share checklist
    private void shareChecklistAction(){

        ChecklistCollRef.orderBy(FirestoreRepository.CHECKED_FIELD)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        StringBuilder checklistBuilder = new StringBuilder();
                        checklistBuilder.append(notebookContentTitle).append("\n");
                        if(task.getResult() != null && !(task.getResult().isEmpty())){
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                Checklist_Item item = documentSnapshot.toObject(Checklist_Item.class);
                                if(item.isChecked()){
                                    checklistBuilder.append("◼ ").append(item.getItem()).append("\n");
                                }
                                else{
                                    checklistBuilder.append("◻ ").append(item.getItem()).append("\n");
                                }
                            }

                            String checklist = checklistBuilder.toString();
                            shareChecklistIntent(checklist);
                        }
                        else {
                            Toast.makeText(ChecklistEditActivity.this, "Can't share empty checklist", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, e.toString());
                    }
                });
    }

    private void shareChecklistIntent(String checklist) {
        if(checklist != null && checklist.trim().length() != 0){
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, checklist);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }
        else{
            Toast.makeText(ChecklistEditActivity.this, "Can't share empty checklist", Toast.LENGTH_SHORT).show();
        }

    }

    //get and display checklist info
    private void getChecklistInfo(){

        ChecklistDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        NotebookContent note = task.getResult().toObject(NotebookContent.class);
                        Date creation = note.getCreatedTime();
                        Date modified = note.getLatestUpdateTime();

                        //pattern is:  Tue, 13 May 2011 14:23
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm E, dd MMM yyyy");

                        String creationStr = simpleDateFormat.format(creation);
                        String modifiedStr = simpleDateFormat.format(modified);

                        String dialogMessage = "Created: " + creationStr + "\n\nModified: " + modifiedStr;

                        showChecklistInfoDialog(dialogMessage);
                    }
                });

    }


    //alert dialog for displaying checklist info
    private void showChecklistInfoDialog(String dialogMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Checklist Information")
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


    //lock or unlock checklist
    private void lockChecklistAction() {
        if(locked){
            //if note is locked we want to UNLOCK
            showUnlockWarningDialog();
        }
        else{
            //if note is unlocked we want to LOCK
            Intent intent = new Intent(ChecklistEditActivity.this, PinLockScreenActivity.class);
            intent.putExtra(NotebookActivity.EXTRA_FROM_VIEW_ACTIVITY, true);
            startActivityForResult(intent, LOCK_CHECKLIST_REQUEST_CODE);
        }
    }

    //alert dialog for unlocking checklist
    private void showUnlockWarningDialog() {
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

    //pin or unpin checklist
    private void pinChecklistAction() {
        //make pinned equal to the opposite of its current value
        pinned = !pinned;
        //update pinned status in firestore
        firestoreRepository.updatePinnedStatus(notebookId, notebookContentId, pinned);
        //update pinned icon
        setPinnedIcon(pinned, activityMenu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == LOCK_CHECKLIST_REQUEST_CODE && resultCode == RESULT_OK){
            //done: update global variable, firestore and icon
            locked = true;
            firestoreRepository.updateLockedStatus(notebookId, notebookContentId, locked);
            setLockedIcon(locked, activityMenu);
        }
    }

    //set pin icon depending on current pinned status of checklist
    private void setPinnedIcon(boolean pinned, Menu menu){
        //get the pin menu item and set icon based on pinned status in firestore
        MenuItem menuItem = menu.findItem(R.id.pin_checklist);
        if(pinned){
            menuItem.setIcon(R.drawable.ic_pinned);
            menuItem.setTitle("Unpin checklist");
        }
        else{
            menuItem.setIcon(R.drawable.ic_not_pinned);
            menuItem.setTitle("Pin checklist");
        }
    }

    //set pin icon depending on current locked status of checklist
    private void setLockedIcon(boolean locked, Menu menu){
        //get the lock menu item and set icon based on pinned status in firestore
        MenuItem menuItem = menu.findItem(R.id.lock_checklist);
        if(locked){
            menuItem.setIcon(R.drawable.ic_locked);
            menuItem.setTitle("Unlock checklist");
        }
        else{
            menuItem.setIcon(R.drawable.ic_unlocked);
            menuItem.setTitle("Lock checklist");
        }
    }

    private void showDeleteWarningDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to delete?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        firestoreRepository.deleteNote(ChecklistDocRef);
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


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    //this method is used to update the checklist title in firestore and the titleView
    @Override
    public void createChecklist(String Title) {
        //done: update title in titleView and firestore
        firestoreRepository.updateChecklistTitle(notebookId, notebookContentId, Title);

        notebookContentTitle = Title;

        titleView.setText(notebookContentTitle);
    }


    @Override
    public void updateChecklistEntry(String item_id, String Text) {
        firestoreRepository.updateChecklistItemText(notebookId, notebookContentId, item_id, Text);
    }
}