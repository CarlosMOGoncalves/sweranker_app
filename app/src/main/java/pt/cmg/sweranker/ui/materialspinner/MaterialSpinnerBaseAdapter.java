package pt.cmg.sweranker.ui.materialspinner;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Most of this Adapter was... adapter from Material Spinner.
 * More could be added, but as of right now I am not really thinking of reuse.
 */

public abstract class MaterialSpinnerBaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context _context;
    private int _selectedIndex;
    private int _textColor;
    protected OnItemSelectedListener _onItemSelectedListener;

    public MaterialSpinnerBaseAdapter(Context context) {
        _context = context;
    }

    public void setOnItemSelectedListener(MaterialSpinnerBaseAdapter.OnItemSelectedListener listener) {
        _onItemSelectedListener = listener;
    }

    public void removeOnItemSelectedListener() {
        _onItemSelectedListener = null;
    }


    public OnItemSelectedListener getListener() {
        return _onItemSelectedListener;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public abstract Object getItem(int position);

    public abstract String getItemName(int position);

    public abstract boolean isValidPosition(int position);

    public abstract List<Object> getItems();

    public MaterialSpinnerBaseAdapter setTextColor(int textColor) {
        this._textColor = textColor;
        return this;
    }

    public int getTextColor() {
        return _textColor;
    }


    /**
     * This is just a communication interface so that someone can listen to this adapter whenever an item was chosen.
     */
    public interface OnItemSelectedListener {
        void onItemSelected(Object selectedObject, String textToSet, int selectedIndex);
    }
}

