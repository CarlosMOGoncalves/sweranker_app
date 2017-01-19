package pt.cmg.sweranker.ui;

import android.transition.Transition;

public abstract class OnEndTransitionListener implements Transition.TransitionListener {

    @Override
    public void onTransitionStart(Transition transition) {

    }

    @Override
    public void onTransitionEnd(Transition transition) {
        onEndTransition(transition);
    }

    public abstract void onEndTransition(Transition transition);

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
