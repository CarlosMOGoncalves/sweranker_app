package pt.cmg.sweranker.degrees;

import android.app.Activity;
import android.app.Fragment;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pt.cmg.sweranker.MainActivity;
import pt.cmg.sweranker.MainActivityViewModel;
import pt.cmg.sweranker.R;

/**
 * Created by Carlos on 25/01/2017.
 */

public class DegreeDetailsFragment extends Fragment implements LifecycleRegistryOwner {


    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    private View _myView;

    private int _degreeId;
    private Degree _degree;

    private MainActivityViewModel _sharedViewModel;

    private DegreeDetailsFragmentInteractionListener _parentActivity;


    public DegreeDetailsFragment() {
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DegreeDetailsFragment.
     */
    public static DegreeDetailsFragment newInstance() {
        return new DegreeDetailsFragment();
    }


    /**
     * Communication Interface used to communicate between this fragment and its parent Activity.
     * <p>
     * Write here any method needed to trigger in the Activity
     */
    public interface DegreeDetailsFragmentInteractionListener {
        /**
         * Loads the class fragment whose item was chosen.
         */
        void loadDegreeClassFragment(View selectedView);
    }

    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof DegreeDetailsFragmentInteractionListener) {
            _parentActivity = (DegreeDetailsFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement DegreeDetailsFragmentInteractionListener");
        }

    }

    @Override
    public void onAttach(Activity parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof DegreeDetailsFragmentInteractionListener) {
            _parentActivity = (DegreeDetailsFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement DegreeDetailsFragmentInteractionListener");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        _sharedViewModel = ViewModelProviders.of((MainActivity) getActivity()).get(MainActivityViewModel.class);
        _degree = _sharedViewModel.getSelectedDegree();
        _degreeId = _degree.getId();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _myView = inflater.inflate(R.layout.degree_details_fragment, container, false);

        ImageView degreeImage = _myView.findViewById(R.id.degree_image);
        TextView universityName = _myView.findViewById(R.id.university_name);
        TextView degreeName = _myView.findViewById(R.id.degree_name);
        TextView isDegreeEvaluated = _myView.findViewById(R.id.evaluated_status);

        degreeImage.setImageDrawable(this.getResources().getDrawable(_degree.getImageResource(), null));
        universityName.setText(this.getResources().getText(_degree.getUniversityResource()));
        degreeName.setText(this.getResources().getText(_degree.getFullNameResource()));

        if (_sharedViewModel.isDegreeMatched(_degreeId)) {
            isDegreeEvaluated.setText(R.string.matched);
            isDegreeEvaluated.setTextColor((this.getResources().getColor(R.color.materialAffirmative)));
        } else {
            isDegreeEvaluated.setText(R.string.notMatched);
            isDegreeEvaluated.setTextColor((this.getResources().getColor(R.color.materialNegative)));
        }

        ViewPager viewPager = _myView.findViewById(R.id.degree_viewPager);
        viewPager.setAdapter(new DegreeViewPagerAdapter(this.getActivity(), _degree, (view, degreeClass) -> {
            _sharedViewModel.setSelectedDegreeClass(degreeClass);
            _parentActivity.loadDegreeClassFragment(view);
        }));

        TabLayout tabLayout = _myView.findViewById(R.id.degree_tabs);
        tabLayout.setupWithViewPager(viewPager);

        return _myView;
    }


}
