package pt.cmg.sweranker.ui;

import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;


public class ImageSizeAndPlaceTransition extends TransitionSet {
    public ImageSizeAndPlaceTransition() {
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds()).
                addTransition(new ChangeTransform()).
                addTransition(new ChangeImageTransform());
    }
}
