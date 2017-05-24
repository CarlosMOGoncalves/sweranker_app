package pt.cmg.sweranker.ranking;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

import pt.cmg.sweranker.R;

/**
 * Created by Carlos on 28/03/2017.
 */

public class ScoresAndImagesAdapter extends MultiSelectableAdapter<ScoresAndImagesAdapter.ScoreImageViewHolder> {

    private Context _context;

    // Keys -> The degree score Id , Values -> the integer that is the resource of this degree image
    private Map<String, Integer> _scoresAndImages;

    // These indexes are just another view on the above Map, it is merely the degree score id in an array to use the indexes for positions
    private String[] _degreeCombinationIds;
    private OnScoresGridAdapterListener _listener;


    public ScoresAndImagesAdapter(Context context, Map<String, Integer> scoresAndImages, OnScoresGridAdapterListener listener) {
        super();
        _context = context;
        _scoresAndImages = scoresAndImages;
        _degreeCombinationIds = _scoresAndImages.keySet().toArray(new String[_scoresAndImages.size()]);
        _listener = listener;
    }


    @Override
    public ScoreImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ranking_card, parent, false);
        return new ScoreImageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ScoreImageViewHolder holder, int position) {
        holder._degreeLogo.setImageDrawable(_context.getDrawable(_scoresAndImages.get(_degreeCombinationIds[position])));
        holder._rankingName.setText(_degreeCombinationIds[position]);
        holder._selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return _scoresAndImages.size();
    }


    public class ScoreImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView _degreeLogo;
        private TextView _rankingName;
        private View _selectedOverlay;

        public ScoreImageViewHolder(View itemView) {
            super(itemView);

            _degreeLogo = (ImageView) itemView.findViewById(R.id.ranking_logo);
            _rankingName = (TextView) itemView.findViewById(R.id.ranking_name);
            _selectedOverlay = itemView.findViewById(R.id.selected_overlay);

            // On click, either we open a new fragment with the detailed score or we add a new element to the multi select Action Mode
            _degreeLogo.setOnClickListener(view -> {
                if (isInSelectedMode()) {

                    if (isSelected(getAdapterPosition())) {
                        toggleSelection(getAdapterPosition());
                        _listener.onItemUnselectedInActionMode(view, getSelectedItemCount());
                    } else {
                        toggleSelection(getAdapterPosition());
                        _listener.onItemSelectedInActionMode(view, getSelectedItemCount());
                    }

                } else {
                    _listener.loadDegreeChartsFragment(view, _degreeCombinationIds[getAdapterPosition()]);
                }
            });

            // In long click however we either enter Action Mode (if it's not active yet) or we do nothing.
            _degreeLogo.setOnLongClickListener(view -> {

                if (isInSelectedMode()) {
                    // If we're in select mode, there is really nothing to do.
                } else {
                    toggleSelection(getAdapterPosition());
                    _listener.startActionMode(view, _degreeCombinationIds[getAdapterPosition()]);
                }
                return true;
            });
        }
    }


    public interface OnScoresGridAdapterListener {

        /**
         * Loads the Chart Fragment for this Degree Combination
         *
         * @param rootView
         * @param degreeCombinationId
         */
        void loadDegreeChartsFragment(View rootView, String degreeCombinationId);


        void startActionMode(View selectedCardView, String degreeCombinationId);

        void onItemSelectedInActionMode(View selectedCardView, int numberOfItemsSelected);

        void onItemUnselectedInActionMode(View selectedCardView, int numberOfItemsSelected);
    }
}
