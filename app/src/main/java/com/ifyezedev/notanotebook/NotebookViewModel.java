package com.ifyezedev.notanotebook;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotebookViewModel extends ViewModel {
    private FirestoreRepository firestoreRepository;
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    public CollectionReference notebookRef = db.collection("Notebooks");

    public void initialize(){
        if(firestoreRepository == null){
            firestoreRepository = FirestoreRepository.getInstance();
        }
    }


}
