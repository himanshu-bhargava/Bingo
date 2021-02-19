package com.sb.play.media;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.sb.play.bingo.R;

public class AnimationMedia {
    private Context context;
    private Animation rotate;

    public AnimationMedia(Context context) {
        rotate = AnimationUtils.loadAnimation(context, R.anim.rotate);
    }

    public void animateRotation(View view) {
        view.startAnimation(rotate);
    }
}
