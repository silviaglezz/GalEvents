package com.sga.galevents.model;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginModel {
    private FirebaseAuth fauth;

    public LoginModel(){
        fauth = FirebaseAuth.getInstance();
    }

    public void signIn(String email, String password, OnCompleteListener<AuthResult> listener){
        fauth.signInWithEmailAndPassword(email,password).addOnCompleteListener(listener);
    }
}
