package pt.cmg.sweranker.ui.materialspinner;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import java.lang.reflect.Method;
import java.util.List;

import pt.cmg.sweranker.R;

/**
 * I did almost nothing of this class. I just got it from the internet, liked it and tweaked it
 * to display my list. I have zero clue how he does this, because I can't be bothered to read it
 * since my time is running out. Time constraints, hate them.
 * <p>
 * I changed the dropdown view from Listview to RecyclerView as it is much much easier to use
 * multiple views in the list.
 * <p>
 * It still has a lot of rubbish, but the show must go on.
 */
public class MaterialSpinner extends AppCompatTextView {

    private OnNothingSelectedListener _onNothingSelectedListener;

    private Object _selectedObject = null;
    private MaterialSpinnerBaseAdapter _adapter;
    private PopupWindow _popupWindow;
    private RecyclerView _listView;
    private Drawable _arrowDrawable;
    private boolean _hideArrow;
    private boolean _isNothingSelected;
    private int _popupWindowMaxHeight;
    private int _popupWindowHeight;
    private int _selectedIndex;
    private int _backgroundColor;
    private int _arrowColor;
    private int _isArrowColorDisabled;
    private int _textColor;
    private int _numberOfItems;

    public MaterialSpinner(Context context) {
        super(context);
        init(context, null);
    }

