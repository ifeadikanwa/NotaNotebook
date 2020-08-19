package com.example.notanotebook;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

public class NotebookActivity extends AppCompatActivity implements NotebookCustomDialog.TitleDialogInterface {
    public static final String EXTRA_NOTEBOOK_ID = "com.example.notanotebook.EXTRA_NOTEBOOK_ID";
    public static final String EXTRA_NOTEBOOK_NAME = "com.example.notanotebook.EXTRA_NOTEBOOK_NAME";
    public static final String EXTRA_NOTEBOOK_COLOR = "com.example.notanotebook.EXTRA_NOTEBOOK_COLOR";
    public static final String EXTRA_NOTEBOOK_CONTENT_ID = "com.example.notanotebook.EXTRA_NOTEBOOK_CONTENT_ID";
    public static final String EXTRA_NOTEBOOK_CONTENT_TITLE = "com.example.notanotebook.EXTRA_NOTEBOOK_CONTENT_TITLE";
    public static final String EXTRA_NOTEBOOK_CONTENT = "com.example.notanotebook.EXTRA_NOTEBOOK_CONTENT";


    private NotebookViewModel notebookViewModel;
    private RecyclerView recyclerView;
    private NotebookAdapter adapter;
    private FloatingActionButton add_notebook;
    private FirestoreRepository firestoreRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);

        getSupportActionBar().hide();

        add_notebook = findViewById(R.id.add_notebook);
        add_notebook.setOnClickListener(addNotebook);

        notebookViewModel = new ViewModelProvider(this).get(NotebookViewModel.class);
        notebookViewModel.initialize();

        firestoreRepository = FirestoreRepository.getInstance();
        
        setUpRecyclerView();
    }



    private void setUpRecyclerView() {
        Query query = firestoreRepository.notebookRef.whereEqualTo("archive", false)
                .orderBy(FirestoreRepository.DATE_FIELD, Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Notebook> options = new FirestoreRecyclerOptions.Builder<Notebook>()
                .setQuery(query, Notebook.class)
                .build();

        adapter = new NotebookAdapter(options);

        recyclerView  = findViewById(R.id.notebook_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        LinearLayoutManager HorizontalLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(HorizontalLayout);
        recyclerView.setAdapter(adapter);

        //archive notebook on swipe
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.DOWN) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            //onSwipe open dialog that asks delete, archive or cancel
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                ShowWarningAlertDialog(viewHolder.getAdapterPosition());
            }
        })
        .attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new NotebookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Notebook notebook = documentSnapshot.toObject(Notebook.class);
                String notebookId = notebook.getNotebookId();
                String notebookName = notebook.getName();
                int notebookColor = notebook.getColor();

                firestoreRepository.updateNotebookTimestamp(notebookId);

                Intent intent = new Intent(NotebookActivity.this, NotebookContentActivity.class);
                intent.putExtra(EXTRA_NOTEBOOK_NAME, notebookName);
                intent.putExtra(EXTRA_NOTEBOOK_ID, notebookId);
                intent.putExtra(EXTRA_NOTEBOOK_COLOR, String.valueOf(notebookColor));
                startActivity(intent);
            }
        });

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

    //OnClickListener for floating action button the adds new notebook
     View.OnClickListener addNotebook = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //we want to open the custom dialog on click of this button
            NotebookCustomDialog dialog = new NotebookCustomDialog();
            dialog.show(getSupportFragmentManager(), "Notebook Custom Dialog");
        }
    };

    //We collect data sent from the custom dialog and we use it to create a new notebook
    @Override
    public void createNotebook(String notebookTitle) {
        firestoreRepository.addNotebook(notebookTitle);
    }

    //AlertDialog for deleting or archiving notebook.
    private void ShowWarningAlertDialog(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(NotebookActivity.this);
        builder.setMessage("What would you like to do?")
                .setPositiveButton("Archive", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //done: archive notebook
                        adapter.archiveNotebook(position);
                        Toast.makeText(NotebookActivity.this, "Notebook Archived", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //done: delete notebook
                        adapter.deleteNotebook(position);
                        Toast.makeText(NotebookActivity.this, "Notebook Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .create();

        builder.show();

    }
}