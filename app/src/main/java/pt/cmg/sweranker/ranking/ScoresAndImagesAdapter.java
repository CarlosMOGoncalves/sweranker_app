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

public class ScoresAndImagesAdapter extends RecyclerView.Adapter<ScoresAndImagesAdapter.ScoreImageViewHolder> {

    private Context _context;

    // Keys -> The degree score Id , Values -> the integer that is the resourced of this degree image
    private Map<String, Integer> _scoresAndImages;

    // These indexes are just another view on the above Map, it is merely the degree score id in an array to use the indexes for positions
    private String[] _degreeCombinationIds;
    private OnScoresGridAdapterListener _listener;


    public ScoresAndImagesAdapter(Context context, Map<String, Integer> scoresAndImages, OnScoresGridAdapterListener listener) {
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
    }

    @Override
    public int getItemCount() {
        return _scoresAndImages.size();
    }


    public class ScoreImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView _degreeLogo;
        private TextView _rankingName;

        public ScoreImageViewHolder(View itemView) {
            super(itemView);

            _degreeLogo = (ImageView) itemView.findViewById(R.id.ranking_logo);
            _rankingName = (TextView) itemView.findViewById(R.id.ranking_name);

            _degreeLogo.setOnClickListener(view -> _listener.loadDegreeChartsFragment(view, _degreeCombinationIds[getAdapterPosition()]));

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
    }
}
