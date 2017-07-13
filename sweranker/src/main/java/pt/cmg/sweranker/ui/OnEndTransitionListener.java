package pt.cmg.sweranker.ui;

import android.transition.Transition;

/**
 * This class is basically a Wrapper around the Transition Listener so that I wouldn't have to clutter
 * the code with writing a bunch of empty hooks such as this class does.
 * <p>
 * It hides this stuff by declaring one only abstract method that will be used by the actual callback.
 * This way, by using this class as the listener you only have to implement what you want to do when
 * the Transition ends.
 */
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
