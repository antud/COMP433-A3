package com.example.a3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button photoTaggerButton = findViewById(R.id.photoTagger);
        Button sketchTaggerButton = findViewById(R.id.sketchTagger);

        photoTaggerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PhotoTaggerActivity.class);
            startActivity(intent);
        });

        sketchTaggerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SketchTaggerActivity.class);
            startActivity(intent);
        });
    }
}