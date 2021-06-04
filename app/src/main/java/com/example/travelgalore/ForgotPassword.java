package com.example.travelgalore;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    //(CodeWithMazn, 2020)
    private EditText editEmail;
    private Button btnResetPwd;
    private ProgressBar progressBar;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editEmail= (EditText) findViewById(R.id.edtRecEmail);
        btnResetPwd= (Button) findViewById(R.id.btnReset);
        progressBar= (ProgressBar) findViewById(R.id.progressBar3);

        mAuth = FirebaseAuth.getInstance();

        btnResetPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword(){
        String email = editEmail.getText().toString().trim();

        if(email.isEmpty()){
            editEmail.setError("Email required!");
            editEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editEmail.setError("Invalid email!");
            editEmail.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPassword.this, "Check email to reset pasword!", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(ForgotPassword.this, "Something wen wrong! Please try again!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

/*CodeWithMazn, 2020. #6 Login and Registration Android App Tutorial Using Firebase Authentication - Reset Password.
[online] Youtube.com. Available at: <https://www.youtube.com/watch?v=w-Uv-ydX_LY> [Accessed 2 June 2021].
 */