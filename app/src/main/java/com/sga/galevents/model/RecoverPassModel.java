package com.sga.galevents.model;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class RecoverPassModel {
    private FirebaseAuth fauth;
    private FirebaseFirestore fStore;


    public RecoverPassModel(){
        fauth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    public void recoverPass(String email, OnCompleteListener<Void> listener){
        fauth.sendPasswordResetEmail(email).addOnCompleteListener(listener);
    }

    public void existsEmail(String email, OnCompleteListener<QuerySnapshot> listener){
        fStore.collection("users").whereEqualTo("email", email).get()
                .addOnCompleteListener(listener);
    }
}
