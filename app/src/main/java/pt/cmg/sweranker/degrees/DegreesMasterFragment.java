package pt.cmg.sweranker.degrees;

import android.app.Activity;
import android.app.Fragment;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.List;

import pt.cmg.sweranker.MainActivity;
import pt.cmg.sweranker.MainActivityViewModel;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;


public class DegreesMasterFragment extends Fragment implements LifecycleRegistryOwner {


    private LifecycleRegistry _lifecycle;

    @Override
    public LifecycleRegistry getLifecycle() {
        return _lifecycle;
    }

    /**
     * This is a reference to the parent activity that this fragment will be attached to on onAttach()
     * It is used to communicate with it.
     */
    private DegreesFragmentInteractionListener _parentActivity;

    private RecyclerView _degreesGrid;
    private View _myRootView;
    private ProgressBar _progressBar;
    private MainActivityViewModel _sharedViewModel;

    public DegreesMasterFragment() {
        _lifecycle = new LifecycleRegistry(this);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DegreesMasterFragment.
     */
    public static DegreesMasterFragment newInstance() {
        return new DegreesMasterFragment();
    }

    /**
     * Communication Interface
     */
    public interface DegreesFragmentInteractionListener {
        /**
         * Loads and replaces the current fragment with the detailed Degree fragment.
         *
         * @param v The selected view. This is used to create some shared elements transitions.
         */
        void loadDetailedDegreeFragment(View v);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _sharedViewModel = ViewModelProviders.of((MainActivity) this.getActivity()).get(MainActivityViewModel.class);
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
    }

    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof DegreesFragmentInteractionListener) {
            _parentActivity = (DegreesFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement OnScoreChartFragmentInteractionListener");
        }
    }

    // NOTE: this is here because onAttach(Context) was added only on API 23, so as long as Lollipop is min sdk this shall be here
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DegreesFragmentInteractionListener) {
            _parentActivity = (DegreesFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnScoreChartFragmentInteractionListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _myRootView = inflater.inflate(R.layout.degrees_grid_fragment, container, false);
        _progressBar = (ProgressBar) _myRootView.findViewById(R.id.degrees_progress_bar);
        _progressBar.setVisibility(View.VISIBLE);

        _degreesGrid = (RecyclerView) _myRootView.findViewById(R.id.degrees_grid);
        _degreesGrid.setVisibility(View.INVISIBLE);

        if (!_sharedViewModel.getDegrees().getValue().isEmpty()) {
            fillDegreesGrid(_sharedViewModel.getDegrees().getValue());
        }
        _sharedViewModel.getDegrees().observe(this, degrees -> fillDegreesGrid(degrees));


        return _myRootView;
    }

    private void fillDegreesGrid(List<Degree> allDegrees) {

        DegreeAdapter adapter = new DegreeAdapter(this.getActivity(),
                allDegrees,
                (rootView, degree) -> {
                    _sharedViewModel.setSelectedDegree(degree);
                    _parentActivity.loadDetailedDegreeFragment(rootView);
                });

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this.getActivity(), 2);
        _degreesGrid.setLayoutManager(mLayoutManager);
        _degreesGrid.addItemDecoration(new ConstantSpacingItemDecorator(this.getActivity(),
                10,
                ConstantSpacingItemDecorator.Side.LEFT,
                ConstantSpacingItemDecorator.Side.RIGHT,
                ConstantSpacingItemDecorator.Side.ALL_SIDES));
        _degreesGrid.setItemAnimator(new DefaultItemAnimator());
        _degreesGrid.setAdapter(adapter);

        _progressBar.setVisibility(View.INVISIBLE);
        _degreesGrid.setVisibility(View.VISIBLE);

    }

    @Override
    public void onStart() {
        super.onStart();
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START);

    }

    @Override
    public void onResume() {
        super.onResume();
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
    }

    @Override
    public void onPause() {
        super.onPause();
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
    }

    @Override
    public void onStop() {
        super.onStop();
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _parentActivity = null;
    }

}
