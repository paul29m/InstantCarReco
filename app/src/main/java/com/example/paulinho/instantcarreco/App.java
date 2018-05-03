package com.example.paulinho.instantcarreco;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class App extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        if(FirebaseApp.getApps(this).isEmpty())
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
    }
}
