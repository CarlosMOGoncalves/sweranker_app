package pt.cmg.sweranker.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pt.cmg.sweranker.KnowledgeArea;
import pt.cmg.sweranker.KnowledgeAreaTopic;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;
import pt.cmg.sweranker.ui.UnderlineDividerItemDecorator;

/**
 * Created by Carlos on 12/01/2017.
 */

public class KADetailsFragment extends Fragment {

    private static final String KNOWLEDGE_AREA_ID = "ka_id";


    private RecyclerView _topicList;
    private View _myView;
    private OnKaDetailsFragmentInteractionListener _parentActivity;

    private int _knowledgeAreaId;
    private KnowledgeArea _knowledgeArea;


    public KADetailsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SwebokFragment.
     */
    public static KADetailsFragment newInstance(int knowledgeAreaId) {
        KADetailsFragment fragment = new KADetailsFragment();
        Bundle args = new Bundle();
        args.putInt(KNOWLEDGE_AREA_ID, knowledgeAreaId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof OnKaDetailsFragmentInteractionListener) {
            _parentActivity = (OnKaDetailsFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            _knowledgeAreaId = getArguments().getInt(KNOWLEDGE_AREA_ID);
            _knowledgeArea = _parentActivity.getKnowledgeArea(_knowledgeAreaId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _myView = inflater.inflate(R.layout.ka_fragment, container, false);

        ImageView kaImage = (ImageView) _myView.findViewById(R.id.ka_details_image);
        TextView kaTitle = (TextView) _myView.findViewById(R.id.ka_details_name);

        kaImage.setImageDrawable(this.getResources().getDrawable(_knowledgeArea.getImageResource(), null));
        kaImage.setBackgroundColor(ContextCompat.getColor((Context) _parentActivity, _knowledgeArea.getColourResource()));
        kaImage.setColorFilter(Color.parseColor("#ffffff"));
        kaTitle.setText(this.getResources().getText(_knowledgeArea.getNameResource()));

        _topicList = (RecyclerView) _myView.findViewById(R.id.ka_details_topics_list);


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false);
        _topicList.setLayoutManager(mLayoutManager);

        _topicList.addItemDecoration(new ConstantSpacingItemDecorator(this.getActivity(), 20, ConstantSpacingItemDecorator.Side.BOTTOM));
        _topicList.addItemDecoration(new UnderlineDividerItemDecorator.Builder(this.getActivity(), ContextCompat.getColor((Context) _parentActivity, R.color.darkerBackground), 1)
                .targetViewHolderClass(KADetailsAdapter.KATopicsViewHolder.class)
                .skipViews(2)
                .rightInset(20)
                .leftInset(20)
                .build());

        _topicList.setItemAnimator(new DefaultItemAnimator());

        KADetailsAdapter adapter = new KADetailsAdapter(this.getActivity(), _knowledgeArea);
        _topicList.setAdapter(adapter);
        return _myView;
    }


    /**
     * Communication Interface used to load a Knowledge Area from the Activity
     */
    public interface OnKaDetailsFragmentInteractionListener {
        KnowledgeArea getKnowledgeArea(int knowledgeAreaIdToLoad);
    }

    /**
     * This adapter is used to fill the Recycler View of this fragment that shows de details of the Knowledge
     * Area.
     * <p>
     * Unlike simpler adapters this one has an header type View. Refer to this code to understand how to use
     * the adapter with more than one ViewHolder.
     */
    private class KADetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 10;
        private static final int TYPE_TOPICS_TITLE = 15;
        private static final int TYPE_ITEM = 20;

        /**
         * Extra views at the top, namely the TYPE_HEADER and TYPE_TOPICS_TITLE
         */
        private static final int EXTRA_VIEWS = 2;

        private KnowledgeArea _knowledgeArea;
        private Context _context;


        public KADetailsAdapter(Context context, KnowledgeArea knowledgeArea) {
            _context = context;
            _knowledgeArea = knowledgeArea;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView;
            if (viewType == TYPE_HEADER) {
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ka_details_header, parent, false);
                return new KADetailsViewHolder(itemView);
            } else if (viewType == TYPE_TOPICS_TITLE) {
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ka_details_topics_text, parent, false);
                return new KATopicsTitleViewHolder(itemView);
            }

            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ka_topics_item, parent, false);
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
         * ViewHolder pattern to hold one of the cards
         */
        class KATopicsViewHolder extends RecyclerView.ViewHolder {

            private TextView _topicName;
            private TextView _topicDescritpion;

            public KATopicsViewHolder(View view) {
                super(view);
                _topicName = (TextView) view.findViewById(R.id.topic_name);
                _topicDescritpion = (TextView) view.findViewById(R.id.topic_description);

                //This listener is used to set the visibility of the topic description, it is GONE by default
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isGone = _topicDescritpion.getVisibility() == View.GONE;
                        _topicDescritpion.setVisibility(isGone ? View.VISIBLE : View.GONE);
                    }
                });
            }
        }
    }

}
