package com.example.notanotebook;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FirestoreRepository {
    public static final String TAG = "FIRESTORE_REPOSITORY";
    private static FirestoreRepository instance;
    public static final String NAME_FIELD = "name";
    public static final String CONTENTS_FIELD = "contents";
    public static final String DATE_FIELD = "latestUpdateTime";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference notebookRef = db.collection("Notebooks");
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
    void createNewNote(final String notebookId, String title, String noteContent){
        DocumentReference notebookContentDocRef = notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION).document();

        NotebookContent content = new NotebookContent(notebookId,notebookContentDocRef.getId(), title,null,null,true);
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

    //done: add new to-do
    void createNewTodo(final String notebookId, String title, List<String> todoContent){
        DocumentReference notebookContentDocRef = notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION).document();

        NotebookContent content = new NotebookContent(notebookId, notebookContentDocRef.getId(), title,null,null,false);
        content.setTodoContent(todoContent);

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


}
