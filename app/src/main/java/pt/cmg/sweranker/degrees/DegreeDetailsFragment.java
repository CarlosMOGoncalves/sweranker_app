package pt.cmg.sweranker.degrees;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pt.cmg.sweranker.R;

/**
 * Created by Carlos on 25/01/2017.
 */

public class DegreeDetailsFragment extends Fragment {

    private static final String DEGREE_ID = "degree_id";

    private View _myView;

    private int _degreeId;
    private Degree _degree;

    private DegreeDetailsFragmentInteractionListener _parentActivity;

    public DegreeDetailsFragment() {

    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DegreeDetailsFragment.
     */
    public static DegreeDetailsFragment newInstance(int degreeId) {
        DegreeDetailsFragment fragment = new DegreeDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(DEGREE_ID, degreeId);
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Communication Interface used to communicate between this fragment and its parent Activity.
     * <p>
     * Write here any method needed to trigger in the Activity
     */
    public interface DegreeDetailsFragmentInteractionListener {

        /**
         * Loads a Degree passing its id.
         *
         * @param degreeId
         * @return
         */
        Degree getDegree(int degreeId);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            _degreeId = getArguments().getInt(DEGREE_ID);
            _degree = _parentActivity.getDegree(_degreeId);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _myView = inflater.inflate(R.layout.degree_details_fragment, container, false);

        ImageView degreeImage = (ImageView) _myView.findViewById(R.id.degree_image);
        TextView universityName = (TextView) _myView.findViewById(R.id.university_name);
        TextView degreeName = (TextView) _myView.findViewById(R.id.degree_name);

        degreeImage.setImageDrawable(this.getResources().getDrawable(_degree.getImageResource(), null));
        universityName.setText(this.getResources().getText(_degree.getUniversityResource()));
        degreeName.setText(this.getResources().getText(_degree.getFullNameResource()));

        ViewPager viewPager = (ViewPager) _myView.findViewById(R.id.degree_viewPager);
        viewPager.setAdapter(new DegreeViewPagerAdapter(this.getActivity(), _degree, new DegreeViewPagerAdapter.OnDegreeClassItemSelected() {
            @Override
            public void onDegreeClassClicked(int degreeId, String degreeClassId) {
                loadDegreeClassFragment(degreeId, degreeClassId);
            }
        }));

        TabLayout tabLayout = (TabLayout) _myView.findViewById(R.id.degree_tabs);
        tabLayout.setupWithViewPager(viewPager);

        return _myView;
    }


    private void loadDegreeClassFragment(int degreeId, String degreeClassId) {
        Fragment degreeClassFragment = DegreeClassFragment.newInstance(degreeId, degreeClassId);

        Transition contentTransition = new Slide(Gravity.LEFT);
        contentTransition.setDuration(500);
        degreeClassFragment.setEnterTransition(contentTransition);
        degreeClassFragment.setExitTransition(contentTransition);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content_area, degreeClassFragment, "DegreeClass")
                .addToBackStack(null)
                .commit();
    }

}
