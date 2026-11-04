package com.example.dailyassistant;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    private ImageView imgBack, imgEyesOpen, imgEyesClosed;
    private boolean isDatabaseReady = false;
    private boolean hasNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        imgBack = findViewById(R.id.imgBack);
        imgEyesOpen = findViewById(R.id.imgEyesOpen);
        imgEyesClosed = findViewById(R.id.imgEyesClosed);

        startSplashAnimation();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isDatabaseReady = true;
            }
        }, 1390);
    }

    private void startSplashAnimation() {
        ObjectAnimator rotateRight = ObjectAnimator.ofFloat(imgBack, "rotation", 0f, 90f);
        rotateRight.setDuration(500);

        ObjectAnimator closeEye = ObjectAnimator.ofFloat(imgEyesOpen, "alpha", 1f, 0f);
        ObjectAnimator showClosed = ObjectAnimator.ofFloat(imgEyesClosed, "alpha", 0f, 1f);
        closeEye.setDuration(250);
        showClosed.setDuration(250);

        AnimatorSet motion1 = new AnimatorSet();
        motion1.playTogether(rotateRight, closeEye, showClosed);

        ObjectAnimator waitInMiddle = ObjectAnimator.ofFloat(imgBack, "alpha", 1f, 1f);
        waitInMiddle.setDuration(100);

        ObjectAnimator rotateLeft = ObjectAnimator.ofFloat(imgBack, "rotation", 90f, 0f);
        rotateLeft.setDuration(500);

        ObjectAnimator openEye = ObjectAnimator.ofFloat(imgEyesOpen, "alpha", 0f, 1f);
        ObjectAnimator hideClosed = ObjectAnimator.ofFloat(imgEyesClosed, "alpha", 1f, 0f);
        openEye.setDuration(250);
        hideClosed.setDuration(250);

        AnimatorSet motion2 = new AnimatorSet();
        motion2.playTogether(rotateLeft, openEye, hideClosed);

        ObjectAnimator waitAtEnd = ObjectAnimator.ofFloat(imgBack, "alpha", 1f, 1f);
        waitAtEnd.setDuration(200);

        final AnimatorSet overallAnimation = new AnimatorSet();
        overallAnimation.playSequentially(motion1, waitInMiddle, motion2, waitAtEnd);
        overallAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

        overallAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if (hasNavigated) return;

                if (isDatabaseReady) {
                    openPermissionActivity();
                } else {
                    overallAnimation.start();
                }
            }
        });

        overallAnimation.start();
    }

    private void openPermissionActivity() {
        if (hasNavigated) return;

        hasNavigated = true;
        Intent intent = new Intent(MainActivity.this, PermissionActivity.class);
        startActivity(intent);
        finish();
    }
}
