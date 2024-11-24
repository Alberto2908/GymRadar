package com.example.gyms;

import android.app.Application;

public class App extends Application {
    private UserLogged userLogged;

    public UserLogged getUserLogged() {
        return userLogged;
    }

    public void setUserLogged(UserLogged userLogged) {
        this.userLogged = userLogged;
    }
}