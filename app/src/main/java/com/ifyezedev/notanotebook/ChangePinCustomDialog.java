package com.ifyezedev.notanotebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beautycoder.pflockscreen.security.PFResult;
import com.beautycoder.pflockscreen.security.PFSecurityManager;
import com.beautycoder.pflockscreen.security.callbacks.PFPinCodeHelperCallback;
import com.google.android.material.textfield.TextInputEditText;

public class ChangePinCustomDialog extends AppCompatDialogFragment {
    TextInputEditText answer_edit_text;
    TextView question_text_view;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.change_pin_dialog, null);

        builder.setView(view)
                .setTitle("Answer security question")
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
                        //we get the correct answer and the user answer
                        String correctAnswer = PreferencesSettings.getAnswer(getContext()).trim();
                        String userAnswer = answer_edit_text.getText().toString().trim();

                        //compare both answers
                        if(correctAnswer.equalsIgnoreCase(userAnswer)){
                            //if the answer is correct take the user to reset their pin
                            PFSecurityManager.getInstance().getPinCodeHelper().delete(new PFPinCodeHelperCallback<Boolean>() {
                                @Override
                                public void onResult(PFResult<Boolean> result) {
                                    Log.i("DELETED", "PIN DELETED");
                                    Intent intent = new Intent(getContext(), PinLockScreenActivity.class);
                                    intent.putExtra(NotebookActivity.EXTRA_RESET_PIN, true);
                                    startActivity(intent);
                                }
                            });

                            dialogInterface.dismiss();
                        }
                        else {
                            //if the answer is wrong, tell the user
                            Toast.makeText(getContext(), "Incorrect answer, please try again", Toast.LENGTH_SHORT).show();

                            //then make dialog reappear
                            ChangePinCustomDialog changePinCustomDialog = new ChangePinCustomDialog();
                            changePinCustomDialog.show(getFragmentManager(), "Change Pin");
                        }
                    }
                });

        answer_edit_text = view.findViewById(R.id.dialog_answer_edit);
        question_text_view = view.findViewById(R.id.dialog_question_textview);
        question_text_view.setText(PreferencesSettings.getQuestion(getContext()).toString());

        return builder.create();
    }
}