package com.example.notanotebook;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
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
    public static final String TITLE_FIELD = "title";
    public static final String CHECKED_FIELD = "checked";
    public static final String PINNED_FIELD = "pinned";
    public static final String NOTE_CONTENT_FIELD = "noteContent";
    public static final String CHECKLIST_ITEM_FIELD = "item";
    public static final String CHECKLIST_DOC_ID_FIELD = "item_id";
    final CollectionReference notebookRef = db.collection("Notebooks");
    static final String NOTEBOOK_CONTENT_COLLECTION = "Notebook Content";
    static final String CHECKLIST_CONTENT_COLLECTION = "Checklist Content";


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

    void increaseNotebookContentCount(String notebookId){
        notebookRef.document(notebookId).update(CONTENTS_FIELD, FieldValue.increment(1));
    }

    void decreaseNotebookContentCount(String notebookId){
        notebookRef.document(notebookId).update(CONTENTS_FIELD, FieldValue.increment(-1));
    }

    //done: add new note
    void createNewNote(final String notebookId, int color, String title, String noteContent){
        DocumentReference notebookContentDocRef = notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION).document();

        NotebookContent content = new NotebookContent(notebookId,notebookContentDocRef.getId(), title, color, null,null,true, false);
        content.setNoteContent(noteContent);

        notebookContentDocRef.set(content)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //done: increment content field in notebook document
                        increaseNotebookContentCount(notebookId);
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
    String createNewChecklist(final String notebookId, int color, String title){
        DocumentReference notebookContentDocRef = notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION).document();

        String docId = notebookContentDocRef.getId();

        NotebookContent content = new NotebookContent(notebookId, docId, title, color, null,null,false, false);

        notebookContentDocRef.set(content)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //done: increment content field in notebook document
                        increaseNotebookContentCount(notebookId);
                        Log.i(TAG, "Todo Created");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                    }
                });

        return docId;
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

    void updateNote(String notebookId, String notebookContentId, String title, String content){
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(TITLE_FIELD, title);
        updates.put(NOTE_CONTENT_FIELD, content);

        notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId)
                .update(updates);

        updateNotebookContentTimestamp(notebookId, notebookContentId);
    }

    void updateChecklistTitle(String notebookId, String notebookContentId, String title){
        notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION).document(notebookContentId)
                .update(TITLE_FIELD, title);
    }


    void addChecklistItem(String notebookId, String notebookContentId, String item){
        Checklist_Item checklistItem = new Checklist_Item(item, false);

        notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId)
                .collection(CHECKLIST_CONTENT_COLLECTION)
                .add(checklistItem)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String id = documentReference.getId();
                        documentReference.update(CHECKLIST_DOC_ID_FIELD, id);
                        Log.i(TAG, "Item Added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                    }
                });
    }

    void updateCheckedField(String notebookId, String notebookContentId, String checklistItemId, boolean checked){
        notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId)
                .collection(CHECKLIST_CONTENT_COLLECTION)
                .document(checklistItemId)
                .update(CHECKED_FIELD, checked);
    }

    void updateChecklistItemText(String notebookId, String notebookContentId, String checklistItemId, String newText){
        notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId)
                .collection(CHECKLIST_CONTENT_COLLECTION)
                .document(checklistItemId)
                .update(CHECKLIST_ITEM_FIELD, newText);
    }

    void updateNotebookContentTimestamp(String notebookId, String notebookContentId){
        notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId)
                .update(DATE_FIELD, FieldValue.serverTimestamp());
    }

    void updatePinnedStatus(String notebookId, String notebookContentId, boolean pinned){
        notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId)
                .update(PINNED_FIELD, pinned);
    }

    void deleteChecklist(DocumentReference documentReference){
         documentReference.collection(CHECKLIST_CONTENT_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult() != null){
                                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                    queryDocumentSnapshot.getReference().delete();
                                }
                            }
                            documentReference.delete();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                    }
                });

    }

    void deleteNote(DocumentReference documentReference){
        documentReference.delete();
    }

    void deleteNotebook(DocumentReference documentReference){
        documentReference.collection(NOTEBOOK_CONTENT_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult() != null){
                            for (QueryDocumentSnapshot notebookContentDoc : task.getResult()) {
                                NotebookContent notebookContent = notebookContentDoc.toObject(NotebookContent.class);
                                if(notebookContent.isNote()){
                                    deleteNote(notebookContentDoc.getReference());
                                }
                                else{
                                    deleteChecklist(notebookContentDoc.getReference());
                                }
                            }
                        }
                        documentReference.delete();
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
