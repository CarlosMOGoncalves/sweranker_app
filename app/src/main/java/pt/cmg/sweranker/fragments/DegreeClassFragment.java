package pt.cmg.sweranker.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
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

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;
import pt.cmg.sweranker.ui.UnderlineDividerItemDecorator;

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

        TextView classDescription = (TextView) _myView.findViewById(R.id.class_description_text);
        classDescription.setText(getResources().getString(_degreeClass.getDescriptionResource()));

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


        RecyclerView curriculumList = (RecyclerView) _myView.findViewById(R.id.degree_program_list);

        DegreeClassAdapter adapter = new DegreeClassAdapter(this.getActivity(), _degreeClass);

        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false);
        curriculumList.setLayoutManager(linearLayoutManager);

        curriculumList.addItemDecoration(new ConstantSpacingItemDecorator(this.getActivity(), 2, ConstantSpacingItemDecorator.Side.BOTTOM));
        curriculumList.addItemDecoration(new UnderlineDividerItemDecorator.Builder(this.getActivity(),
                ContextCompat.getColor((Context) _parentActivity, R.color.darkerBackground),
                1)
                .targetViewHolderClass(DegreeClassAdapter.ClassTopicViewHolder.class)
                .build());

        curriculumList.setItemAnimator(new DefaultItemAnimator());
        curriculumList.setAdapter(adapter);

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

    /**
     * This Adapter transforms a list of Knowledge Areas in Views for the parent fragment recycler view.
     */
    private class DegreeClassAdapter extends RecyclerView.Adapter<DegreeClassAdapter.ClassTopicViewHolder> {

        private Context _context;
        private ArrayList<Integer> _topicDescriptions;


        public DegreeClassAdapter(Context context, DegreeClass degreeClass) {
            _context = context;
            _topicDescriptions = new ArrayList<>();

            Set<Map.Entry<String, Integer>> topics = degreeClass.getProgram().entrySet();
            topics.forEach(entry ->
                    _topicDescriptions.add(entry.getValue())
            );
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
