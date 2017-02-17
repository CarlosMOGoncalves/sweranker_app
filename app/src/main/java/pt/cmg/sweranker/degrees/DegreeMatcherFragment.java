package pt.cmg.sweranker.degrees;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.swebok.KnowledgeAreaLoader;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;

/**
 * The DegreeMatcherFragment is the class with the business logic to match each topic of a
 * Degree class with one or more topics of one or more KAs.
 */
public class DegreeMatcherFragment extends Fragment {

    private static final String DEGREE_CLASS_ID = "DEGREE_CLASS_ID";

    private OnDegreeMatcherFragmentInteraction _parentActivity;

    private String _degreeClassId;
    private DegreeClass _degreeClass;

    private View _myView;

    public DegreeMatcherFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DegreeDetailsFragment.
     */
    public static DegreeMatcherFragment newInstance(String degreeClassId) {
        DegreeMatcherFragment fragment = new DegreeMatcherFragment();
        Bundle args = new Bundle();
        args.putString(DEGREE_CLASS_ID, degreeClassId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof OnDegreeMatcherFragmentInteraction) {
            _parentActivity = (OnDegreeMatcherFragmentInteraction) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement DegreeClassFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            _degreeClassId = getArguments().getString(DEGREE_CLASS_ID);
            _degreeClass = _parentActivity.loadDegreeClass(_degreeClassId);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _myView = inflater.inflate(R.layout.degree_matcher_fragment, container, false);

        RecyclerView matcherList = (RecyclerView) _myView.findViewById(R.id.matcher_list);
        DegreeTopicMatcherAdapter adapter = new DegreeTopicMatcherAdapter(getActivity(), _degreeClass, _parentActivity.getKnowledgeAreas(), new DegreeTopicMatcherAdapter.OnDegreeTopicMatcherListener() {

        });
        matcherList.setAdapter(adapter);

        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false);
        matcherList.setLayoutManager(linearLayoutManager);

        matcherList.addItemDecoration(new ConstantSpacingItemDecorator(this.getActivity(), 2, ConstantSpacingItemDecorator.Side.BOTTOM));
        matcherList.setItemAnimator(new DefaultItemAnimator());


        return _myView;
    }


    /**
     * Communication Interface used to communicate between this fragment and its parent Activity.
     * <p>
     * Write here any method needed to trigger in the Activity
     */
    public interface OnDegreeMatcherFragmentInteraction extends DegreeLoader, KnowledgeAreaLoader {

    }
}
