package com.erikligai.doctorplzsaveme.Activities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.erikligai.doctorplzsaveme.Activities.AddRecordActivity;
import com.erikligai.doctorplzsaveme.Adapters.RecordAdapter;
import com.erikligai.doctorplzsaveme.Models.Record;
import com.erikligai.doctorplzsaveme.R;

import java.util.ArrayList;

public class MainRecordActivity extends AppCompatActivity {
    private RecordAdapter adapter;
    private ArrayList<Record> records = new ArrayList<>();

    //sample record list
    Record r1 = new Record("Record1","recordDescription");
    Record r2 = new Record("Record1","recordDescription");
    Record r3 = new Record("Record1","recordDescription");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_record);

        records.add(r1);
        records.add(r2);
        records.add(r3);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.record_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.records);

        FloatingActionButton fab = findViewById(R.id.record_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("fab", "add record");
                //calls AddRecordActivity
                Intent intent = new Intent(view.getContext(), AddRecordActivity.class);
                startActivity(intent);
            }
        });

        RecyclerView recordRecycler = findViewById(R.id.record_recyclerview);
        recordRecycler.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recordRecycler.setLayoutManager(layoutManager);


        adapter = new RecordAdapter(records);
        recordRecycler.setAdapter(adapter);
        adapter.setOnEntryClickListener(new RecordAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(int position) {
//                Intent intent = new Intent(getApplicationContext(), EditProblemActivity.class);
//                intent.putExtra("Pos", position);
//                startActivity(intent);
                Log.d("rview", Integer.toString(position));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_search:
                // User chose the "Settings" item, show the app settings UI...
                Log.d("toolbar", "search function!");
                //calls ProblemSearchActivity
                Intent intent = new Intent(this, RecordSearchActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
