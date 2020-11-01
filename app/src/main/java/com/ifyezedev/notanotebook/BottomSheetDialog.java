package com.ifyezedev.notanotebook;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BottomSheetDialog extends BottomSheetDialogFragment {
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

    //set bottom sheet theme
    @Override
    public int getTheme() {
        return R.style.BottomSheetTheme;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_menu_bottom_sheet, container, false);

        TextView username = view.findViewById(R.id.username);
        TextView email = view.findViewById(R.id.email);
        TextView tutorial = view.findViewById(R.id.tutorial);
        TextView change_pin = view.findViewById(R.id.change_pin);
        TextView log_out = view.findViewById(R.id.log_out);

        //set username and email
        String greeting = "Hi, " + firebaseUser.getDisplayName();
        username.setText(greeting);
        email.setText(firebaseUser.getEmail());

        //display tutorial on click of tutorial textview
        tutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //direct user to change pin on click of change pin textview
        change_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangePinCustomDialog changePinCustomDialog = new ChangePinCustomDialog();
                changePinCustomDialog.show(getFragmentManager(), "Change Pin");
                dismiss();
            }
        });

        //log user out on click of log out textview
        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthUI.getInstance()
                        .signOut(getContext())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startActivity(new Intent(getContext(), SignInActivity.class));
                                dismiss();
                            }
                        });
            }
        });

        return view;
    }
}
