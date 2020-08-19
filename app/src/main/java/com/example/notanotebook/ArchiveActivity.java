package com.example.notanotebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

public class ArchiveActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotebookAdapter adapter;
    private FirestoreRepository firestoreRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        getSupportActionBar().hide();

        firestoreRepository = FirestoreRepository.getInstance();

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        Query query = firestoreRepository.notebookRef.whereEqualTo("archive", true)
                .orderBy(FirestoreRepository.DATE_FIELD, Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Notebook> options = new FirestoreRecyclerOptions.Builder<Notebook>()
                .setQuery(query, Notebook.class)
                .build();

        adapter = new NotebookAdapter(options);

        recyclerView  = findViewById(R.id.archive_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        LinearLayoutManager HorizontalLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(HorizontalLayout);
        recyclerView.setAdapter(adapter);

        //unarchive or delete notebook on swipe
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.DOWN) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            //onSwipe open dialog that asks delete, unarchive
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                ShowWarningAlertDialog(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);


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


    //AlertDialog for deleting or archiving notebook.
    private void ShowWarningAlertDialog(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(ArchiveActivity.this);
        builder.setMessage("What would you like to do?")
                .setPositiveButton("Unarchive", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //done: archive notebook
                        adapter.unArchiveNotebook(position);
                        Toast.makeText(ArchiveActivity.this, "Notebook Unarchived", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //done: delete notebook
                        adapter.deleteNotebook(position);
                        Toast.makeText(ArchiveActivity.this, "Notebook Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        adapter.putBack(position);
                    }
                })
                .create();

        builder.show();

    }
}