package com.example.notanotebook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class ChecklistAdapter extends FirestoreRecyclerAdapter<Checklist_Item, ChecklistAdapter.ChecklistHolder> {
    private ChecklistAdapter.OnItemClickListener listener;

    public ChecklistAdapter(@NonNull FirestoreRecyclerOptions<Checklist_Item> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ChecklistHolder holder, int position, @NonNull Checklist_Item model) {
        if(model.isChecked()){
            holder.item_checkbox.isChecked();
        }

        holder.item_text.setText(model.getItem());
    }


    @NonNull
    @Override
    public ChecklistAdapter.ChecklistHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.checklist_item_view, parent, false);
        return new ChecklistAdapter.ChecklistHolder(v);
    }


    class ChecklistHolder extends RecyclerView.ViewHolder {
        TextView item_text;
        CheckBox item_checkbox;

        public ChecklistHolder(@NonNull View itemView) {
            super(itemView);
            item_text = itemView.findViewById(R.id.item_textview);
            item_checkbox = itemView.findViewById(R.id.checkbox_checklist);

            //we are going to set onclicklistener on the cardview
            // and then send it to the activity displaying the card to decide what happens on click.
            //to do this we create an interface
            item_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

//            item_checkbox.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    //todo: on click check or uncheck box
//                }
//            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(ChecklistAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
