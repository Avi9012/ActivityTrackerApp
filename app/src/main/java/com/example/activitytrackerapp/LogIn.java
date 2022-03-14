package com.example.activitytrackerapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LogIn extends AppCompatActivity {
    private String Email, Pass, EmpId, Name, Dept;
    private ProgressDialog progressDialog;
    private EditText email, pass;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        Objects.requireNonNull(getSupportActionBar()).hide();
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.blue_200));
        }
        mAuth = FirebaseAuth.getInstance();

        TextView forgotPassword = (TextView) findViewById(R.id.forgot_pass);
        TextView singUp = (TextView) findViewById(R.id.sign_up);
        email = (EditText) findViewById(R.id.login_email);
        pass = (EditText) findViewById(R.id.login_pass);
        Button btn = (Button) findViewById(R.id.log_in);

        progressDialog = new ProgressDialog(LogIn.this);

        progressDialog.setMessage("Logging you in..");


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Email = email.getText().toString().trim();
                Pass = pass.getText().toString().trim();
                if(Email.isEmpty()) {
                    email.setError("This is Required.");
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
                    email.setError("Enter a valid email address.");
                    return;
                }
                if(Pass.isEmpty()) {
                    pass.setError("This is Required.");
                }
                else {
                    progressDialog.show();
                    logIn(Email, Pass);
                }
            }
        });

        singUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, SignUp.class);
                startActivity(intent);
                finish();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Email = email.getText().toString().trim();
                if(Email.isEmpty()) {
                    email.setError("This is Required.");
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
                    email.setError("Enter a valid email address.");
                }
                else {
                    mAuth.sendPasswordResetEmail(Email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                AlertDialog.Builder box = new AlertDialog.Builder(LogIn.this);
                                box.setMessage("Password Reset Email has been sent successfully.");
                                AlertDialog alert = box.create();
                                alert.show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(LogIn.this, SignUp.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void logIn(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    if(Objects.requireNonNull(mAuth.getCurrentUser()).isEmailVerified()) {
                        goToMainActivity();
                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(LogIn.this, "Your email is not verified yet.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(LogIn.this, "Email address or Password is wrong.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void goToMainActivity() {
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).child("loggedIn").setValue(true);
                Name = Objects.requireNonNull(snapshot.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).child("name").getValue()).toString().trim();
                Dept = Objects.requireNonNull(snapshot.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).child("department").getValue()).toString().trim();
                EmpId = Objects.requireNonNull(snapshot.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).child("empId").getValue()).toString().trim();
                Bundle data = new Bundle();
                data.putString("empId", EmpId);
                data.putString("name", Name);
                data.putString("dept", Dept);
                Intent intent = new Intent(LogIn.this, MainActivity.class);
                intent.putExtras(data);
                progressDialog.dismiss();
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}