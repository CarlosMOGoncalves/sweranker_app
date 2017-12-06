package pt.cmg.sweranker.swebok;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pt.cmg.sweranker.R;

/**
 * Created by Carlos on 09/02/2017.
 */

public class KnowledgeAreasAdapter extends RecyclerView.Adapter<KnowledgeAreasAdapter.KAViewHolder> {

    private Context _context;
    private List<KnowledgeArea> _knowledgAreas;
    private OnKnowledgeAreaClicked _listener;


    /**
     * Implement this interface if you want to listen to any selected item on the degree class listener
     */
    public interface OnKnowledgeAreaClicked {
        void onKnowledgeAreaClicked(View v, KnowledgeArea knowledgeArea, int colour);
    }

    public KnowledgeAreasAdapter(Context context, List<KnowledgeArea> knowledgeAreas, OnKnowledgeAreaClicked listener) {
        _listener = listener;
        _context = context;
        _knowledgAreas = knowledgeAreas;

        // I remove the last one because it is an empty KA to use as black hole for anything unrelated
//        _knowledgAreas.remove(_knowledgAreas.size() - 1);
    }

    @Override
    public KAViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.knowledge_area_card, parent, false);
        return new KAViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(KAViewHolder holder, int position) {
        KnowledgeArea knowledgeArea = _knowledgAreas.get(position);
        holder._kaName.setText(_context.getResources().getString(knowledgeArea.getNameResource()));
        holder._kaTopicCount.setText(knowledgeArea.getTopicsCount() + " " + _context.getResources().getString(R.string.topics_lowercase));
        holder._kaImage.setImageDrawable(_context.getResources().getDrawable(knowledgeArea.getImageResource(), null));
//        holder._kaImage.setBackgroundColor(ContextCompat.getColor(_context, knowledgeArea.getColourResource()));
//        holder._kaImage.setColorFilter(Color.parseColor("#ffffff"));
        holder._kaImage.setBackgroundColor(Color.parseColor("#ffffff"));
        holder._kaImage.setColorFilter(ContextCompat.getColor(_context, knowledgeArea.getColourResource()));
        holder._kaImage.setTransitionName("ka_image" + position);
    }


    @Override
    public int getItemCount() {
        return _knowledgAreas.size() - 1;
    }


    /**
     * ViewHolder pattern to hold one of the cards
     */
    public class KAViewHolder extends RecyclerView.ViewHolder {

        private ImageView _kaImage;
        private TextView _kaName;
        private TextView _kaTopicCount;

        public KAViewHolder(View view) {
            super(view);
            _kaImage = (ImageView) view.findViewById(R.id.ka_image);
            _kaName = (TextView) view.findViewById(R.id.ka_name);
            _kaTopicCount = (TextView) view.findViewById(R.id.ka_topic_number);

            view.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                // VERY IMPORTANT - The delay is needed because the ripple effect was being triggered too late, as in not triggered at all.
                v.postDelayed(() -> _listener.onKnowledgeAreaClicked(v, _knowledgAreas.get(pos),
                        ContextCompat.getColor(_context, _knowledgAreas.get(pos).getColourResource())), 300);
            });
        }


    }
}
