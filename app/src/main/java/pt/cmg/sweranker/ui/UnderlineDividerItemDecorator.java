package pt.cmg.sweranker.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import pt.cmg.sweranker.util.SweRankerUtils;

/**
 * This Item Decorator is used to draw a simple underline below ViewHolders.
 * This can be every ViewHolder or a specific one passed as parameter.
 * <p>
 * This Item Decorator uses a Builder to be constructed, so the right way to do it is:
 * new UnderlineDividerItemDecorator.Builder(...).leftInset(...).build()
 */
public class UnderlineDividerItemDecorator extends RecyclerView.ItemDecoration {

    private final Class<?> _targetViewHolderClass;
    private final Paint _paint;
    private final int _leftInset;
    private final int _rightInset;
    private final int _dividerHeight;
    private final int _skippableViews;

    private UnderlineDividerItemDecorator(Builder builder) {
        _targetViewHolderClass = builder._targetViewHolderClass;

        _leftInset = builder._leftInsetInPixels;
        _rightInset = builder._rightInsetInPixels;

        _dividerHeight = builder._dividerHeightInPixels;

        _skippableViews = builder._skippableViews;

        _paint = new Paint();
        _paint.setColor(builder._colour);
        _paint.setStyle(Paint.Style.STROKE);
        _paint.setStrokeWidth(builder._dividerHeightInPixels);
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();
        if (childCount < _skippableViews) return;

        RecyclerView.LayoutManager lm = parent.getLayoutManager();
        float[] lines = new float[childCount * 4];
        boolean hasDividers = false;

        for (int i = 0; i < childCount; i++) {

            View child = parent.getChildAt(i);

            if (_targetViewHolderClass != null && parent.getChildViewHolder(child).getClass() != _targetViewHolderClass) {
                continue;
            }

            lines[i * 4] = _leftInset + lm.getDecoratedLeft(child);
            lines[(i * 4) + 2] = lm.getDecoratedRight(child) - _rightInset;

            int y = lm.getDecoratedBottom(child) + (int) child.getTranslationY() - _dividerHeight;
            lines[(i * 4) + 1] = y;
            lines[(i * 4) + 3] = y;
            hasDividers = true;

        }
        if (hasDividers) {
            canvas.drawLines(lines, _paint);
        }
    }


    public static class Builder {

        // Mandatory Parameter
        private final Context _context;
        private final int _colour;
        private final int _dividerHeightInPixels;

        //Optional Parameters
        private Class _targetViewHolderClass = null;
        private int _leftInsetInPixels = 0;
        private int _rightInsetInPixels = 0;
        private int _skippableViews = 0;

        public Builder(Context context, @ColorInt int dividerColour, int dividerHeightInDps) {
            _context = context;
            _colour = dividerColour;
            _dividerHeightInPixels = SweRankerUtils.convertDpToPixels(_context, dividerHeightInDps);
        }

        public Builder targetViewHolderClass(Class targetViewHolderClass) {
            _targetViewHolderClass = targetViewHolderClass;
            return this;
        }

        public Builder leftInset(int leftInsetInDps) {
            _leftInsetInPixels = SweRankerUtils.convertDpToPixels(_context, leftInsetInDps);
            return this;
        }

        public Builder rightInset(int rightInsetInDps) {
            _rightInsetInPixels = SweRankerUtils.convertDpToPixels(_context, rightInsetInDps);
            return this;
        }

        public Builder skipViews(int skippableViews) {
            _skippableViews = skippableViews;
            return this;
        }

        public UnderlineDividerItemDecorator build() {
            return new UnderlineDividerItemDecorator(this);
        }

    }
}

