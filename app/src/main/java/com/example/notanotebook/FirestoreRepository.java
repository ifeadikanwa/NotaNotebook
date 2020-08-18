package com.example.notanotebook;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirestoreRepository {
    public static final String TAG = "FIRESTORE_REPOSITORY";
    private static FirestoreRepository instance;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static final String NAME_FIELD = "name";
    public static final String CONTENTS_FIELD = "contents";
    public static final String DATE_FIELD = "latestUpdateTime";
    public static final String COLOR_FIELD = "color";
    public static final String PRIORITY_FIELD = "priority";
    final CollectionReference notebookRef = db.collection("Notebooks");
    final String NOTEBOOK_CONTENT_COLLECTION = "Notebook Content";

    private FirestoreRepository(){
    }

    public static FirestoreRepository getInstance(){
        instance = new FirestoreRepository();
        return instance;
    }

    //done: add new notebook to database
    void addNotebook(String name) {
        DocumentReference documentReference =  notebookRef.document();

        int contents = 0;
        int color = Color.parseColor("#1E90FF");
        String documentId = documentReference.getId();

        Notebook notebook = new Notebook(documentId, name, contents, color,  null, false);

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

    //done: update/edit notebook name
    void editNotebook(String documentId, String name){
        notebookRef.document(documentId).update(NAME_FIELD, name)
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

    //done: add new note
    void createNewNote(final String notebookId, int color, String title, String noteContent){
        DocumentReference notebookContentDocRef = notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION).document();

        NotebookContent content = new NotebookContent(notebookId,notebookContentDocRef.getId(), title, color, 0,null,null,true);
        content.setNoteContent(noteContent);

        notebookContentDocRef.set(content)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //done: increment content field in notebook document
                        notebookRef.document(notebookId).update(CONTENTS_FIELD, FieldValue.increment(1));
                        Log.i(TAG, "Note Created");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                    }
                });
    }

    //done: add new checklist
    void createNewChecklist(final String notebookId, int color, String title, List<String> checklistContent){
        DocumentReference notebookContentDocRef = notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION).document();

        NotebookContent content = new NotebookContent(notebookId, notebookContentDocRef.getId(), title, color, 0,null,null,false);
        content.setChecklistContent(checklistContent);

        notebookContentDocRef.set(content)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //done: increment content field in notebook document
                        notebookRef.document(notebookId).update(CONTENTS_FIELD, FieldValue.increment(1));
                        Log.i(TAG, "Todo Created");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                    }
                });
    }

    //update color of notebook and notebook content
    void updateColor(String notebookId, final int color){
            notebookRef.document(notebookId).update(COLOR_FIELD, color);

            notebookRef.document(notebookId)
                    .collection(NOTEBOOK_CONTENT_COLLECTION).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                if (documentSnapshot.exists()) {
                                    documentSnapshot.getReference().update(COLOR_FIELD, color);
                                }

                            }
                        }
                    });

    }


}
