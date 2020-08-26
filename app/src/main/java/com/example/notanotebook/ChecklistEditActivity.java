package com.example.notanotebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

public class ChecklistEditActivity extends AppCompatActivity implements ChecklistCustomDialog.TitleDialogInterface, Checklist_Item_Dialog.TextDialogInterface {
    private String notebookId;
    private String notebookContentId;
    private String notebookContentTitle;
    boolean pinned;
    private Menu activityMenu;
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



    public void add_item(View view){
        String item_text = item_edit_text.getText().toString();
        String text_trimmed = item_text.trim();
        if(!(text_trimmed.length() == 0)){
            firestoreRepository.addChecklistItem(notebookId, notebookContentId, item_text);
            item_edit_text.setText("");
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //get an instance of activity's menu and save to global variable
        activityMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_checklist_edit, menu);

        //set correct pin icon on menu creation
        setPinnedIcon(pinned, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.pin_checklist:
                //todo: pin or unpin on click
                pinChecklistAction();
                return true;
            case R.id.done_button:
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    //set pin icon depending on current pinned status of checklist
    private void setPinnedIcon(boolean pinned, Menu menu){
        //get the pin menu item and set icon based on pinned status in firestore
        MenuItem menuItem = menu.findItem(R.id.pin_checklist);
        if(pinned){
            menuItem.setIcon(R.drawable.ic_pinned);
        }
        else{
            menuItem.setIcon(R.drawable.ic_not_pinned);
        }
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

    @Override
    public void createChecklist(String Title) {
        //done: update title in titlebar and firestore
        firestoreRepository.updateChecklistTitle(notebookId, notebookContentId, Title);

        notebookContentTitle = Title;

        titleView.setText(notebookContentTitle);
    }


    @Override
    public void updateChecklistEntry(String item_id, String Text) {
        firestoreRepository.updateChecklistItemText(notebookId, notebookContentId, item_id, Text);
    }
}