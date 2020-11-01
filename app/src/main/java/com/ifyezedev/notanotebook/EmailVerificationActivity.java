package com.ifyezedev.notanotebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {
    private static final String TAG = EmailVerificationActivity.class.getSimpleName();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    Button done;
    Button resend;
    TextView different_email;
    TextView user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        sendEmailVerification();

        done = findViewById(R.id.verify_done_button);
        resend = findViewById(R.id.resend_verify_button);
        different_email = findViewById(R.id.different_email_button);
        user_email = findViewById(R.id.user_email_text_view);

        user_email.setText(user.getEmail());

        done.setOnClickListener(doneListener);
        resend.setOnClickListener(resendListener);
        different_email.setOnClickListener(diff_email_Listener);
    }

    private void sendEmailVerification() {
        //send verification email
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EmailVerificationActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private View.OnClickListener diff_email_Listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //delete the current account
            AuthUI.getInstance()
                    .signOut(EmailVerificationActivity.this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Sign out succeeded
                                //return to sign in activity
                                startActivity(new Intent(EmailVerificationActivity.this, SignInActivity.class));
                            } else {
                                // Deletion failed
                                Toast.makeText(EmailVerificationActivity.this, "Process failed, try again later", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    };

    private View.OnClickListener resendListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //resend verification email
            sendEmailVerification();
        }
    };

    private View.OnClickListener doneListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (user.isEmailVerified()) {
                        //go to notebook activity
                        Toast.makeText(EmailVerificationActivity.this, "Email verified", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EmailVerificationActivity.this, SecurityQuestionActivity.class));
                        finish();
                    } else {
                        //email has not been verified, notify user and stay here
                        Toast.makeText(EmailVerificationActivity.this, "Email not verified", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    };
}