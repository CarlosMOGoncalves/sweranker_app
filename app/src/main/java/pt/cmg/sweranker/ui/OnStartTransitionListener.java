package pt.cmg.sweranker.ui;

import android.transition.Transition;

/**
 * Created by Carlos on 19/01/2017.
 */

public abstract class OnStartTransitionListener implements Transition.TransitionListener {
    @Override
    public void onTransitionStart(Transition transition) {
        onStartTransition(transition);
    }

    public abstract void onStartTransition(Transition transition);

    @Override
    public void onTransitionEnd(Transition transition) {
    }


    @Override
    public void onTransitionCancel(Transition transition) {

    }

    @Override
    public void onTransitionPause(Transition transition) {

    }

    @Override
    public void onTransitionResume(Transition transition) {

    }
}
