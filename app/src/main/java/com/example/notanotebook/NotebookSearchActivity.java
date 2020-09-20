package com.example.notanotebook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;

import org.json.JSONObject;

public class NotebookSearchActivity extends AppCompatActivity {
    SearchView searchView;
    ListView listView;

    Client client;
    Index notebookIndex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook_search);

        getSupportActionBar().hide();

        //initialize searchview and attach querytextlistener
        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(searchListener);

        listView = findViewById(R.id.notebook_search_listView);

        //initialize client and get index
        client = new Client(BuildConfig.API_CLIENT_ID, BuildConfig.API_CLIENT_KEY);
        notebookIndex = client.getIndex("notebook_NAME");
    }

    SearchView.OnQueryTextListener searchListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            //Use the text to create a query to retrieve mentioned data
            Query query = new Query(s)
                    .setAttributesToRetrieve("NotebookId", "Notebook Name", "Color", "Contents");
            notebookIndex.searchAsync(query, new CompletionHandler() {
                @Override
                public void requestCompleted(JSONObject content, AlgoliaException error) {
                    //use the retrieved data to populate the listview
                }
            });
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };
}