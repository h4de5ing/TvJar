package com.example.tvjar;

import android.app.Activity;
import android.os.Bundle;

import com.github.catvod.spider.Czsapp;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(() -> {
            String home = new Czsapp().homeContent(true);
            System.out.println("home=" + home);
        }).start();
    }
}