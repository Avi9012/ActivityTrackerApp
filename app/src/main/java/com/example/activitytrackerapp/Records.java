package com.example.activitytrackerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.example.activitytrackerapp.UtilityClasses.Project;
import com.example.activitytrackerapp.UtilityClasses.RecyclerViewAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Records extends AppCompatActivity {

    ProgressDialog progressDialog;

    SwipeRefreshLayout swipeRefreshLayout;

    List<Project> list;

    RecyclerView recyclerView;

    RecyclerView.Adapter adapter;

    String EmpId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        Objects.requireNonNull(getSupportActionBar()).hide();
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.blue_200));
        }
        recyclerView = (RecyclerView) findViewById(R.id.activity_list);
        recyclerView.setHasFixedSize(true);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_activity);

        recyclerView.setLayoutManager(new LinearLayoutManager(Records.this));

        progressDialog = new ProgressDialog(Records.this);

        progressDialog.setMessage("Loading Data from Database");

        progressDialog.show();
        EmpId = getIntent().getStringExtra("empId");
        list = new ArrayList<Project>();
        loadData();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        // Stop animation (This will be after 3 seconds)
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 4000); // Delay in millis
            }
        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void loadData() {
        list.clear();
        FirebaseDatabase.getInstance().getReference().child("activities").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data: snapshot.getChildren()){
                    long lastPauseTime = (long) data.child("lastPauseTime").getValue();
                    long pauseTime = (long) data.child("pauseTime").getValue();
                    String startTime = (String) data.child("startTime").getValue().toString();
                    String submitTime = (String) data.child("submitTime").getValue();
                    String projectName = (String) data.child("projectName").getValue();
                    String woNumber = (String) data.child("woNumber").getValue();
                    String desc = (String) data.child("description").getValue();
                    String Id = (String) data.child("empId").getValue();
                    String userId = (String) data.child("userId").getValue();
                    String runningTime = (String) data.child("runningTime").getValue();
                    Project project = new Project(projectName, woNumber, Id, userId, startTime, pauseTime, submitTime, lastPauseTime, desc, runningTime);
                    if(project.getEmpId().equals(EmpId)) {
                        list.add(project);
                    }
                }
                adapter = new RecyclerViewAdapter(Records.this, list);
                recyclerView.setAdapter(adapter);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}