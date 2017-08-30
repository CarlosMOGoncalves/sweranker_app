package pt.cmg.sweranker.degrees;

import android.app.Activity;
import android.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import pt.cmg.sweranker.MainActivity;
import pt.cmg.sweranker.MainActivityViewModel;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;
import pt.cmg.sweranker.ui.UnderlineDividerItemDecorator;

/**
 * This Fragment is used to show the details of a given Degree Class, namely its program and
 * some of its common attributes such as year, semester, etc.
 * <p>
 * More importantly, this is the entry point to the Degree Class Matching, where one can match
 * each program item to one or more Knowledge Area Topics. That however is only available in
 * development mode.
 */
public class DegreeClassFragment extends Fragment {

    private DegreeClassFragmentInteractionListener _parentActivity;


    private View _myView;

    private String _degreeClassId;
    private DegreeClass _degreeClass;
    private FloatingActionButton _fab;

    private MainActivityViewModel _sharedViewModel;


    /**
     * Communication Interface used to communicate between this fragment and its parent Activity.
     * <p>
     * Write here any method needed to trigger in the Activity
     */
    public interface DegreeClassFragmentInteractionListener {
        /**
         * Loads the Degree Evaluator fragment for this degreeClass.
         */
        void loadDegreeTopicMatcherFragment();
    }


    public DegreeClassFragment() {
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DegreeDetailsFragment.
     */
    public static DegreeClassFragment newInstance() {
        return new DegreeClassFragment();
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

    // NOTE: this is here because onAttach(Context) was added only on API 23, so as long as Lollipop is min sdk this shall be here
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DegreeClassFragmentInteractionListener) {
            _parentActivity = (DegreeClassFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement DegreeClassFragmentInteractionListener");
        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _sharedViewModel = ViewModelProviders.of((MainActivity) getActivity()).get(MainActivityViewModel.class);
        _degreeClass = _sharedViewModel.getSelectedDegreeClass();
        _degreeClassId = _degreeClass.getId();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _myView = inflater.inflate(R.layout.degree_class_fragment, container, false);

        TextView classYear = (TextView) _myView.findViewById(R.id.year);
        classYear.setText(String.valueOf(_degreeClass.getYear()));
        TextView classSemester = (TextView) _myView.findViewById(R.id.semester);
        classSemester.setText(String.valueOf(_degreeClass.getSemester()));
        TextView classECTS = (TextView) _myView.findViewById(R.id.ects);
        classECTS.setText(String.valueOf(_degreeClass.getEctsCredits()));
        TextView classOptional = (TextView) _myView.findViewById(R.id.optional);

        if (_degreeClass.isOptionalClass()) {
            classOptional.setText(getString(R.string.yes));
            classOptional.setTextColor(ContextCompat.getColor(getActivity(), R.color.materialAffirmative));
        } else {
            classOptional.setText(getString(R.string.no));
            classOptional.setTextColor(ContextCompat.getColor(getActivity(), R.color.materialNegative));
        }

        TextView areMatchesAvailable = (TextView) _myView.findViewById(R.id.is_matched);


        if (_sharedViewModel.hasMatches(_degreeClassId)) {
            areMatchesAvailable.setText("(" + getString(R.string.matched) + ")");
            areMatchesAvailable.setTextColor(ContextCompat.getColor(getActivity(), R.color.materialAffirmative));
        } else {
            areMatchesAvailable.setText("(" + getString(R.string.notMatched) + ")");
            areMatchesAvailable.setTextColor(ContextCompat.getColor(getActivity(), R.color.materialNegative));
        }


        RecyclerView curriculumList = (RecyclerView) _myView.findViewById(R.id.degree_program_list);

        DegreeClassAdapter adapter = new DegreeClassAdapter(this.getActivity(), _degreeClass);

        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        curriculumList.setLayoutManager(linearLayoutManager);

        curriculumList.addItemDecoration(new ConstantSpacingItemDecorator(getActivity(), 2, ConstantSpacingItemDecorator.Side.BOTTOM));
        curriculumList.addItemDecoration(new UnderlineDividerItemDecorator.Builder(this.getActivity(),
                ContextCompat.getColor(getActivity(), R.color.darkerBackground),
                1)
                .targetViewHolderClass(DegreeClassAdapter.ClassTopicViewHolder.class)
                .build());

        curriculumList.setItemAnimator(new DefaultItemAnimator());
        curriculumList.setAdapter(adapter);

        _fab = (FloatingActionButton) _myView.findViewById(R.id.evaluateButton);
        _fab.setOnClickListener(view -> _parentActivity.loadDegreeTopicMatcherFragment());

        return _myView;
    }


    /**
     * This Adapter is used to show the topics of each DegreeClass in a list.
     */
    private class DegreeClassAdapter extends RecyclerView.Adapter<DegreeClassAdapter.ClassTopicViewHolder> {

        private Context _context;
        private ArrayList<Integer> _topicDescriptions;


        public DegreeClassAdapter(Context context, DegreeClass degreeClass) {
            _context = context;
            _topicDescriptions = new ArrayList<>();

            Set<Map.Entry<String, Integer>> topics = degreeClass.getProgram().entrySet();
            for (Map.Entry<String, Integer> topic : topics) {
                _topicDescriptions.add(topic.getValue());
            }
//            topics.forEach(entry ->
//                    _topicDescriptions.add(entry.getValue())
//            );
        }

        @Override
        public ClassTopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.degree_class_topic, parent, false);
            return new ClassTopicViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(ClassTopicViewHolder holder, int position) {
            holder._classTopic.setText(_context.getResources().getString(_topicDescriptions.get(position)));
        }


        @Override
        public int getItemCount() {
            return _topicDescriptions.size();
        }


        /**
         * ViewHolder pattern to hold one of the cards
         */
        class ClassTopicViewHolder extends RecyclerView.ViewHolder {

            private TextView _classTopic;

            public ClassTopicViewHolder(View view) {
                super(view);
                _classTopic = (TextView) view.findViewById(R.id.degree_class_topic);
            }


        }
    }

}
