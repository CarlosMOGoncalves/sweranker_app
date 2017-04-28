package pt.cmg.sweranker.ranking;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

import pt.cmg.sweranker.R;

/**
 * Created by Carlos on 28/03/2017.
 */

public class ScoresAndImagesAdapter extends RecyclerView.Adapter<ScoresAndImagesAdapter.ScoreImageViewHolder> {

    private Context _context;
    private Map<String, Integer> _scoresAndImages;
    private String[] _indexes;


    public ScoresAndImagesAdapter(Context context, Map<String, Integer> scoresAndImages) {
        _context = context;
        _scoresAndImages = scoresAndImages;
        _indexes = _scoresAndImages.keySet().toArray(new String[_scoresAndImages.size()]);
    }


    @Override
    public ScoreImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ranking_card, parent, false);
        return new ScoreImageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ScoreImageViewHolder holder, int position) {
        holder._degreeLogo.setImageDrawable(_context.getDrawable(_scoresAndImages.get(_indexes[position])));
        holder._rankingName.setText(_indexes[position]);
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

            _degreeLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(_context, _rankingName.getText(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}
