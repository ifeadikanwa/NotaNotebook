package com.ifyezedev.notanotebook;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class Checklist_Item_Dialog extends AppCompatDialogFragment {

    TextDialogInterface customDialogInterface;
    EditText titleEditText;
    String item_id;
    String oldText;


    //constructor for updating checklist item title, item_id and old title will be passed in and displayed in edit text
    public Checklist_Item_Dialog(String item_id, String oldText){
        this.item_id = item_id;
        this.oldText = oldText;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.edit_title_dialog, null);

        builder.setView(view)
                .setTitle("Enter Checklist Entry:")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(dialogInterface != null){
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //here we are using our custom dialog interface to send data to the activity that calls it
                        String newText = titleEditText.getText().toString();
                        if(!newText.isEmpty()){
                            customDialogInterface.updateChecklistEntry(item_id, newText);
                        }
                        else {
                            Toast.makeText(getActivity(), "text can't be empty", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

        titleEditText = view.findViewById(R.id.titleEditText);
        if(!(oldText == null)){
            titleEditText.setText(oldText);
        }

        return builder.create();
    }


    //our custom interface for sending data to the activity that implements it
    public interface TextDialogInterface {

        void updateChecklistEntry(String item_id, String Text);

    }

    //initialize the custom interface variable in onAttach
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        customDialogInterface = (TextDialogInterface) context;
    }
}
