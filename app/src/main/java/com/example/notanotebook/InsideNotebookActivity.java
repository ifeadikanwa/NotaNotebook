package com.example.notanotebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class InsideNotebookActivity extends AppCompatActivity {
    String notebookId;
    String notebookName;
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}