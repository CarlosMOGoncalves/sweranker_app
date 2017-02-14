package pt.cmg.sweranker.degrees;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pt.cmg.sweranker.R;

/**
 * The DegreeMatcherFragment is the class with the business logic to match each topic of a
 * Degree class with one or more topics of one or more KAs.
 */
public class DegreeMatcherFragment extends Fragment {

    private static final String DEGREE_CLASS_ID = "DEGREE_CLASS_ID";

    private DegreeMatcherFragmentInteractionListener _parentActivity;

    private String _degreeClassId;
    private DegreeClass _degreeClass;

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
        if (parentActivity instanceof DegreeMatcherFragmentInteractionListener) {
            _parentActivity = (DegreeMatcherFragmentInteractionListener) parentActivity;
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.degree_matcher_fragment, container, false);
    }


    /**
     * Communication Interface used to communicate between this fragment and its parent Activity.
     * <p>
     * Write here any method needed to trigger in the Activity
     */
    public interface DegreeMatcherFragmentInteractionListener {

        /**
         * Loads a Degree Class from the system.
         *
         * @param degreeClassId
         * @return
         */
        DegreeClass loadDegreeClass(String degreeClassId);

    }
}
