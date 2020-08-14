package com.example.notanotebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class InsideNotebookActivity extends AppCompatActivity implements NotebookCustomDialog.NotebookDialogInterface {
    String notebookId;
    String notebookName;

    FirestoreRepository firestoreRepository = FirestoreRepository.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inside_notebook);

        Intent intent = getIntent();
        notebookId = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_ID);
        notebookName = intent.getStringExtra(NotebookActivity.EXTRA_NOTEBOOK_NAME);

        setTitle(notebookName);
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
                //todo :on click open the custom dialog and UPDATE notebook title only
                NotebookCustomDialog notebookCustomDialog = new NotebookCustomDialog(notebookName);
                notebookCustomDialog.show(getSupportFragmentManager(), "Edit Notebook Title");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void createNotebook(String notebookTitle) {
        //todo call fireRepository query to update title
        firestoreRepository.editNotebook(notebookId, notebookTitle);
        Toast.makeText(this, "Notebook Title Updated", Toast.LENGTH_SHORT).show();

        //todo setTitle(new name)
        setTitle(notebookTitle);
    }
}