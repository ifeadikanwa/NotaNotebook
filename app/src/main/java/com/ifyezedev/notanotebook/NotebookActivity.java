package com.ifyezedev.notanotebook;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotebookActivity extends AppCompatActivity implements NotebookCustomDialog.TitleDialogInterface {
    public static final String EXTRA_NOTEBOOK_ID = "com.ifyezedev.notanotebook.EXTRA_NOTEBOOK_ID";
    public static final String EXTRA_NOTEBOOK_NAME = "com.ifyezedev.notanotebook.EXTRA_NOTEBOOK_NAME";
    public static final String EXTRA_NOTEBOOK_COLOR = "com.ifyezedev.notanotebook.EXTRA_NOTEBOOK_COLOR";
    public static final String EXTRA_NOTEBOOK_CONTENT_ID = "com.ifyezedev.notanotebook.EXTRA_NOTEBOOK_CONTENT_ID";
    public static final String EXTRA_NOTEBOOK_CONTENT_TITLE = "com.ifyezedev.notanotebook.EXTRA_NOTEBOOK_CONTENT_TITLE";
    public static final String EXTRA_NOTEBOOK_CONTENT = "com.ifyezedev.notanotebook.EXTRA_NOTEBOOK_CONTENT";
    public static final String EXTRA_FROM_VIEW_ACTIVITY = "com.ifyezedev.notanotebook.EXTRA_FROM_VIEW_ACTIVITY";
    public static final String EXTRA_FROM_SHARE_ACTIVITY = "com.ifyezedev.notanotebook.EXTRA_FROM_VIEW_ACTIVITY";
    public static final String EXTRA_PINNED_STATUS = "com.ifyezedev.notanotebook.EXTRA_PINNED_STATUS";
    public static final String EXTRA_LOCKED_STATUS = "com.ifyezedev.notanotebook.EXTRA_LOCKED_STATUS";
    public static final String EXTRA_IS_NOTE = "com.ifyezedev.notanotebook.EXTRA_IS_NOTE";
    private NotebookViewModel notebookViewModel;
    private RecyclerView recyclerView;
    private NotebookAdapter adapter;
    TextView labelText;
    ImageButton archiveButton;
    ImageButton helpButton;
    ImageButton menuButton;
    private FirestoreRepository firestoreRepository;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String sharedTitle;
    String sharedContent;
    boolean share;
    private Client client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);

        getSupportActionBar().hide();

        FloatingActionButton add_notebook = findViewById(R.id.add_notebook);
        add_notebook.setOnClickListener(addNotebook);

        recyclerView  = findViewById(R.id.notebook_recyclerview);

        labelText = findViewById(R.id.textView);

        archiveButton = findViewById(R.id.archive_button);
        archiveButton.setOnClickListener(archiveListener);

        helpButton = findViewById(R.id.help_button);
        menuButton = findViewById(R.id.menu_button);

        notebookViewModel = new ViewModelProvider(this).get(NotebookViewModel.class);
        notebookViewModel.initialize();

        firestoreRepository = FirestoreRepository.getInstance();

        Intent intent = getIntent();
        if(intent != null){
            share = intent.getBooleanExtra(EXTRA_FROM_SHARE_ACTIVITY, false);
            if(share){
                sharedTitle = intent.getStringExtra(EXTRA_NOTEBOOK_CONTENT_TITLE);
                sharedContent = intent.getStringExtra(EXTRA_NOTEBOOK_CONTENT);

                labelText.setText(R.string.choose_notebook);
                archiveButton.setVisibility(View.GONE);
                helpButton.setVisibility(View.GONE);
                menuButton.setVisibility(View.GONE);
            }

        }

        setUpRecyclerView();

        //TODO: this is for Algolia Search, *Setup at the end*
//        client = new Client(BuildConfig.API_CLIENT_ID, BuildConfig.API_CLIENT_KEY);
//        addNotebookToIndex();
    }


    private void setUpRecyclerView() {
        Query query = firestoreRepository.notebookRef
                .whereEqualTo("archive", false)
                .whereEqualTo(FirestoreRepository.USER_ID_FIELD, user.getUid())
                .orderBy(FirestoreRepository.DATE_FIELD, Query.Direction.DESCENDING);


        FirestoreRecyclerOptions<Notebook> options = new FirestoreRecyclerOptions.Builder<Notebook>()
                .setQuery(query, Notebook.class)
                .build();

        adapter = new NotebookAdapter(options);

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

            //onSwipe open dialog that asks delete, archive
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

                //if we are dealing with a share intent, we want to create a new note with
                //data the user wants to save in the notebook they clicked.
                if(share){
                    firestoreRepository.createNewNote(notebookId, notebookColor, sharedTitle, sharedContent);
                    firestoreRepository.updateNotebookTimestamp(notebookId);
                    Toast.makeText(NotebookActivity.this, "Note Saved", Toast.LENGTH_SHORT).show();
                    finish();
                }
                //open notebook content activity and display notebook contents
                else{
                    firestoreRepository.updateNotebookTimestamp(notebookId);

                    Intent intent = new Intent(NotebookActivity.this, NotebookContentActivity.class);
                    intent.putExtra(EXTRA_NOTEBOOK_NAME, notebookName);
                    intent.putExtra(EXTRA_NOTEBOOK_ID, notebookId);
                    intent.putExtra(EXTRA_NOTEBOOK_COLOR, String.valueOf(notebookColor));
                    startActivity(intent);
                }


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


    //OnClickListener for archive button, opens archive activity
    View.OnClickListener archiveListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(NotebookActivity.this, ArchiveActivity.class);
            startActivity(intent);
        }
    };


    //We collect data sent from the custom dialog and we use it to create a new notebook
    @Override
    public void createNotebook(String notebookTitle) {
        //get the linearlayoutmanger of the recyclerview
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        firestoreRepository.addNotebook(user.getUid(), notebookTitle, layoutManager);

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
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        adapter.putBack(position);
                    }
                })
                .create();

        builder.show();

    }




    private void addNotebookToIndex(){
        Index index = client.getIndex("notebook_NAME");
        List<JSONObject> array = new ArrayList<JSONObject>();

        firestoreRepository.notebookRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult() != null && !task.getResult().isEmpty()){
                            Log.e("Index", "not null");
                            for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                Log.e("Index", "IN FOR LOOP");
                                Notebook notebook = queryDocumentSnapshot.toObject(Notebook.class);
                                try {
                                    Log.e("Index", "IN TRY CATCH");
                                    array.add( new JSONObject().put("NotebookId", notebook.getNotebookId()).put("Notebook Name", notebook.getName()).put("Color", notebook.getColor()).put("Contents", notebook.getContents()));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                            index.addObjectsAsync(new JSONArray(array), null);
                        }
                    }
                });



    }
}