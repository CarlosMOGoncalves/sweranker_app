package pt.cmg.sweranker.ui.materialspinner;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Created by Carlos on 21/02/2017.
 */

public abstract class MaterialSpinnerBaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context _context;
    private int _selectedIndex;
    private int _textColor;

    public MaterialSpinnerBaseAdapter(Context context) {
        this._context = context;
    }

    public int getSelectedIndex() {
        return _selectedIndex;
    }

    public void notifyItemSelected(int index) {
        _selectedIndex = index;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public abstract Object getItem(int position);

    public abstract List<Object> getItems();

    public MaterialSpinnerBaseAdapter setTextColor(int textColor) {
        this._textColor = textColor;
        return this;
    }

    public int getTextColor() {
        return _textColor;
    }


}

