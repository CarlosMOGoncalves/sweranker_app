package pt.cmg.sweranker.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.knowledgeareas.KnowledgeArea;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;


public class SwebokFragment extends Fragment {


    /**
     * This is a reference to the parent activity that this fragment will be attached to on onAttach()
     * It is used to communicate with it.
     */
    private OnSwebokFragmentInteractionListener _parentActivity;


    private RecyclerView _swebokGrid;
    private View _myView;


    public SwebokFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SwebokFragment.
     */
    public static SwebokFragment newInstance() {
        SwebokFragment fragment = new SwebokFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof OnSwebokFragmentInteractionListener) {
            _parentActivity = (OnSwebokFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private class SlideUnderToolbar extends RecyclerView.OnScrollListener{


        private RecyclerView _grid;
        private LinearLayoutManager _layoutManager;
        private Toolbar _toolBar;

        private SlideUnderToolbar(RecyclerView grid , LinearLayoutManager manager , Toolbar toolbar){
            _layoutManager = manager;
            _toolBar = toolbar;
            _grid = grid;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            // we want the grid to scroll over the top of the toolbar but for the toolbar items
            // to be clickable when visible. To achieve this we play games with elevation. The
            // toolbar is laid out in front of the grid but when we scroll, we lower it's elevation
            // to allow the content to pass in front (and reset when scrolled to top of the grid)
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && _layoutManager.findFirstVisibleItemPosition() == 0
                    && _layoutManager.findViewByPosition(0).getTop() == _grid.getPaddingTop()
                    && _toolBar.getTranslationZ() != 0) {
                // at top, reset elevation
                _toolBar.setTranslationZ(0f);
            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING
                    && _toolBar.getTranslationZ() != -1f) {
                // grid scrolled, lower toolbar to allow content to pass in front
                _toolBar.setTranslationZ(-1f);
                _toolBar.setElevation(-1f);
            }
        }
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _myView = inflater.inflate(R.layout.fragment_swebok, container, false);

        _swebokGrid = (RecyclerView) _myView.findViewById(R.id.swebok_grid);

        KnowledgeAreaAdapter adapter = new KnowledgeAreaAdapter(this.getActivity(), _parentActivity.loadKnowledgeAreasForSwebokFragment());

        GridLayoutManager mLayoutManager = new GridLayoutManager(this.getActivity(), 2);
        _swebokGrid.setLayoutManager(mLayoutManager);
        _swebokGrid.addItemDecoration(new ConstantSpacingItemDecorator(this.getActivity(),
                10,
                ConstantSpacingItemDecorator.Side.LEFT,
                ConstantSpacingItemDecorator.Side.RIGHT,
                ConstantSpacingItemDecorator.Side.ALL_SIDES));
        _swebokGrid.setItemAnimator(new DefaultItemAnimator());
        _swebokGrid.setAdapter(adapter);
        return _myView;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        _parentActivity = null;
    }


    /**
     * Communication Interface
     */
    public interface OnSwebokFragmentInteractionListener {
        List<KnowledgeArea> loadKnowledgeAreasForSwebokFragment();

        void loadDetailedKnowledgeAreaFragment(View v, int knowledgeAreaId);
    }


    /**
     * This Adapter transforms a list of Knowledge Areas in Views for the parent fragment recycler view.
     */
    private class KnowledgeAreaAdapter extends RecyclerView.Adapter<KnowledgeAreaAdapter.KAViewHolder> {

        private Context _context;
        private List<KnowledgeArea> _knowledgAreas;


        public KnowledgeAreaAdapter(Context context, List<KnowledgeArea> knowledgeAreas) {
            _context = context;
            _knowledgAreas = knowledgeAreas;
        }

        @Override
        public KnowledgeAreaAdapter.KAViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.swebok_card, parent, false);
            return new KnowledgeAreaAdapter.KAViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(KnowledgeAreaAdapter.KAViewHolder holder, int position) {
            KnowledgeArea knowledgeArea = _knowledgAreas.get(position);
            holder._kaName.setText(_context.getResources().getString(knowledgeArea.getNameResource()));
            holder._kaTopicCount.setText(knowledgeArea.getTopicsCount() + " " + _context.getResources().getString(R.string.topics_lowercase));
            holder._kaImage.setImageDrawable(_context.getResources().getDrawable(knowledgeArea.getImageResource(), null));
            holder._kaImage.setBackgroundColor(ContextCompat.getColor(_context, knowledgeArea.getColourResource()));
            holder._kaImage.setColorFilter(Color.parseColor("#ffffff"));
            holder._kaImage.setTransitionName("ka_image" + position);
        }


        @Override
        public int getItemCount() {
            return _knowledgAreas.size();
        }


        /**
         * ViewHolder pattern to hold one of the cards
         */
        class KAViewHolder extends RecyclerView.ViewHolder {

            private ImageView _kaImage;
            private TextView _kaName;
            private TextView _kaTopicCount;

            public KAViewHolder(View view) {
                super(view);
                _kaImage = (ImageView) view.findViewById(R.id.ka_image);
                _kaName = (TextView) view.findViewById(R.id.ka_name);
                _kaTopicCount = (TextView) view.findViewById(R.id.ka_topic_number);

                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = getAdapterPosition();
                        _parentActivity.loadDetailedKnowledgeAreaFragment(v, _knowledgAreas.get(pos).getId());
                    }
                });
            }


        }
    }

}

