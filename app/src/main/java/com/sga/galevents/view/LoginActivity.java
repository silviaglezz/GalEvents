package com.sga.galevents.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sga.galevents.Event;
import com.sga.galevents.R;
import com.sga.galevents.controller.LoginController;
import com.sga.galevents.io.TicketMasterApiAdapter;

import java.util.ArrayList;

import retrofit2.Call;

public class LoginActivity extends AppCompatActivity{
    EditText etEmailLog, etPassLog;
    TextView tvEmailLog, tvPassLog, tvCreateUser, tvRecoverPass;
    Button btnLog;
    ProgressBar progBarLog;
    private LoginController controller;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmailLog = findViewById(R.id.etEmailLog);
        etPassLog = findViewById(R.id.etPassLog);
        tvEmailLog = findViewById(R.id.tvEmailLog);
        tvPassLog = findViewById(R.id.tvPassLog);
        tvRecoverPass = findViewById(R.id.tvRecoverPass);
        tvCreateUser = findViewById(R.id.tvCreateUser);
        btnLog = findViewById(R.id.btnLog);
        progBarLog = findViewById(R.id.progBarLog);

        controller = new LoginController(this);

        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmailLog.getText().toString().trim();
                String password = etPassLog.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    etEmailLog.setError("Email is required");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    etPassLog.setError("Password is required");
                    return;
                }
                if (password.length() < 8){
                    etPassLog.setError("Password must be 8 or more characters");
                    return;
                }
                progBarLog.setVisibility(View.VISIBLE);

                controller.signIn(email,password);
            }
        });

        tvCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegister();
            }
        });

        tvRecoverPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRecoverPass();
            }
        });
    }

    private void goToRegister() {
        startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
        finish();
    }

    public void goToHome(){
        startActivity(new Intent(getApplicationContext(),HomeActivity.class));
        finish();
    }

    private void goToRecoverPass() {
        startActivity(new Intent(getApplicationContext(),RecoverPassActivity.class));
        finish();
    }

    public void showError(String errorMsg){
        Toast.makeText(LoginActivity.this, "Error! " + errorMsg, Toast.LENGTH_SHORT).show();
        progBarLog.setVisibility(View.GONE);
    }

    public void sucessMsg(){
        Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
    }
}
