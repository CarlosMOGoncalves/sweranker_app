package pt.cmg.sweranker.ui;

import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;

/**
 * This TransitionSet basically executes  3 stock transitions on an image and it is the conjunction of all.
 * So it takes a view, drags it to the new position in the new scene while at the same time changing its scale, if needed.
 */
public class ImageSizeAndPlaceTransition extends TransitionSet {
    public ImageSizeAndPlaceTransition() {
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds()).
                addTransition(new ChangeTransform()).
                addTransition(new ChangeImageTransform());
    }
}
