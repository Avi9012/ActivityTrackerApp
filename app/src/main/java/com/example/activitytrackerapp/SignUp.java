package com.example.activitytrackerapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.activitytrackerapp.UtilityClasses.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class SignUp extends AppCompatActivity {
    private EditText name, empId, email, phone, pass, cnfPass, department;
    private String Name, EmpId, Phone, CnfPass, Email, Pass, Dept;
    SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private FirebaseUser fUser;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Objects.requireNonNull(getSupportActionBar()).hide();

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.blue_200));
        }
        mAuth = FirebaseAuth.getInstance();

        sharedPreferences = getApplicationContext().getSharedPreferences("employeeInfo", Context.MODE_PRIVATE);
        Name = sharedPreferences.getString("name", "UserName").trim();
        Dept = sharedPreferences.getString("dept", "Department").trim();
        Email = sharedPreferences.getString("email", "example@gmail.com").trim();

        progressDialog = new ProgressDialog(SignUp.this);
        progressDialog.setMessage("Sending Verification Email");

        TextView login = (TextView) findViewById(R.id.log_in);
        name = (EditText) findViewById(R.id.name);
        empId = (EditText) findViewById(R.id.empId);
        email = (EditText) findViewById(R.id.email);
        phone = (EditText) findViewById(R.id.phone);
        pass = (EditText) findViewById(R.id.pass);
        department = (EditText) findViewById(R.id.dept);
        cnfPass = (EditText) findViewById(R.id.cnfPass);
        Button btn = (Button) findViewById(R.id.registerBtn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Name = name.getText().toString().trim();
                EmpId = empId.getText().toString().trim();
                Phone = phone.getText().toString().trim();
                Email = email.getText().toString().trim();
                Pass = pass.getText().toString().trim();
                Dept = department.getText().toString().trim();
                CnfPass = cnfPass.getText().toString().trim();
                if(Name.isEmpty()) {
                    name.setError("This is Required.");
                }
                if(EmpId.isEmpty()) {
                    empId.setError("This is Required.");
                }
                if(Phone.isEmpty()) {
                    phone.setError("This is Required.");
                }
                if(Pass.isEmpty()) {
                    pass.setError("This is Required.");
                }
                if(Dept.isEmpty()) {
                    department.setError("This is Required.");
                }
                if(CnfPass.isEmpty()) {
                    cnfPass.setError("This is Required.");
                }
                if(!Pass.equals(CnfPass)) {
                    AlertDialog.Builder box = new AlertDialog.Builder(SignUp.this);
                    box.setMessage("Password and Confirm Password must match.");
                    AlertDialog alert = box.create();
                    alert.show();
                    return;
                }
                if(Email.isEmpty()) {
                    email.setError("This is Required.");
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
                    email.setError("Enter a valid email address.");
                }
                else {
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);
                    user = new User(Name, Email, EmpId, Phone, Dept, false);
                    progressDialog.show();
                    registerUser(Email, Pass);
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, LogIn.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerUser(String email, String pass) {
        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    fUser = mAuth.getCurrentUser();
                    assert fUser != null;
                    fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            AlertDialog.Builder box = new AlertDialog.Builder(SignUp.this);
                            box.setMessage("Email sent, Check your Email!");
                            AlertDialog alert = box.create();
                            alert.show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.d("tag", "Error: Email not verified."+e.getMessage());
                        }
                    });
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(SignUp.this, "Couldn't send email, Some Error Occurred!", Toast.LENGTH_SHORT).show();
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
            createUserAndLogin();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            saveSharedPreferences();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void createUserAndLogin() {
        Name = sharedPreferences.getString("name", "UserName").trim();
        Dept = sharedPreferences.getString("dept", "Department").trim();
        Email = sharedPreferences.getString("email", "example@gmail.com").trim();
        EmpId = sharedPreferences.getString("empId", "Employee ID").trim();
        Phone = sharedPreferences.getString("phone", "1234567890").trim();
        Pass = sharedPreferences.getString("pass", "password123").trim();
        user = new User(Name, Email, EmpId, Phone, Dept, true);
        if(Email != null && Pass != null && !Email.isEmpty() && !Pass.isEmpty()) {
            progressDialog = new ProgressDialog(SignUp.this);
            progressDialog.setMessage("Creating your account...");
            progressDialog.show();
            FirebaseAuth.getInstance().signInWithEmailAndPassword(Email, Pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).isEmailVerified()) {
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
                        String today = sdf.format(new Date());
                        myRef = FirebaseDatabase.getInstance().getReference();
                        myRef.child("users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).setValue(user);
                        myRef.child("users").child(mAuth.getCurrentUser().getUid()).child("date").setValue(today);
                        myRef.child("Employees").child(EmpId).child(mAuth.getCurrentUser().getUid()).setValue(true);
                        Bundle data = new Bundle();
                        data.putString("name", Name);
                        data.putString("dept", Dept);
                        data.putString("empId", EmpId);
                        Intent intent = new Intent(SignUp.this, MainActivity.class);
                        intent.putExtras(data);
                        progressDialog.dismiss();
                        startActivity(intent);
                        finish();
                    }
                    else {
                        progressDialog.dismiss();
                        AlertDialog.Builder box = new AlertDialog.Builder(SignUp.this);
                        box.setMessage("Email verification is pending...");
                        AlertDialog alert = box.create();
                        alert.show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void saveSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", Name);
        editor.putString("email", Email);
        editor.putString("empId", EmpId);
        editor.putString("phone", Phone);
        editor.putString("pass", Pass);
        editor.putString("dept", Dept);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}