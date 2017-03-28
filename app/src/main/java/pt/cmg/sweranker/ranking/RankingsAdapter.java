package pt.cmg.sweranker.ranking;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import pt.cmg.sweranker.R;

/**
 * Created by Carlos on 28/03/2017.
 */

public class RankingsAdapter extends RecyclerView.Adapter<RankingsAdapter.RankingViewHolder> {

    private Context _context;
    private List<String> _data;


    public RankingsAdapter(Context context, List<String> data) {
        _context = context;
        _data = data;
    }


    @Override
    public RankingsAdapter.RankingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ranking_card, parent, false);
        return new RankingViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RankingsAdapter.RankingViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return _data.size();
    }


    public class RankingViewHolder extends RecyclerView.ViewHolder {

        private ImageView _degreeLogo;
        private TextView _rankingName;

        public RankingViewHolder(View itemView) {
            super(itemView);

            _degreeLogo = (ImageView) itemView.findViewById(R.id.ranking_logo);
            _rankingName = (TextView) itemView.findViewById(R.id.ranking_name);

            _degreeLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(_context, _rankingName.getText(), Toast.LENGTH_SHORT);
                }
            });

        }
    }
}
