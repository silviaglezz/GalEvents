package com.sga.galevents.controller;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.sga.galevents.model.RegisterModel;
import com.sga.galevents.view.RegisterActivity;

public class RegisterController {
    private RegisterActivity view;
    private RegisterModel model;

    public RegisterController(RegisterActivity view){
        this.view = view;
        this.model = new RegisterModel();
    }

    public void register(String email, String password, String name){
        model.register(email, password, name, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    view.sucessMsg();
                    view.goToLogin();
                }else{
                    view.showError(task.getException().getMessage());
                }
            }
        });
    }
}
