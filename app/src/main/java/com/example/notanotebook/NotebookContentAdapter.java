package com.example.notanotebook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class NotebookContentAdapter extends FirestoreRecyclerAdapter<NotebookContent, NotebookContentAdapter.NotebookContentHolder> {

    private NotebookAdapter.OnItemClickListener listener;

    public NotebookContentAdapter(@NonNull FirestoreRecyclerOptions<NotebookContent> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull NotebookContentHolder holder, int position, @NonNull NotebookContent model) {
        holder.notebookContentTitle.setText(model.getTitle());

        holder.imageCard.setCardBackgroundColor(model.getColor());

        if(model.isNote()){
            holder.contentTypeImage.setImageResource(R.drawable.ic_notes);
        }
        else{
            holder.contentTypeImage.setImageResource(R.drawable.ic_baseline_check);
        }
    }

    @NonNull
    @Override
    public NotebookContentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notebook_content_item_view, parent, false);
        return new NotebookContentAdapter.NotebookContentHolder(v);
    }

    class NotebookContentHolder extends RecyclerView.ViewHolder {
        TextView notebookContentTitle;
        ImageView contentTypeImage;
        CardView imageCard;

        public NotebookContentHolder(@NonNull View itemView) {
            super(itemView);
            notebookContentTitle = itemView.findViewById(R.id.notebookContentTitle);
            contentTypeImage = itemView.findViewById(R.id.contentTypeImg);
            imageCard = itemView.findViewById(R.id.imageCardView);

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

    public void setOnItemClickListener(NotebookAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }


}
