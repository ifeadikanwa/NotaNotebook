package com.ifyezedev.notanotebook;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;

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
    public static final String LOCKED_FIELD = "locked";
    public static final String NOTE_CONTENT_FIELD = "noteContent";
    public static final String CHECKLIST_ITEM_FIELD = "item";
    public static final String CHECKLIST_DOC_ID_FIELD = "item_id";
    public static final String ENTRY_TIME_FIELD = "entryTime";
    public static final String USER_ID_FIELD = "userID";
    public static final String USER_SECURITY_FIELD = "securitySet";
    final CollectionReference notebookRef = db.collection("Notebooks");
    final CollectionReference userRef = db.collection("Users");
    static final String NOTEBOOK_CONTENT_COLLECTION = "NotebookContent";
    static final String CHECKLIST_CONTENT_COLLECTION = "ChecklistContent";


    private FirestoreRepository() {
    }

    public static FirestoreRepository getInstance() {
        instance = new FirestoreRepository();
        return instance;
    }

    //done: add new notebook to database and scroll to start of recyclerview on success
    void addNotebook(String userID, String name, LinearLayoutManager layoutManager) {
        DocumentReference documentReference = notebookRef.document();

        int contents = 0;
        int color = Color.parseColor("#BDB76B");
        String documentId = documentReference.getId();

        Notebook notebook = new Notebook(userID, documentId, name, contents, color, null, false);

        documentReference.set(notebook)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Notebook Created");

                        //scroll to the atart of recyclerview
                        layoutManager.scrollToPosition(0);
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
    void editNotebook(String documentId, String name) {
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

    void updateNotebookTimestamp(String notebookId) {
        notebookRef.document(notebookId).update(DATE_FIELD, FieldValue.serverTimestamp())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                    }
                });
    }

    void increaseNotebookContentCount(String notebookId) {
        notebookRef.document(notebookId).update(CONTENTS_FIELD, FieldValue.increment(1));
    }

    void decreaseNotebookContentCount(String notebookId) {
        notebookRef.document(notebookId).update(CONTENTS_FIELD, FieldValue.increment(-1));
    }

    //done: add new note
    void createNewNote(final String notebookId, int color, String title, String noteContent) {
        DocumentReference notebookContentDocRef = notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION).document();

        NotebookContent content = new NotebookContent(notebookId, notebookContentDocRef.getId(), title, color, null, null, true, false, false);
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
    String createNewChecklist(final String notebookId, int color, String title) {
        DocumentReference notebookContentDocRef = notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION).document();

        String docId = notebookContentDocRef.getId();

        NotebookContent content = new NotebookContent(notebookId, docId, title, color, null, null, false, false, false);

        notebookContentDocRef.set(content)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //done: increment content field in notebook document
                        increaseNotebookContentCount(notebookId);
                        Log.i(TAG, "Checklist Created");
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
    void updateColor(String notebookId, final int color) {
        //Using batch to update color, so if one fails they all fail
        WriteBatch batch = db.batch();

        //first update the notebook color
        batch.update(notebookRef.document(notebookId), COLOR_FIELD, color);
//        notebookRef.document(notebookId).update(COLOR_FIELD, color);

        notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                //next update every notebook content
                                batch.update(documentSnapshot.getReference(), COLOR_FIELD, color);
//                                documentSnapshot.getReference().update(COLOR_FIELD, color);
                            }

                        }

                        //finally commit the batch when everything has been added to the batch
                        batch.commit().addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        });
                    }
                });

    }

    void updateNote(String notebookId, String notebookContentId, String title, String content) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(TITLE_FIELD, title);
        updates.put(NOTE_CONTENT_FIELD, content);

        notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId)
                .update(updates);

        updateNotebookContentTimestamp(notebookId, notebookContentId);
    }

    void updateChecklistTitle(String notebookId, String notebookContentId, String title) {
        notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION).document(notebookContentId)
                .update(TITLE_FIELD, title);
    }


    void addChecklistItem(String notebookId, String notebookContentId, String item) {
        Checklist_Item checklistItem = new Checklist_Item(item, false, null);

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

    void updateCheckedField(String notebookId, String notebookContentId, String checklistItemId, boolean checked) {
        notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId)
                .collection(CHECKLIST_CONTENT_COLLECTION)
                .document(checklistItemId)
                .update(CHECKED_FIELD, checked);
    }

    void updateChecklistItemText(String notebookId, String notebookContentId, String checklistItemId, String newText) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(CHECKLIST_ITEM_FIELD, newText);
        updates.put(ENTRY_TIME_FIELD, FieldValue.serverTimestamp());
        notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId)
                .collection(CHECKLIST_CONTENT_COLLECTION)
                .document(checklistItemId)
                .update(updates);

    }

    void updateNotebookContentTimestamp(String notebookId, String notebookContentId) {
        notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId)
                .update(DATE_FIELD, FieldValue.serverTimestamp());
    }

    void updatePinnedStatus(String notebookId, String notebookContentId, boolean pinned) {
        notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId)
                .update(PINNED_FIELD, pinned);
    }

    void updateLockedStatus(String notebookId, String notebookContentId, boolean locked) {
        notebookRef.document(notebookId)
                .collection(NOTEBOOK_CONTENT_COLLECTION)
                .document(notebookContentId)
                .update(LOCKED_FIELD, locked);
    }

    void deleteChecklist(DocumentReference documentReference) {
        documentReference.collection(CHECKLIST_CONTENT_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
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

    void deleteNote(DocumentReference documentReference) {
        documentReference.delete();
    }

    void deleteNotebook(DocumentReference documentReference) {
        documentReference.collection(NOTEBOOK_CONTENT_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot notebookContentDoc : task.getResult()) {
                                NotebookContent notebookContent = notebookContentDoc.toObject(NotebookContent.class);
                                if (notebookContent.isNote()) {
                                    deleteNote(notebookContentDoc.getReference());
                                } else {
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

    void addUser(String userID, boolean isSecuritySet) {
        Log.i(TAG, "userID: " + userID);

        Users user = new Users(userID, isSecuritySet);
        userRef.document(userID)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "New User added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                    }
                });
    }

    void updateSecurityField(String userID, boolean isSecuritySet){
        userRef.document(userID).update(USER_SECURITY_FIELD, isSecuritySet);
    }

}
