package com.erikligai.doctorplzsaveme.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.erikligai.doctorplzsaveme.R;
import com.erikligai.doctorplzsaveme.SearchTabFragments.AddBodyLocationFragment;

import java.util.Date;

public class EditRecordActivity extends AppCompatActivity {

    private Button saveChangesBtn,nextEditBtn;
    private EditText EditTitleText,EditCommentText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_record);

        // Get buttons
        saveChangesBtn = findViewById(R.id.saveChangesButton);
        nextEditBtn = findViewById(R.id.editNextButton3);
        // Set comment editText
        EditCommentText = findViewById(R.id.editRecordComment);
        EditTitleText = findViewById(R.id.editRecordTitle);

        nextEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = EditTitleText.getText().toString();
                String comment = EditCommentText.getText().toString();
                Date date = new Date();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                AddBodyLocationFragment fragment = new AddBodyLocationFragment();
                fragmentTransaction.replace(R.id.editRecordActivity, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

//        saveChangesBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String title = EditTitleText.getText().toString();
//                String comment = EditCommentText.getText().toString();
//                Date date = new Date();
//            }
//        });
    }

    private void openEditBodyLocationActivity(){
        Intent intent = new Intent(this,AddBodylocationActivity.class);
        startActivity(intent);
    }
}
