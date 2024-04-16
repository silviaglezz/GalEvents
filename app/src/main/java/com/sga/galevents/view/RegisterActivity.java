package com.sga.galevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sga.galevents.R;
import com.sga.galevents.controller.RegisterController;

public class RegisterActivity extends AppCompatActivity {
    EditText etNameReg, etEmailReg, etPassReg;
    TextView tvNameReg, tvEmailReg, tvPassReg;
    Button btnReg;
    ProgressBar progBarReg;

    private RegisterController controller;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNameReg = findViewById(R.id.etNameReg);
        etEmailReg = findViewById(R.id.etEmailReg);
        etPassReg = findViewById(R.id.etPassReg);
        tvNameReg = findViewById(R.id.tvNameReg);
        tvEmailReg = findViewById(R.id.tvEmailReg);
        tvPassReg = findViewById(R.id.tvPassReg);
        btnReg = findViewById(R.id.btnReg);
        progBarReg = findViewById(R.id.progBarReg);

        controller = new RegisterController(this);

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etNameReg.getText().toString().trim();
                String email = etEmailReg.getText().toString().trim();
                String password = etPassReg.getText().toString().trim();

                if(TextUtils.isEmpty(name)){
                    etNameReg.setError("Name is required");
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    etEmailReg.setError("Email is required");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    etPassReg.setError("Password is required");
                    return;
                }
                if (password.length() < 8){
                    etPassReg.setError("Password must be 8 or more characters");
                    return;
                }
                progBarReg.setVisibility(View.VISIBLE);

                controller.register(email, password, name);
            }
        });
    }

    public void goToLogin(){
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }

    public void showError(String errorMsg){
        Toast.makeText(RegisterActivity.this, "Error! " + errorMsg, Toast.LENGTH_SHORT).show();
        progBarReg.setVisibility(View.GONE);
    }

    public void sucessMsg(){
        Toast.makeText(RegisterActivity.this, "User created", Toast.LENGTH_SHORT).show();
    }
}