    public MaterialSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MaterialSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        TypedArray styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.MaterialSpinner);
        int defaultColor = getTextColors().getDefaultColor();
        boolean rtl = Utils.isRtl(context);

        try {
            _backgroundColor = styledAttributes.getColor(R.styleable.MaterialSpinner_materialspinner_background_color, Color.WHITE);
            _textColor = styledAttributes.getColor(R.styleable.MaterialSpinner_materialspinner_text_color, defaultColor);
            _arrowColor = styledAttributes.getColor(R.styleable.MaterialSpinner_materialspinner_arrow_tint, _textColor);
            _hideArrow = styledAttributes.getBoolean(R.styleable.MaterialSpinner_materialspinner_hide_arrow, false);
            _popupWindowMaxHeight = styledAttributes.getDimensionPixelSize(R.styleable.MaterialSpinner_materialspinner_dropdown_max_height, 0);
            _popupWindowHeight = styledAttributes.getDimensionPixelSize(R.styleable.MaterialSpinner_materialspinner_dropdown_height, 0);
            _isArrowColorDisabled = Utils.lighter(_arrowColor, 0.8f);
        } finally {
            styledAttributes.recycle();
        }

        Resources resources = getResources();
        int left, right, bottom, top;
        left = right = bottom = top = resources.getDimensionPixelSize(R.dimen.material_spinner_padding_top);
        if (rtl) {
            right = resources.getDimensionPixelSize(R.dimen.material_spinner_padding_left);
        } else {
            left = resources.getDimensionPixelSize(R.dimen.material_spinner_padding_left);
        }

        setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        setClickable(true);
        setPadding(left, top, right, bottom);
        setBackgroundResource(R.drawable.material_spinner_selector);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && rtl) {
            setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            setTextDirection(View.TEXT_DIRECTION_RTL);
        }

        if (!_hideArrow) {
            _arrowDrawable = Utils.getDrawable(context, R.drawable.material_spinner_arrow).mutate();
            _arrowDrawable.setColorFilter(_arrowColor, PorterDuff.Mode.SRC_IN);
            if (rtl) {
                setCompoundDrawablesWithIntrinsicBounds(_arrowDrawable, null, null, null);
            } else {
                setCompoundDrawablesWithIntrinsicBounds(null, null, _arrowDrawable, null);
            }
        }

        _listView = new RecyclerView(context);
        _listView.setId(getId());
        _listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        _popupWindow = new PopupWindow(context);
        _popupWindow.setContentView(_listView);
        _popupWindow.setOutsideTouchable(true);
        _popupWindow.setFocusable(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            _popupWindow.setElevation(16);
            _popupWindow.setBackgroundDrawable(Utils.getDrawable(context, R.drawable.material_spinner_drawable));
        } else {
            _popupWindow.setBackgroundDrawable(Utils.getDrawable(context, R.drawable.material_spinner_drop_down_shadow));
        }

        if (_backgroundColor != Color.WHITE) { // default color is white
            setBackgroundColor(_backgroundColor);
        }
        if (_textColor != defaultColor) {
            setTextColor(_textColor);
        }

        _popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                if (_isNothingSelected && _onNothingSelectedListener != null) {
                    _onNothingSelectedListener.onNothingSelected(MaterialSpinner.this);
                }
                if (!_hideArrow) {
                    animateArrow(false);
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        _popupWindow.setWidth(MeasureSpec.getSize(widthMeasureSpec));
        _popupWindow.setHeight(calculatePopupWindowHeight());
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isEnabled() && isClickable()) {
                if (!_popupWindow.isShowing()) {
                    expand();
                } else {
                    collapse();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void setBackgroundColor(int color) {
        _backgroundColor = color;
        Drawable background = getBackground();
        if (background instanceof StateListDrawable) { // pre-L
            try {
                Method getStateDrawable = StateListDrawable.class.getDeclaredMethod("getStateDrawable", int.class);
                if (!getStateDrawable.isAccessible()) getStateDrawable.setAccessible(true);
                int[] colors = {Utils.darker(color, 0.85f), color};
                for (int i = 0; i < colors.length; i++) {
                    ColorDrawable drawable = (ColorDrawable) getStateDrawable.invoke(background, i);
                    drawable.setColor(colors[i]);
                }
            } catch (Exception e) {
                Log.e("MaterialSpinner", "Error setting background color", e);
            }
        } else if (background != null) { // 21+ (RippleDrawable)
            background.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        _popupWindow.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void setTextColor(int color) {
        _textColor = color;
        super.setTextColor(color);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("state", super.onSaveInstanceState());
        bundle.putInt("selected_index", _selectedIndex);
        if (_popupWindow != null) {
            bundle.putBoolean("is_popup_showing", _popupWindow.isShowing());
            collapse();
        } else {
            bundle.putBoolean("is_popup_showing", false);
        }
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable savedState) {
        if (savedState instanceof Bundle) {
            Bundle bundle = (Bundle) savedState;
            _selectedIndex = bundle.getInt("selected_index");
            if (_adapter != null) {
                setText(_adapter.getItem(_selectedIndex).toString());
            }
            if (bundle.getBoolean("is_popup_showing")) {
                if (_popupWindow != null) {
                    // Post the show request into the looper to avoid bad token exception
                    post(new Runnable() {

                        @Override
                        public void run() {
                            expand();
                        }
                    });
                }
            }
            savedState = bundle.getParcelable("state");
        }
        super.onRestoreInstanceState(savedState);
    }

    public void setSelectedItemText(String text) {
        setText(text);
    }


    public void setSelectedObject(Object selectedObject) {
        _selectedObject = selectedObject;
    }

    public Object getSelectedObject() {
        return _selectedObject;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (_arrowDrawable != null) {
            _arrowDrawable.setColorFilter(enabled ? _arrowColor : _isArrowColorDisabled, PorterDuff.Mode.SRC_IN);
        }
    }

    /**
     * @return the selected item position
     */
    public int getSelectedIndex() {
        return _selectedIndex;
    }

    /**
     * Set the default spinner item using its index
     *
     * @param position the item's position
     */
    public void setSelectedIndex(int position) {
        if (_adapter != null) {

            if (!_adapter.isValidPosition(position)) {
                throw new IllegalArgumentException("This is not a valid position for this adapter! Likely a separator view of some sorts.");
            }

            if (position >= 0 && position <= _adapter.getItemCount()) {
                _selectedIndex = position;
                _selectedObject = _adapter.getItem(position);
                setText(_adapter.getItemName(position));
            } else {
                throw new IllegalArgumentException("Position must be lower than adapter count!");
            }
        }
    }


    /**
     * Get the list of items in the adapter
     *
     * @return A list of items or {@code null} if no items are set.
     */
    public List<Object> getItems() {
        if (_adapter == null) {
            return null;
        }
        return _adapter.getItems();
    }


    /**
     * Sets the Adapter for this Spinner. Internally this uses a Recycler View, so it is really the same process
     * as writing an adapter for a Recycler View.
     *
     * @param adapter
     */
    public void setAdapter(MaterialSpinnerBaseAdapter adapter) {
        _adapter = adapter;
        adapter.setOnItemSelectedListener(new MaterialSpinnerBaseAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Object item, String textToSet, int colorResource, int position) {
                _selectedObject = item;
                _selectedIndex = position;
                setText(textToSet);
                setTextColor(colorResource);
                collapse();
            }
        });
        setAdapterInternal(adapter);
    }


    private void setAdapterInternal(@NonNull MaterialSpinnerBaseAdapter adapter) {
        _listView.setAdapter(adapter);

        if (_selectedIndex >= _numberOfItems) {
            _selectedIndex = 0;
        }

        // Initialises the spinner placeholder.
        setText(getContext().getString(R.string.select_topic));
    }

    /**
     * Show the dropdown menu
     */
    public void expand() {
        if (!_hideArrow) {
            animateArrow(true);
        }
        _isNothingSelected = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            _popupWindow.setOverlapAnchor(false);
            _popupWindow.showAsDropDown(this);
        } else {
            int[] location = new int[2];
            getLocationOnScreen(location);
            int x = location[0];
            int y = getHeight() + location[1];
            _popupWindow.showAtLocation(this, Gravity.TOP | Gravity.START, x, y);
        }
    }

    /**
     * Closes the dropdown menu
     */
    public void collapse() {
        if (!_hideArrow) {
            animateArrow(false);
        }
        _popupWindow.dismiss();
    }

    /**
     * Set the tint color for the dropdown arrow
     *
     * @param color the color value
     */
    public void setArrowColor(@ColorInt int color) {
        _arrowColor = color;
        _isArrowColorDisabled = Utils.lighter(_arrowColor, 0.8f);
        if (_arrowDrawable != null) {
            _arrowDrawable.setColorFilter(_arrowColor, PorterDuff.Mode.SRC_IN);
        }
    }

    private void animateArrow(boolean shouldRotateUp) {
        int start = shouldRotateUp ? 0 : 10000;
        int end = shouldRotateUp ? 10000 : 0;
        ObjectAnimator animator = ObjectAnimator.ofInt(_arrowDrawable, "level", start, end);
        animator.start();
    }

    /**
     * Set the maximum height of the dropdown menu.
     *
     * @param height the height in pixels
     */
    public void setDropdownMaxHeight(int height) {
        _popupWindowMaxHeight = height;
        _popupWindow.setHeight(calculatePopupWindowHeight());
    }

    /**
     * Set the height of the dropdown menu
     *
     * @param height the height in pixels
     */
    public void setDropdownHeight(int height) {
        _popupWindowHeight = height;
        _popupWindow.setHeight(calculatePopupWindowHeight());
    }

    private int calculatePopupWindowHeight() {
        float listViewHeight = _adapter.getItemCount() * getResources().getDimension(R.dimen.material_spinner_item_height);
        if (_popupWindowMaxHeight > 0 && listViewHeight > _popupWindowMaxHeight) {
            return _popupWindowMaxHeight;
        }
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    /**
     * Get the {@link PopupWindow}.
     *
     * @return The {@link PopupWindow} that is displayed when the view has been clicked.
     */

    public PopupWindow getPopupWindow() {
        return _popupWindow;
    }

    /**
     * Interface definition for a callback to be invoked when an item in this view has been selected.
     */
    public interface OnItemSelectedListener {

        /**
         * Nothing at the moment.
         */
        void onItemSelected();

    }

    /**
     * Interface definition for a callback to be invoked when the dropdown is dismissed and no item was selected.
     */
    public interface OnNothingSelectedListener {

        /**
         * Also nothing. If I ever make a library out of this I really should do something interesting here.
         */
        void onNothingSelected(MaterialSpinner spinner);
    }

}
