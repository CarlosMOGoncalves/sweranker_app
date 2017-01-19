package pt.cmg.sweranker.ui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.annotation.ColorInt;
import android.support.v7.widget.Toolbar;

/**
 * Created by Carlos on 19/01/2017.
 */

public class UXUtils {

    public static void animateActionBarColourChange(Toolbar toolbar, @ColorInt int targetColour, long duration, long startDelay) {
        Integer colorFrom = toolbar.getSolidColor();
        Integer colorTo = targetColour;

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);

        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                toolbar.setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });

        colorAnimation.setDuration(duration);
        colorAnimation.setStartDelay(startDelay);
        colorAnimation.start();

    }

    public static void animateStatusBarColourChange(Activity hostActivity, @ColorInt int targetColour) {
        Integer colorStatusFrom = hostActivity.getWindow().getStatusBarColor();
        Integer colorStatusTo = targetColour;

        ValueAnimator colorStatusAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorStatusFrom, colorStatusTo);


        colorStatusAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                hostActivity.getWindow().setStatusBarColor((Integer) animator.getAnimatedValue());
            }
        });

        colorStatusAnimation.setDuration(500);
        colorStatusAnimation.setStartDelay(0);
        colorStatusAnimation.start();
    }
}
