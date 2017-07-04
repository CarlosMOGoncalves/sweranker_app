package pt.cmg.sweranker.ui.materialspinner;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
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


    private Dialog _dialogWindow;
    private PopupWindow _popupWindow;
    private RecyclerView _recyclerView;
    private Drawable _arrowDrawable;

    private boolean _isDialogMode;
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


    /**
     * This is a constructor tailor-made to allow dialog mode instead of the normal Popup Window mode.
     * As the Popup Window mode was only really useful when the Spinner control was placed in a view on
     * top of the window and in this context (SweRanker app) I have multiple ones well below in the
     * window, a dialog will do just fine.
     *
     * @param context      the acticity or app context
     * @param isDialogMode true if the dialog mode is needed
     */
    public MaterialSpinner(Context context, boolean isDialogMode) {
        super(context);
        _isDialogMode = isDialogMode;
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


        boolean isRightToLeft = Utils.isRtl(context);

        try {
            _backgroundColor = styledAttributes.getColor(R.styleable.MaterialSpinner_materialspinner_background_color, Color.WHITE);
            _textColor = styledAttributes.getColor(R.styleable.MaterialSpinner_materialspinner_text_color, getTextColors().getDefaultColor());
            _arrowColor = styledAttributes.getColor(R.styleable.MaterialSpinner_materialspinner_arrow_tint, _textColor);
            _hideArrow = styledAttributes.getBoolean(R.styleable.MaterialSpinner_materialspinner_hide_arrow, false);
            _isDialogMode = styledAttributes.getBoolean(R.styleable.MaterialSpinner_materialspinner_dialog_mode, _isDialogMode ? true : false);
            _popupWindowMaxHeight = styledAttributes.getDimensionPixelSize(R.styleable.MaterialSpinner_materialspinner_dropdown_max_height, 0);
            _popupWindowHeight = styledAttributes.getDimensionPixelSize(R.styleable.MaterialSpinner_materialspinner_dropdown_height, 0);
            _isArrowColorDisabled = Utils.lighter(_arrowColor, 0.8f);
        } finally {
            styledAttributes.recycle();
        }

        Resources resources = getResources();
        int left, right, bottom, top;
        left = right = bottom = top = resources.getDimensionPixelSize(R.dimen.material_spinner_padding_top);
        if (isRightToLeft) {
            right = resources.getDimensionPixelSize(R.dimen.material_spinner_padding_left);
        } else {
            left = resources.getDimensionPixelSize(R.dimen.material_spinner_padding_left);
        }

        // Isto é para o efeito quando se clica, o ripple. Francamente não percebo a diferença de cores.
        setBackgroundResource(R.drawable.material_spinner_selector);

        // Isto quase de certeza tem que ver com acessibilidade, mas o meu é sempre LeftToRight, so...
        if (isRightToLeft) {
            setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            setTextDirection(View.TEXT_DIRECTION_RTL);
        }

        // Isto literalmente carrega e coloca o desenho da seta ao lado da Textview
        if (!_hideArrow) {
            _arrowDrawable = Utils.getDrawable(context, R.drawable.material_spinner_arrow).mutate();
            _arrowDrawable.setColorFilter(_arrowColor, PorterDuff.Mode.SRC_IN);
            if (isRightToLeft) {
                setCompoundDrawablesWithIntrinsicBounds(_arrowDrawable, null, null, null);
            } else {
                setCompoundDrawablesWithIntrinsicBounds(null, null, _arrowDrawable, null);
            }
        }

        // Aqui para baixo vem a parte da lista de escolhas do spinner em si
        _recyclerView = new RecyclerView(context);
        _recyclerView.setId(getId());
        _recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        if (_isDialogMode) {

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.getContext());

            dialogBuilder
                    .setView(_recyclerView)
                    .setNegativeButton(getResources().getString(R.string.dismiss), (dialog, id) ->
                            dialog.cancel()
                    );

            _dialogWindow = dialogBuilder.create();
        } else {
            _popupWindow = new PopupWindow(context);
            _popupWindow.setContentView(_recyclerView);
            _popupWindow.setOutsideTouchable(true);
            _popupWindow.setFocusable(true);
            // Propriedades da lista popup
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                _popupWindow.setElevation(16);
                _popupWindow.setBackgroundDrawable(Utils.getDrawable(context, R.drawable.material_spinner_drawable));
            } else {
                _popupWindow.setBackgroundDrawable(Utils.getDrawable(context, R.drawable.material_spinner_drop_down_shadow));
            }

            _popupWindow.setOnDismissListener(() -> {
                if (_isNothingSelected && _onNothingSelectedListener != null) {
                    _onNothingSelectedListener.onNothingSelected(MaterialSpinner.this);
                }
                if (!_hideArrow) {
                    animateArrow(false);
                }
            });
        }

        setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        setClickable(true);
        setPadding(left, top, right, bottom);
        setBackgroundColor(_backgroundColor);
        setTextColor(_textColor);

    }

    /**
     * This animates the arrow when clicked. It is a cool effect, thumbs up for who did it!
     *
     * @param shouldRotateUp true if it should rotate upwards
     */
    private void animateArrow(boolean shouldRotateUp) {
        int start = shouldRotateUp ? 0 : 10000;
        int end = shouldRotateUp ? 10000 : 0;
        ObjectAnimator animator = ObjectAnimator.ofInt(_arrowDrawable, "level", start, end);
        animator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (_isDialogMode) {

        } else {
            _popupWindow.setWidth(MeasureSpec.getSize(widthMeasureSpec));
            _popupWindow.setHeight(calculatePopupWindowHeight());
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int calculatePopupWindowHeight() {
        float listViewHeight = _adapter.getItemCount() * getResources().getDimension(R.dimen.material_spinner_item_height);
        if (_popupWindowMaxHeight > 0 && listViewHeight > _popupWindowMaxHeight) {
            return _popupWindowMaxHeight;
        }
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {

            // Mostra o menu se está activo.
            if (isEnabled() && isClickable()) {

                if (!_dialogWindow.isShowing() || !_popupWindow.isShowing()) {
                    showSpinnerMenu();
                } else {
                    dismissSpinnerMenu();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * Show the dropdown menu of this spinner
     */
    public void showSpinnerMenu() {
        if (!_hideArrow) {
            animateArrow(true);
        }
        _isNothingSelected = true;
        if (_isDialogMode) {
            _dialogWindow.show();
        } else {
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

    }

    /**
     * Closes the dropdown menu
     */
    public void dismissSpinnerMenu() {
        if (!_hideArrow) {
            animateArrow(false);
        }

        if (_isDialogMode) {
            _dialogWindow.dismiss();
        } else {
            _popupWindow.dismiss();
        }
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

        if (_isDialogMode) {

        } else {
            _popupWindow.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

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
            dismissSpinnerMenu();
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
                            showSpinnerMenu();
                        }
                    });
                }
            }
            savedState = bundle.getParcelable("state");
        }
        super.onRestoreInstanceState(savedState);
    }


    /**
     * This literally sets the text for the selected element spinner.
     * Really this is just a hardly used wrapper.
     *
     * @param text The text to be displayed.
     */
    public void setSelectedItemText(String text) {
        setText(text);
    }


    /**
     * This literally sets the text for the selected element.
     * Really this is just a hardly used wrapper.
     * Mostly values will be set from the adapter.
     *
     * @param selectedObject The object selected
     */
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
        adapter.setOnItemSelectedListener((item, textToSet, colorResource, position) -> {
            _selectedObject = item;
            _selectedIndex = position;
            setText(textToSet);
            setTextColor(colorResource);
            dismissSpinnerMenu();
        });
        setAdapterInternal(adapter);
    }


    private void setAdapterInternal(@NonNull MaterialSpinnerBaseAdapter adapter) {
        _recyclerView.setAdapter(adapter);

        if (_selectedIndex >= _numberOfItems) {
            _selectedIndex = 0;
        }

        // Initialises the spinner placeholder.
        setText(getContext().getString(R.string.select_topic));
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
