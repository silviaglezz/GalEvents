package com.sga.galevents.controller;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.sga.galevents.model.LoginModel;
import com.sga.galevents.view.LoginActivity;

public class LoginController {
    private LoginActivity view;
    private LoginModel model;

    public LoginController(LoginActivity view){
        this.view = view;
        this.model = new LoginModel();
    }

    public void signIn(String email, String password){
        model.signIn(email, password, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    view.sucessMsg();
                    view.goToHome();
                }else{
                    view.showError(task.getException().getMessage());
                }
            }
        });
    }
}
