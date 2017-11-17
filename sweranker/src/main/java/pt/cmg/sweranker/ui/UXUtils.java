package pt.cmg.sweranker.ui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.annotation.ColorInt;
import android.support.v7.widget.Toolbar;

/**
 * UXUtils is an utility class with a set of utility functions that are somehow related to UX, such as painting some View,
 * creating some animations etc.
 */
public class UXUtils {

    /**
     * Takes a toolbar and creates an animator to paint it in the colour passed as parameter with the duration and delay given.
     * It uses a Value Animator...not that I know what it does YET, but hey, deadlines...
     *
     * @param toolbar
     * @param targetColour
     * @param duration
     * @param startDelay
     */
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

    /**
     * Takes an Activity and creates an animator to paint the Status Bar it in the colour passed as parameter with the duration and delay given.
     * It uses a Value Animator...not that I know what it does YET, but hey, deadlines...
     *
     * @param hostActivity
     * @param targetColour
     * @param duration
     * @param startDelay
     */
    public static void animateStatusBarColourChange(Activity hostActivity, @ColorInt int targetColour, long duration, long startDelay) {
        Integer colorStatusFrom = hostActivity.getWindow().getStatusBarColor();
        Integer colorStatusTo = targetColour;

        ValueAnimator colorStatusAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorStatusFrom, colorStatusTo);


        colorStatusAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                hostActivity.getWindow().setStatusBarColor((Integer) animator.getAnimatedValue());
            }
        });

        colorStatusAnimation.setDuration(duration);
        colorStatusAnimation.setStartDelay(startDelay);
        colorStatusAnimation.start();
    }
}
