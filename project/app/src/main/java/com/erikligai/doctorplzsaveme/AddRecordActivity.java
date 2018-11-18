package com.erikligai.doctorplzsaveme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

public class AddRecordActivity extends AppCompatActivity {

    private Button backBtn1,nextBtn1;
    private EditText titleText,commentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        // Get buttons
        backBtn1 = findViewById(R.id.backButton1);
        nextBtn1 = findViewById(R.id.editNextButton3);
        // Set title & comment editText
        commentText = findViewById(R.id.editRecordComment);
        titleText = findViewById(R.id.editRecordTitle);

        backBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRecordListActivity();

            }
        });

        nextBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleText.getText().toString();
                String comment = commentText.getText().toString();
                Date date = new Date();
                openAddBodyLocationActivity();
            }
        });
    }

    private void openAddBodyLocationActivity(){
        Intent intent = new Intent(this,AddBodylocationActivity.class);
        startActivity(intent);
    }

    private void openRecordListActivity(){
        Intent intent = new Intent(this,RecordListActivity.class);
        startActivity(intent);
    }

}
