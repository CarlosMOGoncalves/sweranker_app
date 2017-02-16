package pt.cmg.sweranker.degrees;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;


public class DegreesFragment extends Fragment {

    /**
     * This is a reference to the parent activity that this fragment will be attached to on onAttach()
     * It is used to communicate with it.
     */
    private DegreesFragmentInteractionListener _parentActivity;

    private RecyclerView _degreesGrid;
    private View _myRootView;


    public DegreesFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DegreesFragment.
     */
    public static DegreesFragment newInstance() {
        DegreesFragment fragment = new DegreesFragment();
        return fragment;
    }

    /**
     * Communication Interface
     */
    public interface DegreesFragmentInteractionListener extends DegreeLoader {

        /**
         * Loads all the Degrees from the system.
         *
         * @return
         */
//        List<Degree> loadDegrees();


        /**
         * Loads and replaces the current fragment with the detailed Degree fragment.
         *
         * @param v        The selected view. This is used to create some shared elements transitions.
         * @param degreeId
         */
        void loadDetailedDegreeFragment(View v, int degreeId);

    }


    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof DegreesFragmentInteractionListener) {
            _parentActivity = (DegreesFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement DegreesFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _myRootView = inflater.inflate(R.layout.degrees_grid_fragment, container, false);

        _degreesGrid = (RecyclerView) _myRootView.findViewById(R.id.degrees_grid);

        DegreeAdapter adapter = new DegreeAdapter(this.getActivity(), _parentActivity.loadDegrees(), new DegreeAdapter.OnDegreeAdapterListener() {

            @Override
            public void loadDetailedDegreeFragment(View rootView, int degreeId) {
                _parentActivity.loadDetailedDegreeFragment(rootView, degreeId);
            }
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

        return _myRootView;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        _parentActivity = null;
    }

}
