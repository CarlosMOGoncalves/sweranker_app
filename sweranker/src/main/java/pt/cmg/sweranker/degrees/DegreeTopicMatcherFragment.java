package pt.cmg.sweranker.degrees;


import android.app.Activity;
import android.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pt.cmg.sweranker.MainActivity;
import pt.cmg.sweranker.MainActivityViewModel;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;

/**
 * The DegreeTopicMatcherFragment is the class with the business logic to match each topic of a
 * Degree class with one or more topics of one or more KAs.
 */
public class DegreeTopicMatcherFragment extends Fragment {

    private String _degreeClassId;
    private DegreeClass _degreeClass;

    private View _myView;

    private MainActivityViewModel _sharedViewModel;
    private DegreeTopicMatcherFragmentInteractionListener _parentActivity;

    public interface DegreeTopicMatcherFragmentInteractionListener {

        void degreeMatchSaveSuccess();
    }

    public DegreeTopicMatcherFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DegreeDetailsFragment.
     */
    public static DegreeTopicMatcherFragment newInstance() {
        return new DegreeTopicMatcherFragment();
    }

    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
//        if (parentActivity instanceof DegreeTopicMatcherFragmentInteractionListener) {
//            _parentActivity = (DegreeTopicMatcherFragmentInteractionListener) parentActivity;
//        } else {
//            throw new RuntimeException(parentActivity.toString() + " must implement DegreeTopicMatcherFragmentInteractionListener");
//        }
    }

    // NOTE: this is here because onAttach(Context) was added only on API 23, so as long as Lollipop is min sdk this shall be here
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        if (activity instanceof DegreeTopicMatcherFragmentInteractionListener) {
//            _parentActivity = (DegreeTopicMatcherFragmentInteractionListener) activity;
//        } else {
//            throw new RuntimeException(activity.toString() + " must implement DegreeTopicMatcherFragmentInteractionListener");
//        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _sharedViewModel = ViewModelProviders.of((MainActivity) getActivity()).get(MainActivityViewModel.class);

        _degreeClassId = _sharedViewModel.getSelectedDegreeClass().getId();
        _degreeClass = _sharedViewModel.getSelectedDegreeClass();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _myView = inflater.inflate(R.layout.degree_matcher_fragment, container, false);

        RecyclerView matcherList = (RecyclerView) _myView.findViewById(R.id.matcher_list);

        if (_sharedViewModel.hasMatches(_degreeClassId)) {

            DegreeClassMatch previousMatch = _sharedViewModel.getDegreeClassMatch(_degreeClassId);
            DegreeTopicMatcherAdapter adapter = new DegreeTopicMatcherAdapter(getActivity(),
                    _degreeClass,
                    previousMatch,
                    _sharedViewModel.getKnowledgeAreas().getValue(),
                    selectedMatch -> {
                        if (_sharedViewModel.saveMatch(selectedMatch)) {
                            getFragmentManager().popBackStackImmediate();
                        }
                    });
            matcherList.setAdapter(adapter);

        } else {
            DegreeTopicMatcherAdapter adapter = new DegreeTopicMatcherAdapter(getActivity(),
                    _degreeClass,
                    _sharedViewModel.getKnowledgeAreas().getValue(),
                    selectedMatch -> {
                        if (_sharedViewModel.saveMatch(selectedMatch)) {
                            getFragmentManager().popBackStackImmediate();
                        }
                    });
            matcherList.setAdapter(adapter);
        }

        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false);
        matcherList.setLayoutManager(linearLayoutManager);

        matcherList.addItemDecoration(new ConstantSpacingItemDecorator(this.getActivity(), 2, ConstantSpacingItemDecorator.Side.ALL_SIDES));
        matcherList.setItemAnimator(new DefaultItemAnimator());


        return _myView;
    }


}
