package pt.cmg.sweranker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class KnowledgeAreaAdapter extends RecyclerView.Adapter<KnowledgeAreaAdapter.KAViewHolder> {

    private Context _context;
    private List<KnowledgeArea> _knowledgAreas;


    public KnowledgeAreaAdapter(Context context, List<KnowledgeArea> knowledgeAreas) {
        _context = context;
        _knowledgAreas = knowledgeAreas;
    }

    @Override
    public KAViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.swebok_card, parent, false);
        return new KAViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(KAViewHolder holder, int position) {
        KnowledgeArea knowledgeArea = _knowledgAreas.get(position);
        holder._kaName.setText(knowledgeArea.getName());
        holder._kaTopicCount.setText(knowledgeArea.getTopicsCount() + " topics");
        holder._kaImage.setImageDrawable(_context.getResources().getDrawable(knowledgeArea.getImage(), null));
    }


    @Override
    public int getItemCount() {
        return _knowledgAreas.size();
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
        }
    }
}
