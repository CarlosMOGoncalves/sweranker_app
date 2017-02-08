package pt.cmg.sweranker.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.degrees.DegreeClass;

public class DegreeClassFragment extends Fragment {

    private static final String DEGREE_ID = "DEGREE_ID";
    private static final String DEGREE_CLASS_ID = "DEGREE_CLASS_ID";

    private DegreeClassFragmentInteractionListener _parentActivity;


    private View _myView;

    private int _degreeId;
    private String _degreeClassId;
    private DegreeClass _degreeClass;


    public DegreeClassFragment() {
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DegreeDetailsFragment.
     */
    public static DegreeClassFragment newInstance(int degreeId, String degreeClassId) {
        DegreeClassFragment fragment = new DegreeClassFragment();
        Bundle args = new Bundle();
        args.putInt(DEGREE_ID, degreeId);
        args.putString(DEGREE_CLASS_ID, degreeClassId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof DegreeClassFragmentInteractionListener) {
            _parentActivity = (DegreeClassFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement DegreeClassFragmentInteractionListener");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            _degreeId = getArguments().getInt(DEGREE_ID);
            _degreeClassId = getArguments().getString(DEGREE_CLASS_ID);
            _degreeClass = _parentActivity.loadDegreeClass(_degreeId, _degreeClassId);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _myView = inflater.inflate(R.layout.degree_class_fragment, container, false);


        return _myView;
    }


    /**
     * Communication Interface used to communicate between this fragment and its parent Activity.
     * <p>
     * Write here any method needed to trigger in the Activity
     */
    public interface DegreeClassFragmentInteractionListener {

        DegreeClass loadDegreeClass(int degreeId, String degreeClassId);

    }


}
