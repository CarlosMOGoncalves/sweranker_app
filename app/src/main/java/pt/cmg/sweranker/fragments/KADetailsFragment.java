package pt.cmg.sweranker.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
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
            _knowledgeArea = _parentActivity.onKaDetailsFragmentInteraction(_knowledgeAreaId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _myView = inflater.inflate(R.layout.ka_fragment, container, false);

        ImageView kaImage = (ImageView) _myView.findViewById(R.id.ka_details_image);
        TextView kaTitle = (TextView) _myView.findViewById(R.id.ka_details_name);
        TextView kaDescription = (TextView) _myView.findViewById(R.id.ka_details_description_text);

        kaImage.setImageDrawable(this.getResources().getDrawable(_knowledgeArea.getImageResource(), null));
        kaTitle.setText(this.getResources().getText(_knowledgeArea.getNameResource()));
        kaDescription.setText(this.getResources().getText(_knowledgeArea.getDescriptionResource()));

        _topicList = (RecyclerView) _myView.findViewById(R.id.ka_details_topics_list);

        KADetailsAdapter adapter = new KADetailsAdapter(this.getActivity(), _knowledgeArea);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false);
        _topicList.setLayoutManager(mLayoutManager);
        _topicList.setItemAnimator(new DefaultItemAnimator());
        _topicList.setAdapter(adapter);
        return _myView;
    }

    /**
     * Communication Interface
     */
    public interface OnKaDetailsFragmentInteractionListener {
        KnowledgeArea onKaDetailsFragmentInteraction(int knowledgeAreaIdToLoad);
    }

    public class KADetailsAdapter extends RecyclerView.Adapter<KADetailsAdapter.KADetailsViewHolder> {

        private KnowledgeArea _knowledgeArea;
        private Context _context;


        public KADetailsAdapter(Context context, KnowledgeArea knowledgeArea) {
            _context = context;
            _knowledgeArea = knowledgeArea;
        }

        @Override
        public KADetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ka_topics_item, parent, false);
            return new KADetailsViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(KADetailsViewHolder holder, int position) {
            KnowledgeAreaTopic kaTopic = _knowledgeArea.getTopics().get(position);
            holder._topicName.setText(_context.getResources().getString(kaTopic.getNameResource()));
            holder._topicDescritpion.setText(_context.getResources().getString(kaTopic.getDescriptionResource()));
        }


        @Override
        public int getItemCount() {
            return _knowledgeArea.getTopicsCount();
        }


        /**
         * ViewHolder pattern to hold one of the cards
         */
        public class KADetailsViewHolder extends RecyclerView.ViewHolder {

            private TextView _topicName;
            private TextView _topicDescritpion;

            public KADetailsViewHolder(View view) {
                super(view);
                _topicName = (TextView) view.findViewById(R.id.topic_name);
                _topicDescritpion = (TextView) view.findViewById(R.id.topic_description);
            }
        }
    }

}
