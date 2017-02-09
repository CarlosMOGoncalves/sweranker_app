package pt.cmg.sweranker.swebok;

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

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;
import pt.cmg.sweranker.ui.UnderlineDividerItemDecorator;

/**
 * Created by Carlos on 12/01/2017.
 */

public class SwebokKADetailsFragment extends Fragment {

    private static final String KNOWLEDGE_AREA_ID = "ka_id";


    private RecyclerView _topicList;
    private View _myView;
    private OnKaDetailsFragmentInteractionListener _parentActivity;

    private int _knowledgeAreaId;
    private KnowledgeArea _knowledgeArea;


    public SwebokKADetailsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SwebokKAsFragment.
     */
    public static SwebokKADetailsFragment newInstance(int knowledgeAreaId) {
        SwebokKADetailsFragment fragment = new SwebokKADetailsFragment();
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


        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false);
        _topicList.setLayoutManager(linearLayoutManager);

        _topicList.addItemDecoration(new ConstantSpacingItemDecorator(this.getActivity(), 20, ConstantSpacingItemDecorator.Side.BOTTOM));
        _topicList.addItemDecoration(new UnderlineDividerItemDecorator.Builder(this.getActivity(), ContextCompat.getColor((Context) _parentActivity, R.color.darkerBackground), 1)
                .targetViewHolderClass(KnowledgeAreaDetailAdapter.KATopicsViewHolder.class)
                .skipViews(2)
                .rightInset(20)
                .leftInset(20)
                .build());

        _topicList.setItemAnimator(new DefaultItemAnimator());

        KnowledgeAreaDetailAdapter adapter = new KnowledgeAreaDetailAdapter(this.getActivity(), _knowledgeArea, _topicList);
        _topicList.setAdapter(adapter);
        return _myView;
    }


    /**
     * Communication Interface used to load a Knowledge Area from the Activity
     */
    public interface OnKaDetailsFragmentInteractionListener {
        KnowledgeArea getKnowledgeArea(int knowledgeAreaIdToLoad);
    }


}
