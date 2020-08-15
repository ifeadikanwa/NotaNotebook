package com.example.notanotebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

import petrov.kristiyan.colorpicker.ColorPicker;

public class NotebookContentActivity extends AppCompatActivity implements NotebookCustomDialog.NotebookDialogInterface {
    String notebookId;
    String notebookName;
    String notebookColor;

    CardView createNote;
    CardView createChecklist;

    //reference to ColorPicker class
    TheColors theColors = new TheColors();
    ArrayList<String> colors = new ArrayList<>();

    ActionBar actionBar;

    NotebookContentAdapter adapter;

    FirestoreRepository firestoreRepository = FirestoreRepository.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook_content);

        Intent intent = getIntent();
        notebookId = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_ID);
        notebookName = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_NAME);
        notebookColor = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_COLOR);

        //set the action bar title
        setTitle(notebookName);

        //set the action bar color
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Integer.parseInt(notebookColor));
        actionBar.setBackgroundDrawable(colorDrawable);

        createNote = findViewById(R.id.add_note);
        createChecklist = findViewById(R.id.add_checklist);

        //set the colors for the picker
        colors = theColors.getColors();

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        Query query = firestoreRepository.notebookRef.document(notebookId)
                .collection(firestoreRepository.NOTEBOOK_CONTENT_COLLECTION)
                .orderBy(FirestoreRepository.PRIORITY_FIELD, Query.Direction.DESCENDING)
                .orderBy(FirestoreRepository.DATE_FIELD, Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<NotebookContent> options = new FirestoreRecyclerOptions.Builder<NotebookContent>()
                .setQuery(query, NotebookContent.class)
                .build();

        adapter = new NotebookContentAdapter(options);

        RecyclerView recyclerView  = findViewById(R.id.notebook_content_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        LinearLayoutManager VerticalLayout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(VerticalLayout);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new NotebookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                //todo: open note view activity
            }
        });

    }

    //todo: onclicklistener for createNote
    public void createNote(View view){

    }

    //todo: onclicklistener for createChecklist
    public void createChecklist(View view){

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_inside_notebook, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.edit_notebook_title:
                //done:on click open the custom dialog and UPDATE notebook title only
                NotebookCustomDialog notebookCustomDialog = new NotebookCustomDialog(notebookName);
                notebookCustomDialog.show(getSupportFragmentManager(), "Edit Notebook Title");
                return true;

            case R.id.edit_notebook_color:
                //done: open colorpicker, display color palette and take action on selection
                //done: update the color in firestore, update the titlebar color, and the list items
                changeColor();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    //opens color palette dialog box and updates color on user selection
    void changeColor(){
        ColorPicker colorPicker = new ColorPicker(this);

        colorPicker.setColors(colors)
                .setColumns(5)
                .setRoundColorButton(true)
                .setTitle("Pick a color")
                .setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                    @Override
                    public void onChooseColor(int position, int color) {
                        //on choose color we want to update action bar, notebook doc and notebookcontent doc
                        ColorDrawable colorDrawable = new ColorDrawable(color);
                        actionBar.setBackgroundDrawable(colorDrawable);

                        firestoreRepository.updateColor(notebookId, color);

                    }

                    @Override
                    public void onCancel() {

                    }
                })
                .show();
    }


    //updates notebook title
    @Override
    public void createNotebook(String notebookTitle) {
        //done: call fireRepository query to update title
        firestoreRepository.editNotebook(notebookId, notebookTitle);
        Toast.makeText(this, "Notebook Title Updated", Toast.LENGTH_SHORT).show();

        //done: setTitle(new name)
        setTitle(notebookTitle);
    }
}