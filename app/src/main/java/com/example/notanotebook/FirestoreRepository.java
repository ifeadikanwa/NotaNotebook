package com.example.notanotebook;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreRepository {
    public static final String TAG = "FIRESTORE_REPOSITORY";
    private static FirestoreRepository instance;
    public static final String NAME_FIELD = "name";
    public static final String NOTES_FIELD = "notes";
    public static final String DATE_FIELD = "latestUpdateTime";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference notebookRef = db.collection("Notebooks");

    private FirestoreRepository(){
    }

    public static FirestoreRepository getInstance(){
        instance = new FirestoreRepository();
        return instance;
    }

    //TODO: add new notebook to database
    void addNotebook(String name) {
        DocumentReference documentReference =  notebookRef.document();

        int contents = 0;
        String documentId = documentReference.getId();

        Notebook notebook = new Notebook(documentId, name, contents, null, false);

        documentReference.set(notebook)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Notebook Created");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                    }
                });
    }

    //todo: update/edit notebook name
    void editNotebook(String documentId, String name){
        Map<String, Object> changes = new HashMap<>();
        changes.put(NAME_FIELD, name);
        changes.put(DATE_FIELD, FieldValue.serverTimestamp());

        notebookRef.document(documentId).update(changes)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Notebook Updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                    }
                });
    }

    void updateNotebookTimestamp(String notebookId){
        notebookRef.document(notebookId).update(DATE_FIELD, FieldValue.serverTimestamp())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                    }
                });
    }





}
