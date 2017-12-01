package pt.cmg.sweranker.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import pt.cmg.sweranker.util.SweRankerUtils;


/**
 * This Item Decorator will draw an offset outside of the view rectangle.
 * Meaning that it will create some spacing outside the view that is drawing at the moment.
 * <p>
 * To define which side gets the offsets the enum Side must be used. If multiple Sides are used
 * then it will offset each of the selected ones and these ARE NOT cumulative (i.e. you cannot
 * pass multiple times LEFT and expect it to increase the left offset).
 */
public class ConstantSpacingItemDecorator extends RecyclerView.ItemDecoration {

    public enum Side {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        ALL_SIDES
    }

    private int _spacingInDp;
    private Side[] _sideToOffset;
    private Context _context;


    public ConstantSpacingItemDecorator(Context context, int spacingInDp, @NonNull Side... sideToOffset) {
        _context = context;
        _spacingInDp = spacingInDp;
        _sideToOffset = sideToOffset;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        int pixelsToOffset = SweRankerUtils.convertDpToPixels(_context, _spacingInDp);

        for (int i = 0; i < _sideToOffset.length; i++) {
            Side currentSideToOffset = _sideToOffset[i];

            switch (currentSideToOffset) {
                case LEFT:
                    outRect.left = pixelsToOffset;
                    break;
                case RIGHT:
                    outRect.right = pixelsToOffset;
                    break;
                case TOP:
                    outRect.top = pixelsToOffset;
                    break;
                case BOTTOM:
                    outRect.bottom = pixelsToOffset;
                    break;
                default:
                    outRect.bottom = outRect.top = outRect.left = outRect.right = pixelsToOffset;
            }

        }
    }


    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }
}
