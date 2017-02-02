package pt.cmg.sweranker.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.degrees.Degree;
import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;
import pt.cmg.sweranker.ui.UnderlineDividerItemDecorator;

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
        viewPager.setAdapter(new CustomPagerAdapter(this.getActivity()));

        TabLayout tabLayout = (TabLayout) _myView.findViewById(R.id.degree_tabs);
        tabLayout.setupWithViewPager(viewPager);

        return _myView;
    }


    private class CustomPagerAdapter extends PagerAdapter {

        private Context _context;

        private CustomPagerAdapter(Context context) {
            _context = context;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            LayoutInflater inflater = LayoutInflater.from(_context);

            ViewGroup layout;
            if (position == 0) {
                layout = (ViewGroup) inflater.inflate(R.layout.degree_basic_info, collection, false);
                TextView degreeDescription = (TextView) layout.findViewById(R.id.degree_description);
                degreeDescription.setText(_context.getResources().getString(_degree.getDescriptionResource()));
                collection.addView(layout);
            } else {
                layout = (ViewGroup) inflater.inflate(R.layout.degree_program_list, collection, false);
                RecyclerView programList = (RecyclerView) layout.findViewById(R.id.program_list);
                initialiseProgramList(programList);
                collection.addView(layout);
            }

            return layout;
        }


        private void initialiseProgramList(RecyclerView degreeProgramList) {
            RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(_context, LinearLayoutManager.VERTICAL, false);
            degreeProgramList.setLayoutManager(linearLayoutManager);

            degreeProgramList.addItemDecoration(new ConstantSpacingItemDecorator(_context, 2, ConstantSpacingItemDecorator.Side.BOTTOM));
            degreeProgramList.addItemDecoration(new UnderlineDividerItemDecorator.Builder(_context,
                    ContextCompat.getColor((Context) _parentActivity, R.color.darkerBackground),
                    1)
                    .targetViewHolderClass(DegreeProgramAdapter.DegreeClassViewHolder.class)
                    .build());
            degreeProgramList.setItemAnimator(new DefaultItemAnimator());

            DegreeProgramAdapter adapter = new DegreeProgramAdapter();
            degreeProgramList.setAdapter(adapter);
        }


        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Info";
            }

            return "Program";
        }

        private class DegreeProgramAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            private static final int TYPE_HEADER = 10;
            private static final int TYPE_DEGREE_CLASS_ITEM = 20;

            private int[] _yearTitlePositions;
            private DegreeClass[] _degreeClasses;

            public DegreeProgramAdapter() {
                _yearTitlePositions = getYearPositions();
                _degreeClasses = getDegreeClassesAsArray();
            }

            /**
             * Returns an array where each position is the position where a year sits between all the classes view holders.
             * For example the array [0, 7, 10, 20, 30] means that on those positions in the recycler view I have to insert
             * a DegreeYearViewHolder because between them I have the actual classes.
             *
             * @return
             */
            private int[] getYearPositions() {
                int[] positions = new int[_degree.getYears()];

                Map<Integer, Integer> classCountByYear = getClassCountByYear();
                int counter = 1;
                positions[0] = 0;
                for (int i = 1; i < positions.length; i++) {
                    positions[i] = counter + classCountByYear.get(i);
                    counter += classCountByYear.get(i) + 1;
                }

                return positions;
            }

            /**
             * Returns a map where the keys are the year numbers of a degree and the values are the respective number of
             * classes that are possible in that year.
             * <p>
             * This is an auxiliary method to calculate the positions that are meant to be represented by a Year view holder.
             *
             * @return
             */
            private Map<Integer, Integer> getClassCountByYear() {
                Map<Integer, Integer> counts = new HashMap<>();
                _degree.getClasses().entrySet().forEach(entry -> counts.put(entry.getKey(), entry.getValue().size()));
                return counts;
            }


            /**
             * This one is very tricky.
             * Returns an array where each position is occupied by the DegreeClass that matches that same position in the adapter.
             * So there is an array with the number of classes plus the number of years where only the classes are actually filled.
             * Like this [ null , DegreeClass1, DC2, DC3 , null , DC4 , ...] where each null is actually an empty spot to sit the Year View.
             *
             * @return
             */
            private DegreeClass[] getDegreeClassesAsArray() {
                DegreeClass[] degreeClasses = new DegreeClass[getItemCount()];

                // This will iterate over ALL of the available positions in the array that was allocated
                for (int i = 1; i < degreeClasses.length; i++) {

                    // For every year in the degree (usually 5)
                    for (int degreeYear = 1; degreeYear <= _degree.getYears(); degreeYear++) {

                        // I get the classes list
                        List<DegreeClass> currentClasses = _degree.getClassesOfYear(degreeYear);

                        // And then iterate over them so I can finally fill the array.
                        for (int classIndex = 0; classIndex < currentClasses.size(); classIndex++) {
                            degreeClasses[i] = currentClasses.get(classIndex);
                            i++;
                        }

                        // account for the year view
                        i++;
                    }
                }

                return degreeClasses;
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView;

                if (viewType == TYPE_HEADER) {
                    itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.degree_program_list_item_year, parent, false);
                    return new DegreeYearViewHolder(itemView);
                }

                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.degree_program_list_item_class, parent, false);
                return new DegreeClassViewHolder(itemView);
            }


            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                if (holder instanceof DegreeYearViewHolder) {
                    ((DegreeYearViewHolder) holder)._year.setText(_context.getResources().getString(R.string.year) + " " + getYearOfPosition(position));
                } else {
                    DegreeClass currentClass = _degreeClasses[position];
                    ((DegreeClassViewHolder) holder)._degreeClassName.setText(_context.getResources().getString(currentClass.getNameResource()));
                }
            }


            /**
             * Returns the position that the year View occupies in the data set (i.e. the degrees)
             *
             * @param recyclerViewPosition
             * @return
             */
            private int getYearOfPosition(int recyclerViewPosition) {
                // Aha, magic here! The BinarySearch actually returns the position that the given element occupies in the array, so it
                // is used here not for finding out if it exists but rather to get its position. Neat.
                return Arrays.binarySearch(_yearTitlePositions, recyclerViewPosition) + 1;
            }

            @Override
            public int getItemCount() {
                // There are as many views as the number of classes PLUS x views for years
                return _degree.getClassesCount() + _degree.getYears();
            }

            @Override
            public int getItemViewType(int position) {
                if (Arrays.binarySearch(_yearTitlePositions, position) >= 0) {
                    return TYPE_HEADER;
                }
                return TYPE_DEGREE_CLASS_ITEM;
            }


            // Really just a marker class to be able to inflate the textview
            class DegreeYearViewHolder extends RecyclerView.ViewHolder {
                private TextView _year;

                public DegreeYearViewHolder(View view) {
                    super(view);
                    _year = (TextView) view.findViewById(R.id.year_title);
                }
            }

            /**
             * ViewHolder pattern to hold one of the cards
             */
            class DegreeClassViewHolder extends RecyclerView.ViewHolder {

                private TextView _degreeClassName;

                public DegreeClassViewHolder(View view) {
                    super(view);
                    _degreeClassName = (TextView) view.findViewById(R.id.class_name);

                    //This listener is used to set the visibility of the topic description, it is GONE by default
//                    view.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            TransitionManager.beginDelayedTransition(_topicList, new Fade());
//                            boolean isGone = _topicDescritpion.getVisibility() == View.GONE;
//                            _topicDescritpion.setVisibility(isGone ? View.VISIBLE : View.GONE);
//                        }
//                    });
                }
            }
        }

    }

}
