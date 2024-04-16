package com.sga.galevents.model;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterModel {
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    public RegisterModel(){
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    public void register(String email, String password,String name, OnCompleteListener<AuthResult> listener){
        fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
           if (task.isSuccessful()){
               FirebaseUser fUser = fAuth.getCurrentUser();
               saveUser(fUser, name);
           }
           listener.onComplete(task);
        });
    }

    private void saveUser(FirebaseUser fUser, String name){
        if (fUser != null){
            Map<String, Object> user = new HashMap<>();
            user.put("name", name);
            user.put("email", fUser.getEmail());

            fStore.collection("users").document(fUser.getUid())
                    .set(user).addOnSuccessListener(aVoid -> {
                        Log.d("TAG","OnSuccess: user Profile is created for " + fUser.getUid());
                    }).addOnFailureListener(e -> {
                        Log.d("TAG","OnFailure: " + e.getMessage());
                    });
        }
    }
}
