package com.erikligai.doctorplzsaveme.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.erikligai.doctorplzsaveme.R;

public class EditGeolocationActivity extends AppCompatActivity {

    // Get Buttons
    private Button saveChangesBtn3,EditNextBtn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bodylocation);

        // Get buttons
        saveChangesBtn3 = findViewById(R.id.saveChangesButton3);
        EditNextBtn3 = findViewById(R.id.editNextButton3);

        saveChangesBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}