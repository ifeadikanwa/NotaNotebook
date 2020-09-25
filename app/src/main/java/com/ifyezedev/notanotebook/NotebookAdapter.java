package com.ifyezedev.notanotebook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;


public class NotebookAdapter extends FirestoreRecyclerAdapter<Notebook, NotebookAdapter.NotebookHolder> {
    private OnItemClickListener listener;


    public NotebookAdapter(@NonNull FirestoreRecyclerOptions<Notebook> options) {
        super(options);
    }

    class NotebookHolder extends RecyclerView.ViewHolder {
        TextView notebookTitle;
        TextView notesCount;
        CardView notebookCard;

        public NotebookHolder(@NonNull View itemView) {
            super(itemView);
            notebookTitle = itemView.findViewById(R.id.notebookTitle);
            notesCount = itemView.findViewById(R.id.notesCount);
            notebookCard = itemView.findViewById(R.id.notebookCard);

            //we are going to set onclicklistener on the cardview
            // and then send it to the activity displaying the card to decide what happens on click.
            //to do this we create an interface
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }

    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }




    /**
     * @param model represents the notebook object about to be put in the list at position
     **/
    @Override
    protected void onBindViewHolder(@NonNull NotebookHolder holder, int position, @NonNull Notebook model) {
        holder.notebookTitle.setText(model.getName());

        int contentsSize = model.getContents();
        String noteCount;
        if(contentsSize < 2){
            noteCount = String.valueOf(model.getContents()) + " note";
        }
        else {
            noteCount = String.valueOf(model.getContents()) + " notes";
        }

        holder.notesCount.setText(noteCount);

        holder.notebookCard.setCardBackgroundColor(model.getColor());
    }

    @NonNull
    @Override
    public NotebookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notebook_item_view, parent, false);
        return new NotebookHolder(v);
    }

    //delete notebook on swipe
    public void deleteNotebook(int position) {
        FirestoreRepository firestoreRepository = FirestoreRepository.getInstance();
        firestoreRepository.deleteNotebook(getSnapshots().getSnapshot(position).getReference());
    }

    //archive notebook
    public void archiveNotebook(int position){
        getSnapshots().getSnapshot(position).getReference().update("archive", true);
    }

    //unarchive notebook
    public void unArchiveNotebook(int position){
        getSnapshots().getSnapshot(position).getReference().update("archive", false);
        FirestoreRepository firestoreRepository = FirestoreRepository.getInstance();
        firestoreRepository.updateNotebookTimestamp(getSnapshots().getSnapshot(position).getReference().getId());
    }

    public void putBack(int position){
        getSnapshots().getSnapshot(position).getReference().update("archive", true);
        getSnapshots().getSnapshot(position).getReference().update("archive", false);

    }

}
