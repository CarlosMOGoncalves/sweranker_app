package pt.cmg.sweranker.degrees;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.cmg.sweranker.R;

/**
 * Created by Carlos on 09/02/2017.
 */

public class DegreeProgramAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 10;
    private static final int TYPE_DEGREE_CLASS_ITEM = 20;

    private Context _context;
    private Degree _degree;
    private int _degreeId;
    private int[] _yearTitlePositions;
    private DegreeClass[] _degreeClasses;

    private DegreeViewPagerAdapter.OnDegreeClassItemSelected _onDegreeClassSelectedListener;


    public DegreeProgramAdapter(Context context, Degree degree, DegreeViewPagerAdapter.OnDegreeClassItemSelected listener) {

        _context = context;
        _degree = degree;
        _degreeId = degree.getId();

        _yearTitlePositions = getYearPositions();
        _degreeClasses = getDegreeClassesAsArray();

        _onDegreeClassSelectedListener = listener;
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
        for (Map.Entry<Integer, List<DegreeClass>> entry : _degree.getClasses().entrySet()) {
            counts.put(entry.getKey(), entry.getValue().size());
        }
//        _degree.getClasses().entrySet().forEach(entry -> counts.put(entry.getKey(), entry.getValue().size()));
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
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.degree_details_class_list_item_year, parent, false);
            return new DegreeYearViewHolder(itemView);
        }

        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.degree_details_class_list_item_class, parent, false);
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
    public class DegreeYearViewHolder extends RecyclerView.ViewHolder {
        private TextView _year;

        public DegreeYearViewHolder(View view) {
            super(view);
            _year = (TextView) view.findViewById(R.id.year_title);
        }
    }

    /**
     * ViewHolder pattern to hold one of the cards
     */
    public class DegreeClassViewHolder extends RecyclerView.ViewHolder {

        private TextView _degreeClassName;

        public DegreeClassViewHolder(View view) {
            super(view);
            _degreeClassName = (TextView) view.findViewById(R.id.class_name);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // VERY IMPORTANT - The delay is needed because the ripple effect was being triggered too late, as in not triggered at all.
                    v.postDelayed(() -> {

                        int adapterPosition = getAdapterPosition();

                        // AHA, I knew this array stuff would be useful <3
                        String degreeClassId = _degreeClasses[adapterPosition].getId();
                        _onDegreeClassSelectedListener.onDegreeClassClicked(_degreeId, degreeClassId);
                    }, 400);
                }
            });
        }


    }
}
