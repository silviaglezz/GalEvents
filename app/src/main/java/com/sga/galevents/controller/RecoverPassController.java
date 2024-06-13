package com.sga.galevents.controller;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;
import com.sga.galevents.model.RecoverPassModel;
import com.sga.galevents.view.RecoverPassActivity;

public class RecoverPassController {
    private RecoverPassActivity view;
    private RecoverPassModel model;

    public RecoverPassController(RecoverPassActivity view){
        this.view = view;
        this.model = new RecoverPassModel();
    }

    public void recoverPass(String email){
        model.existsEmail(email, new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    model.recoverPass(email, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                view.sucessMsg();
                                view.goToLogin();
                            }else{
                                view.showError(task.getException().getMessage());
                            }
                        }
                    });
                }else{
                    view.showError("Email no registrado");
                }
            }
        });
    }
}
