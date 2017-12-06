package pt.cmg.sweranker.swebok;

import android.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
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

import pt.cmg.sweranker.MainActivity;
import pt.cmg.sweranker.MainActivityViewModel;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;
import pt.cmg.sweranker.ui.UnderlineDividerItemDecorator;

/**
 * Created by Carlos on 12/01/2017.
 */

public class SwebokDetailedFragment extends Fragment {

    private MainActivityViewModel _viewModel;


    private RecyclerView _topicList;
    private View _myView;

    private KnowledgeArea _knowledgeArea;


    public SwebokDetailedFragment() {
    }

    public static SwebokDetailedFragment newInstance() {
        return new SwebokDetailedFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _viewModel = ViewModelProviders.of((MainActivity) this.getActivity()).get(MainActivityViewModel.class);
        _knowledgeArea = _viewModel.getSelectedKnowledgeArea();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _myView = inflater.inflate(R.layout.knowledge_area_details_fragment, container, false);

        ImageView kaImage = _myView.findViewById(R.id.ka_details_image);
        TextView kaTitle = _myView.findViewById(R.id.ka_details_name);

        kaImage.setImageDrawable(this.getResources().getDrawable(_knowledgeArea.getImageResource(), null));
//        kaImage.setBackgroundColor(ContextCompat.getColor(getActivity(), _knowledgeArea.getColourResource()));
//        kaImage.setColorFilter(Color.parseColor("#ffffff"));
        kaImage.setBackgroundColor(Color.parseColor("#ffffff"));
        kaImage.setColorFilter(ContextCompat.getColor(getActivity(), _knowledgeArea.getColourResource()));
        kaTitle.setText(this.getResources().getText(_knowledgeArea.getNameResource()));

        _topicList = _myView.findViewById(R.id.ka_details_topics_list);


        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false);
        _topicList.setLayoutManager(linearLayoutManager);

        _topicList.addItemDecoration(new ConstantSpacingItemDecorator(this.getActivity(), 20, ConstantSpacingItemDecorator.Side.BOTTOM));
        _topicList.addItemDecoration(new UnderlineDividerItemDecorator.Builder(this.getActivity(), ContextCompat.getColor(getActivity(), R.color.darkerBackground), 1)
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

}
