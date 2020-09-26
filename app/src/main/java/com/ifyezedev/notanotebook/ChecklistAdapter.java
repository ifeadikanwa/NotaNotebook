package com.ifyezedev.notanotebook;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        holder.item_text.setText(model.getItem());

        if(model.isChecked()){
            holder.item_checkbox.setChecked(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.item_checkbox.setButtonTintList(ColorStateList.valueOf(Color.GRAY));
            }

            holder.item_text.setTextColor(Color.GRAY);
        }
        else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.item_checkbox.setButtonTintList(ColorStateList.valueOf(Color.BLACK));
            }
            holder.item_text.setTextColor(Color.BLACK);
            holder.item_checkbox.setChecked(false);

        }

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


            item_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            item_checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //todo: on click check or uncheck box
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        boolean isChecked = item_checkbox.isChecked();
                        listener.onCheckboxClick(getSnapshots().getSnapshot(position), position, isChecked);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);

        void onCheckboxClick(DocumentSnapshot documentSnapshot, int position, boolean checked);
    }

    public void setOnItemClickListener(ChecklistAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    //delete item entry
    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }
}
