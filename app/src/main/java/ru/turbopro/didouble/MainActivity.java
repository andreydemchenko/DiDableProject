package ru.turbopro.didouble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        constraintLayout = findViewById(R.id.constraintLayoutMain);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_foreground);
        ImageButton imgbt = (ImageButton)findViewById(R.id.imgBtnOpenChat);
        Animation ranim = (Animation) AnimationUtils.loadAnimation(this, R.anim.rotate);

        imgbt.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    imgbt.startAnimation(ranim);
                    onStartAnimation();
                    break;

                case MotionEvent.ACTION_UP:
                    imgbt.clearAnimation();
                    break;
            }
            return false;
        });

        imgbt.setOnClickListener(v -> {
            tempDisableButton(v);
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });
    }

    private void onStartAnimation() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofObject(constraintLayout, "backgroundColor",
                new ArgbEvaluator(),
                ContextCompat.getColor(this, R.color.red),
                ContextCompat.getColor(this, R.color.black));

        objectAnimator.setRepeatCount(1);
        objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        objectAnimator.setDuration(1000);
        objectAnimator.start();
    }

    public void tempDisableButton(View viewBtn) {
        final View button = viewBtn;
        button.setEnabled(false);
        button.postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(true);
            }
        }, 2000);
    }
}