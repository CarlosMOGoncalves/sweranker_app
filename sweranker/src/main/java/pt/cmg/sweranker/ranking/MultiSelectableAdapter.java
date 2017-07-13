package pt.cmg.sweranker.ranking;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Used as a base class for the creation of multi-selectable RecyclerViews.
 * This was taken straight off the internet.
 */

public abstract class MultiSelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private SparseBooleanArray _selectedItems;

    public MultiSelectableAdapter() {
        _selectedItems = new SparseBooleanArray();
    }

    /**
     * Indicates the list of selected items
     *
     * @return List of selected items ids
     */
    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(_selectedItems.size());
        for (int i = 0; i < _selectedItems.size(); ++i) {
            items.add(_selectedItems.keyAt(i));
        }
        return items;
    }


    /**
     * Indicates if the item at position position is selected.
     *
     * @param position Position of the item to check
     * @return true if the item is selected, false otherwise
     */
    public boolean isSelected(int position) {
        return getSelectedItems().contains(position);
    }

    /**
     * Toggle the selection status of the item at a given position
     *
     * @param position Position of the item to toggle the selection status for
     */
    public void toggleSelection(int position) {
        if (_selectedItems.get(position, false)) {
            _selectedItems.delete(position);
        } else {
            _selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    /**
     * Clear the selection status for all items
     */
    public void clearSelection() {
        List<Integer> selection = getSelectedItems();
        _selectedItems.clear();
        for (Integer i : selection) {
            notifyItemChanged(i);
        }
    }

    /**
     * Count the selected items
     *
     * @return Selected items count
     */
    public int getSelectedItemCount() {
        return _selectedItems.size();
    }

    /**
     * Indicates if the adapter is in Select Mode, i.e. if it in the process of selecting items.
     *
     * @return
     */
    public boolean isInSelectedMode() {
        return _selectedItems.size() > 0;
    }

}
