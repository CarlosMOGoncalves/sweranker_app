package pt.cmg.sweranker.swebok;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;


public class SwebokKAsFragment extends Fragment {


    /**
     * This is a reference to the parent activity that this fragment will be attached to on onAttach()
     * It is used to communicate with it.
     */
    private OnSwebokFragmentInteractionListener _parentActivity;


    private RecyclerView _swebokGrid;
    private View _myView;


    public SwebokKAsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SwebokKAsFragment.
     */
    public static SwebokKAsFragment newInstance() {
        SwebokKAsFragment fragment = new SwebokKAsFragment();
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


    private class SlideUnderToolbar extends RecyclerView.OnScrollListener {


        private RecyclerView _grid;
        private LinearLayoutManager _layoutManager;
        private Toolbar _toolBar;

        private SlideUnderToolbar(RecyclerView grid, LinearLayoutManager manager, Toolbar toolbar) {
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
        _myView = inflater.inflate(R.layout.swebok_kas_grid_fragment, container, false);

        _swebokGrid = (RecyclerView) _myView.findViewById(R.id.swebok_grid);

        KnowledgeAreasAdapter adapter = new KnowledgeAreasAdapter(this.getActivity(), _parentActivity.loadKnowledgeAreasForSwebokFragment(), new KnowledgeAreasAdapter.OnKnowledgeAreaClicked() {
            @Override
            public void onKnowledgeAreaClicked(View cardClicked, int position) {
                _parentActivity.loadDetailedKnowledgeAreaFragment(cardClicked, position);
            }
        });

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
     * Communication Interface used to communicate with parent activity
     */
    public interface OnSwebokFragmentInteractionListener {
        List<KnowledgeArea> loadKnowledgeAreasForSwebokFragment();

        void loadDetailedKnowledgeAreaFragment(View v, int knowledgeAreaId);
    }

}

