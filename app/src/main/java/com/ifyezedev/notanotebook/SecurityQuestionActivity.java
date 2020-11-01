package com.ifyezedev.notanotebook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.rpc.context.AttributeContext;

public class SecurityQuestionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    public static final String TAG = SecurityQuestionActivity.class.getSimpleName();
    FirestoreRepository firestoreRepository;
    Spinner spinner;
    TextInputEditText answer_edit;
    Button done;
    String[] security_questions;

//    String[] security_questions = {
//            "What was your childhood nickname?",
//            "What is the name of your favorite childhood friend?",
//            "What was the name of the street you lived in as a child?",
//            "In what town or city did your parents meet?",
//            "What primary school did you attend?",
//            "In what town or city was your first full time job?",
//            "In what town or city did you meet your partner?",
//            "What is your mother's maiden name?"
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_question);
        getSupportActionBar().hide();

        security_questions = getResources().getStringArray(R.array.security_questions);

        firestoreRepository = FirestoreRepository.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        spinner = findViewById(R.id.question_spinner);
        answer_edit = findViewById(R.id.security_answer);
        done = findViewById(R.id.security_done_bttn);

        spinner.setOnItemSelectedListener(this);

        ArrayAdapter adapter;
        adapter = ArrayAdapter.createFromResource(this, R.array.security_questions, R.layout.custom_spinner_textview);
        adapter.setDropDownViewResource(R.layout.custom_spinner_textview);
        spinner.setAdapter(adapter);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(spinner.getOnItemSelectedListener() != null && answer_edit.getText().toString() != null && !answer_edit.getText().toString().isEmpty()){
                    //if the question and answer have been given, retrieve them
                    String question = spinner.getSelectedItem().toString();
                    String answer = answer_edit.getText().toString();
                    Log.i(TAG, question + " " + answer);

                    //save the question and answer to shared preferences
                    PreferencesSettings.saveQuestionToPref(SecurityQuestionActivity.this, question);
                    PreferencesSettings.saveAnswerToPref(SecurityQuestionActivity.this, answer);

                    //now security is set so update the security field for the current user
                    firestoreRepository.updateSecurityField(user.getUid(), true);

                    //take user to notebook activity
                    startActivity(new Intent(SecurityQuestionActivity.this, NotebookActivity.class));

                    finish();
                }
                else{
                    Toast.makeText(SecurityQuestionActivity.this, "Select a security question and provide an answer", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}