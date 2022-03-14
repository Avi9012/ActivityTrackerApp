package com.example.activitytrackerapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.activitytrackerapp.UtilityClasses.Project;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class MainActivity extends AppCompatActivity {
    private Button logout, details, new_activity, running_activity, start, pause, submit, activityRecords;
    RelativeLayout relativeLayout1, relativeLayout2, relativeLayout3;
    String Name, Department, EmpId, WoNumber, ProjectStart, time;
    TextView empName, empDept, date, start_time, running_time;
    ConstraintLayout expandable1, expandable2, expandable3;
    SwipeRefreshLayout swipeRefreshLayout;
    ArrayList<String> project_names;
    SharedPreferences.Editor editor;
    ArrayAdapter<String> adapter;
    long timer, lastResumeTime;
    SharedPreferences appDataShared;
    Spinner projects_spinner;
    EditText woNumber, Desc;
    Runnable runnableCode;
    FirebaseAuth mAuth;
    boolean isRunning;
    Handler handler;
    @SuppressLint({"SetTextI18n", "CommitPrefEdits"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.blue_200));
        }
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null || !mAuth.getCurrentUser().isEmailVerified()) {
            startActivity(new Intent(MainActivity.this, LogIn.class));
            finish();
        }
        appDataShared = getApplicationContext().getSharedPreferences("AppData", MODE_PRIVATE);
        editor = appDataShared.edit();

        running_activity = (Button) findViewById(R.id.running_activity);
        new_activity = (Button) findViewById(R.id.new_activity);
        activityRecords = (Button) findViewById(R.id.records);
        woNumber = (EditText) findViewById(R.id.wo_number);
        details = (Button) findViewById(R.id.details);
        submit = (Button) findViewById(R.id.Submit);
        logout = (Button) findViewById(R.id.logout);
        start = (Button) findViewById(R.id.Start);
        pause = (Button) findViewById(R.id.Pause);
        Desc = (EditText) findViewById(R.id.desc);

        relativeLayout1 = (RelativeLayout) findViewById(R.id.relativeLayout1);
        relativeLayout2 = (RelativeLayout) findViewById(R.id.relativeLayout2);
        relativeLayout3 = (RelativeLayout) findViewById(R.id.relativeLayout3);

        expandable1 = (ConstraintLayout) findViewById(R.id.expandable1);
        expandable2 = (ConstraintLayout) findViewById(R.id.expandable2);
        expandable3 = (ConstraintLayout) findViewById(R.id.expandable3);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);

        projects_spinner = (Spinner) findViewById(R.id.project_list);

        running_time = (TextView) findViewById(R.id.running_time);
        start_time = (TextView) findViewById(R.id.start_time);
        empName = (TextView) findViewById(R.id.name);
        empDept = (TextView) findViewById(R.id.dep);
        date = (TextView) findViewById(R.id.date);

        project_names = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_view, project_names);
        adapter.setDropDownViewResource(R.layout.drop_downn_item);
        projects_spinner.setAdapter(adapter);

        Department = getIntent().getStringExtra("dept");
        EmpId = getIntent().getStringExtra("empId");
        Name = getIntent().getStringExtra("name");

        ProjectStart = "Not Started";
        isRunning = false;
        timer = 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadData();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
                LoadData();
            }
        });

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        handler = new Handler();
        runnableCode = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                runTimer();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnableCode);
    }

    @Override
    protected void onPause() {
        super.onPause();
        project_names.clear();
        saveData();
        handler.removeCallbacks(runnableCode);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    private void LoadData() {
        if(appDataShared.getString("startButton", "Visible").equals("Gone")) {
            start.setVisibility(View.GONE);
        }
        else {
            start.setVisibility(View.VISIBLE);
        }
        if(appDataShared.getString("pauseButton", "Gone").equals("Gone")) {
            pause.setVisibility(View.GONE);
        }
        else {
            pause.setVisibility(View.VISIBLE);
        }
        if(appDataShared.getString("submitButton", "Gone").equals("Gone")) {
            submit.setVisibility(View.GONE);
        }
        else {
            submit.setVisibility(View.VISIBLE);
        }
        pause.setText(appDataShared.getString("pauseButtonText", "Pause").trim());
        ProjectStart = appDataShared.getString("projectStart", ProjectStart).trim();
        isRunning = appDataShared.getBoolean("isRunning", isRunning);
        Name = appDataShared.getString("name", Name).trim();
        Department = appDataShared.getString("dept", Department).trim();
        EmpId = appDataShared.getString("empId", EmpId).trim();
        timer = appDataShared.getLong("timer", timer);
        lastResumeTime = appDataShared.getLong("lastResumeTime", System.currentTimeMillis());
        if(isRunning) {
            timer += (System.currentTimeMillis()-lastResumeTime)/1000;
        }
        empName.setText("Name:   "+Name.trim());
        empDept.setText("Department:   "+Department.trim());
        FirebaseDatabase.getInstance().getReference().child("users").child(requireNonNull(mAuth.getCurrentUser()).getUid()).child("date").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                date.setText("Date:   "+snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        setStartTime();
        FirebaseDatabase.getInstance().getReference().child("projects").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot project: snapshot.getChildren()) {
                    project_names.add(requireNonNull(project.getValue()).toString().trim());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if(woNumber.getText().toString().isEmpty()) {
                    woNumber.setError("This is required.");
                }
                else {
                    if(Desc.getText().toString().isEmpty()) {
                        Desc.setError("This is required.");
                    }
                    else {
                        registerProject();
                    }
                }
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pause.getText().toString().equals("Pause")) {
                    pause.setText("Resume");
                    FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("woNumber").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            WoNumber = requireNonNull(snapshot.getValue()).toString().trim();
                            FirebaseDatabase.getInstance().getReference().child("activities").child(WoNumber).child("lastPauseTime").setValue(System.currentTimeMillis());
                            isRunning = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else {
                    ResumeActivity("Resume");
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pause.getText().toString().equals("Resume")) {
                    ResumeActivity("Submit");
                }
                FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("woNumber").addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        WoNumber = requireNonNull(snapshot.getValue()).toString().trim();
                        long millis=System.currentTimeMillis();
                        @SuppressLint("SimpleDateFormat") DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy HH:mm");
                        Date now = new Date(millis);
                        FirebaseDatabase.getInstance().getReference().child("activities").child(WoNumber).child("submitTime").setValue(DATE_FORMAT.format(now));
                        FirebaseDatabase.getInstance().getReference().child("activities").child(WoNumber).child("runningTime").setValue(time);
                        start.setVisibility(View.VISIBLE);
                        pause.setText("Pause");
                        pause.setVisibility(View.GONE);
                        submit.setVisibility(View.GONE);
                        isRunning = false;
                        ProjectStart = "Not Started";
                        timer = 0;
                        setStartTime();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        expandable1.setVisibility(View.GONE);
        expandable2.setVisibility(View.GONE);
        expandable3.setVisibility(View.GONE);
        details.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if(expandable1.getVisibility() == View.GONE) {
                    TransitionManager.beginDelayedTransition(relativeLayout1, new AutoTransition());
                    expandable1.setVisibility(View.VISIBLE);
                }
                else {
                    expandable1.setVisibility(View.GONE);
                }
            }
        });

        new_activity.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if(expandable2.getVisibility() == View.GONE) {
                    TransitionManager.beginDelayedTransition(relativeLayout2, new AutoTransition());
                    expandable2.setVisibility(View.VISIBLE);
                }
                else {
                    expandable2.setVisibility(View.GONE);
                }
            }
        });

        running_activity.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if(expandable3.getVisibility() == View.GONE) {
                    TransitionManager.beginDelayedTransition(relativeLayout3, new AutoTransition());
                    expandable3.setVisibility(View.VISIBLE);
                }
                else {
                    expandable3.setVisibility(View.GONE);
                }
            }
        });

        activityRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Records.class);
                intent.putExtra("empId", EmpId);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
                FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("loggedIn").setValue(false);
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, LogIn.class));
                finish();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void ResumeActivity(String from) {
        pause.setText("Pause");
        FirebaseDatabase.getInstance().getReference().child("users").child(requireNonNull(mAuth.getCurrentUser()).getUid()).child("woNumber").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                WoNumber = requireNonNull(snapshot.getValue()).toString().trim();
                FirebaseDatabase.getInstance().getReference().child("activities").child(WoNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long lastPaused = (long) requireNonNull(snapshot).child("lastPauseTime").getValue();
                        long milliseconds = System.currentTimeMillis()-lastPaused;
                        long pauseTime = (long) snapshot.child("pauseTime").getValue()+milliseconds;
                        FirebaseDatabase.getInstance().getReference().child("activities").child(WoNumber).child("pauseTime").setValue(pauseTime);
                        if(from.equals("Resume")) {
                            isRunning = true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveData() {
        if(start.getVisibility() == View.GONE) {
            editor.putString("startButton", "Gone");
        }
        else {
            editor.putString("startButton", "Visible");
        }
        if(submit.getVisibility() == View.GONE) {
            editor.putString("submitButton", "Gone");
        }
        else {
            editor.putString("submitButton", "Visible");
        }
        if(pause.getVisibility() == View.GONE) {
            editor.putString("pauseButton", "Gone");
        }
        else {
            editor.putString("pauseButton", "Visible");
        }
        editor.putString("pauseButtonText", pause.getText().toString());
        editor.putBoolean("isRunning", isRunning);
        editor.putString("empId", EmpId);
        editor.putString("name", Name);
        editor.putString("dept", Department);
        editor.putString("projectStart", ProjectStart);
        editor.putLong("timer", timer);
        editor.putLong("lastResumeTime", System.currentTimeMillis());
        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void registerProject() {
        long millis;
        String now;
        try {
            millis=System.currentTimeMillis();
            @SuppressLint("SimpleDateFormat") DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy HH:mm");
            now = DATE_FORMAT.format(new Date(millis));
        } catch (Exception e) {
            now = Arrays.toString(e.getStackTrace());
        }
        Project activity = new Project(projects_spinner.getSelectedItem().toString(), woNumber.getText().toString(), EmpId, FirebaseAuth.getInstance().getCurrentUser().getUid(), now, 0, "notSubmitted", 0, Desc.getText().toString(),  "");
        FirebaseDatabase.getInstance().getReference().child("activities").child(woNumber.getText().toString()).setValue(activity);
        FirebaseDatabase.getInstance().getReference().child("users").child(requireNonNull(mAuth.getCurrentUser()).getUid()).child("woNumber").setValue(woNumber.getText().toString());
        ProjectStart = now;
        start.setVisibility(View.GONE);
        pause.setVisibility(View.VISIBLE);
        submit.setVisibility(View.VISIBLE);
        woNumber.setText("");
        Desc.setText("");
        isRunning = true;
        timer = 0;
        setStartTime();
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void runTimer() {
        if(isRunning) {
            timer++;
        }
        long hours = timer/3600;
        long minutes = (timer - hours*3600)/60;
        long seconds = (timer - hours*3600 - minutes*60);
        time = String.format(Locale.getDefault(),"%02d:%02d:%02d", hours, minutes, seconds);
        running_time.setText("Running Activity:  "+time);
    }

    @SuppressLint("SetTextI18n")
    private void setStartTime() {
        start_time.setText("Start:  "+ProjectStart);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}