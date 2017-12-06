package pt.cmg.sweranker.swebok;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pt.cmg.sweranker.R;

/**
 * Created by Carlos on 09/02/2017.
 * <p>
 * This adapter is used to fill the Recycler View of this fragment that shows de details of the Knowledge
 * Area.
 * <p>
 * Unlike simpler adapters this one has an header type View. Refer to this code to understand how to use
 * the adapter with more than one ViewHolder.
 */
public class KnowledgeAreaDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 10;
    private static final int TYPE_TOPICS_TITLE = 15;
    private static final int TYPE_ITEM = 20;

    /**
     * Extra views at the top, namely the TYPE_HEADER and TYPE_TOPICS_TITLE
     */
    private static final int EXTRA_VIEWS = 2;

    private RecyclerView _parentView;
    private KnowledgeArea _knowledgeArea;
    private Context _context;


    public KnowledgeAreaDetailAdapter(Context context, KnowledgeArea knowledgeArea, RecyclerView parentView) {
        _parentView = parentView;
        _context = context;
        _knowledgeArea = knowledgeArea;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == TYPE_HEADER) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.knowledge_area_details_description, parent, false);
            return new KADetailsViewHolder(itemView);
        } else if (viewType == TYPE_TOPICS_TITLE) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ka_details_topics_text, parent, false);
            return new KATopicsTitleViewHolder(itemView);
        }

        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.knowledge_area_details_topic_item, parent, false);
        return new KATopicsViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof KADetailsViewHolder) {
            ((KADetailsViewHolder) holder)._kaDescription.setText(_context.getResources().getString(_knowledgeArea.getDescriptionResource()));
        } else if (holder instanceof KATopicsTitleViewHolder) {
            // nothing to bind really
        } else {
            KnowledgeAreaTopic kaTopic = _knowledgeArea.getTopics().get(position - EXTRA_VIEWS);
            ((KATopicsViewHolder) holder)._topicName.setText(_context.getResources().getString(kaTopic.getNameResource()));
            ((KATopicsViewHolder) holder)._topicDescritpion.setText(_context.getResources().getString(kaTopic.getDescriptionResource()));
        }
    }


    @Override
    public int getItemCount() {
        return _knowledgeArea.getTopicsCount() + EXTRA_VIEWS;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) { // it is the header
            return TYPE_HEADER;
        }
        if (position == 1) { // it is just the textview of the topics
            return TYPE_TOPICS_TITLE;
        }
        return TYPE_ITEM;
    }


    class KADetailsViewHolder extends RecyclerView.ViewHolder {

        private TextView _kaDescription;

        public KADetailsViewHolder(View view) {
            super(view);
            _kaDescription = (TextView) view.findViewById(R.id.ka_details_description_text);
        }
    }

    // Really just a marker class to be able to inflate the textview
    class KATopicsTitleViewHolder extends RecyclerView.ViewHolder {

        public KATopicsTitleViewHolder(View view) {
            super(view);
        }
    }

    /**
     * ViewHolder pattern to hold each KA topic and its description
     */
    public class KATopicsViewHolder extends RecyclerView.ViewHolder {

        private TextView _topicName;
        private TextView _topicDescritpion;

        public KATopicsViewHolder(View view) {
            super(view);
            _topicName = (TextView) view.findViewById(R.id.topic_name);
            _topicDescritpion = (TextView) view.findViewById(R.id.topic_description);

            //This listener is used to set the visibility of the topic description, it is GONE by
            //default
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TransitionManager.beginDelayedTransition(_parentView, new Fade());
                    boolean isGone = _topicDescritpion.getVisibility() == View.GONE;
                    _topicDescritpion.setVisibility(isGone ? View.VISIBLE : View.GONE);
                }
            });
        }

    }
}