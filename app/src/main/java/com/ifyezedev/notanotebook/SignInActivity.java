package com.ifyezedev.notanotebook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1234;
    private static final String TAG = SignInActivity.class.getSimpleName();
    private FirestoreRepository firestoreRepository;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        firestoreRepository = FirestoreRepository.getInstance();

        initializeSignIn();

    }

    private void initializeSignIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // already signed in
            //check email verification status and act accordingly

            if (user.isEmailVerified()) {
                //todo: go to notebookActivity
                startActivity(new Intent(SignInActivity.this, NotebookActivity.class));
            } else {
                //todo: go to verification activity
                startActivity(new Intent(SignInActivity.this, EmailVerificationActivity.class));
            }
            finish();

        } else {
            // not signed in
            startActivityForResult(
                    // Get an instance of AuthUI based on the default app
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setLogo(R.drawable.notabook_icon)
                            .setTheme(R.style.LoginTheme)
                            .setIsSmartLockEnabled(true)
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build()))
                            .build(),
                    RC_SIGN_IN);
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                // sign in successful
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                //check if its a new user and them to the database
                FirebaseUserMetadata metadata = user.getMetadata();
                if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                    firestoreRepository.addUser(user.getUid());
                }

                // Todo: if account isn't verified, take them to verification activity
                if (user != null) {
                    if (user.isEmailVerified()) {
                        //todo: go to notebookActivity
                        startActivity(new Intent(SignInActivity.this, NotebookActivity.class));
                    } else {
                        //todo: go to verification activity
                        startActivity(new Intent(SignInActivity.this, EmailVerificationActivity.class));
                    }
                    finish();
                }

            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(this, "Sign in error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }
}