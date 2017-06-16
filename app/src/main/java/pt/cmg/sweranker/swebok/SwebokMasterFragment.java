package pt.cmg.sweranker.swebok;

import android.app.Activity;
import android.app.Fragment;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProviders;
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
import android.widget.ProgressBar;

import java.util.List;

import pt.cmg.sweranker.MainActivity;
import pt.cmg.sweranker.MainActivityViewModel;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;


public class SwebokMasterFragment extends Fragment implements LifecycleRegistryOwner {


    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    /**
     * This is a reference to the parent activity that this fragment will be attached to on onAttach()
     * It is used to communicate with it.
     */
    private OnSwebokFragmentInteractionListener _parentActivity;


    private ProgressBar _progressBar;
    private RecyclerView _swebokGrid;
    private View _myView;


    public SwebokMasterFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SwebokMasterFragment.
     */
    public static SwebokMasterFragment newInstance() {
        SwebokMasterFragment fragment = new SwebokMasterFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof OnSwebokFragmentInteractionListener) {
            _parentActivity = (OnSwebokFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement OnScoreChartFragmentInteractionListener");
        }
    }

    // NOTE: this is here because onAttach(Context) was added only on API 23, so as long as Lollipop is min sdk this shall be here
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnSwebokFragmentInteractionListener) {
            _parentActivity = (OnSwebokFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnScoreChartFragmentInteractionListener");
        }
    }


    private MainActivityViewModel _viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _viewModel = ViewModelProviders.of((MainActivity) this.getActivity()).get(MainActivityViewModel.class);

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
        _progressBar = (ProgressBar) _myView.findViewById(R.id.swebok_progress_bar);
        _progressBar.setVisibility(View.VISIBLE);
        _swebokGrid = (RecyclerView) _myView.findViewById(R.id.swebok_grid);
        _swebokGrid.setVisibility(View.INVISIBLE);

        if (!_viewModel.getKnowledgeAreas().getValue().isEmpty()) {
            fillSwebokGrid(_viewModel.getKnowledgeAreas().getValue());
        }
        _viewModel.getKnowledgeAreas().observe(this, knowledgeAreas -> fillSwebokGrid(knowledgeAreas));


        return _myView;
    }


    /**
     * Creates the Swebok grid that presents each Knowledge Area to the user.
     *
     * @param knowledgeAreas The list of Knowledge Areas that will be adapted to the presentation grid.
     */
    private void fillSwebokGrid(List<KnowledgeArea> knowledgeAreas) {

        KnowledgeAreasAdapter adapter = new KnowledgeAreasAdapter(this.getActivity(), knowledgeAreas, (cardClicked, knowledgeArea) -> {
            _viewModel.setSelectedKnowledgeArea(knowledgeArea);
            _parentActivity.loadDetailedKnowledgeAreaFragment(cardClicked, knowledgeArea);
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

        _progressBar.setVisibility(View.INVISIBLE);
        _swebokGrid.setVisibility(View.VISIBLE);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        _parentActivity = null;
    }


    /**
     * Communication Interface used to communicate with parent activity
     */
    public interface OnSwebokFragmentInteractionListener extends KnowledgeAreaLoader {


        /**
         * Loads the Detailed Knowledge Area fragment.
         * It passes the View so that any shared elements transitions can be applied to it.
         */
        void loadDetailedKnowledgeAreaFragment(View v, KnowledgeArea knowledgeArea);
    }

}

