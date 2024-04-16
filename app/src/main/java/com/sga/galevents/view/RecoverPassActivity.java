package com.sga.galevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sga.galevents.R;
import com.sga.galevents.controller.RecoverPassController;

public class RecoverPassActivity extends AppCompatActivity {
    EditText etEmailRecover;
    Button btnRecover;
    private RecoverPassController controller;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_pass);

        etEmailRecover = findViewById(R.id.etEmailRecover);
        btnRecover = findViewById(R.id.btnRecover);

        controller = new RecoverPassController(this);

        btnRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmailRecover.getText().toString().trim();

                if(TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    etEmailRecover.setError("Email invalid");
                    return;
                }
                controller.recoverPass(email);
            }
        });
    }

    public void goToLogin(){
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }
    public void sucessMsg(){
        Toast.makeText(RecoverPassActivity.this, "Correo enviado", Toast.LENGTH_SHORT).show();
    }

    public void showError(String errorMsg){
        Toast.makeText(RecoverPassActivity.this, "Error! " + errorMsg, Toast.LENGTH_SHORT).show();
    }
}
