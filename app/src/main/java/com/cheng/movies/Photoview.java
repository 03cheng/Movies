package com.cheng.movies;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;

public class Photoview extends AppCompatActivity {
    private String imagePath;
    private PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoview);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
        Intent intent = getIntent();
        if (intent != null) {
            imagePath = intent.getStringExtra("ImagePath");
            photoView = (PhotoView)findViewById(R.id.photoView);
            photoView.enable();
            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            //photoView.animaFrom();//动画
            Glide.with(this).load("http://image.tmdb.org/t/p/original" + imagePath).into(photoView);
        }
    }
}
