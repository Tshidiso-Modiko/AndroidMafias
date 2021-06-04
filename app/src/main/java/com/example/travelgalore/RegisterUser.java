package com.example.travelgalore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener {

    //(CodeWithMazn, 2020)

    private TextView banner;
    private EditText fullName, Email, password;
    private Button register;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        banner = (TextView) findViewById(R.id.txtRegHead);
        banner.setOnClickListener(this);

        register = (Button) findViewById(R.id.btnRegister);
        register.setOnClickListener(this);

        fullName = (EditText) findViewById(R.id.edtFullName);
        Email = (EditText) findViewById(R.id.edtRegEmail);
        password = (EditText) findViewById(R.id.edtRegPassword);

        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.txtRegHead:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.btnRegister:
                registerUser();
                break;
        }
    }

    private void registerUser(){
        String name = fullName.getText().toString().trim();
        String email = Email.getText().toString().trim();
        String pswd= password.getText().toString().trim();

        if(name.isEmpty()){
            fullName.setError("Full name is required!");
            fullName.requestFocus();
            return;
        }

        if(email.isEmpty()){
            Email.setError("Email is required!");
            Email.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Email.setError("Please provide valid email!");
            Email.requestFocus();
            return;
        }

        if(pswd.isEmpty()){
            password.setError("Password is required!");
            password.requestFocus();
            return;
        }

        if(pswd.length()<6){
            password.setError("Minimum password length should be 6 characters!");
            password.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email,pswd)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            User user = new User(name, email);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterUser.this, "User registered!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);

                                        //redirect to layout
                                    } else {
                                        Toast.makeText(RegisterUser.this, "Failed to register user! Try again!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(RegisterUser.this, "Failed to register user!", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}
/*CodeWithMazn, 2020. #1 Login and Registration Android App Tutorial Using Firebase Authentication - Create User.
[online] Youtube.com. Available at: <https://www.youtube.com/watch?v=Z-RE1QuUWPg> [Accessed 2 June 2021].*/