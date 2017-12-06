package pt.cmg.sweranker.ranking;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.degrees.DegreeClass;

/**
 * Created by Carlos on 12/05/2017.
 */

public class DegreeOverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DEGREE_CLASS_YEAR = 10;
    private static final int TYPE_DEGREE_CLASS_ITEM = 20;

    private Context _context;
    private List<DegreeClass> _degreeClassList;
    private int _numberOfYears;

    /**
     * This is an array with all the data transformed for the commodity of the adapter.
     * Every position is either a Degree Class or null, which represents a Year view.
     */
    private DegreeClass[] _degreeClasses;


    private int[] _yearTitlePositions;


    public DegreeOverviewAdapter(Context context, List<DegreeClass> degreeClasses) {

        _context = context;
        _numberOfYears = getNumberOfYears(degreeClasses);
        _degreeClassList = degreeClasses;
        _degreeClasses = getDegreeClassesAsArray();

    }


    /**
     * Just a quick calculation to get the number of years this degree has.
     *
     * @param degreeClasses
     * @return
     */
    private int getNumberOfYears(List<DegreeClass> degreeClasses) {
        Set<Integer> allYears = new TreeSet<>();
        for (DegreeClass degreeClass : degreeClasses) {
            allYears.add(degreeClass.getYear());
        }

        return allYears.size();
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
        Map<Integer, List<DegreeClass>> classesByYear = calculateClassesByYear();
        DegreeClass[] degreeClasses = new DegreeClass[_numberOfYears + _degreeClassList.size()];

        _yearTitlePositions = new int[_numberOfYears];
        _yearTitlePositions[0] = 0;
        // This will iterate over ALL of the available positions in the array that was allocated
        for (int i = 1, position = 1; i <= classesByYear.size(); i++) {

            for (DegreeClass degreeClass : classesByYear.get(i)) {
                degreeClasses[position++] = degreeClass;
            }
            if (i != classesByYear.size()) {
                _yearTitlePositions[i] = position;
                position++;
            }


        }

        return degreeClasses;
    }


    private Map<Integer, List<DegreeClass>> calculateClassesByYear() {
        Map<Integer, List<DegreeClass>> result = new TreeMap<>();
        for (DegreeClass degreeClass : _degreeClassList) {

            if (result.get(degreeClass.getYear()) == null) {
                result.put(degreeClass.getYear(), new ArrayList());
            }
            result.get(degreeClass.getYear()).add(degreeClass);
        }
        return result;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        if (viewType == TYPE_DEGREE_CLASS_YEAR) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.score_chart_degree_overview_dialog_list_item_year, parent, false);
            return new DegreeOverviewAdapter.DegreeYearViewHolder(itemView);
        }

        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.score_chart_degree_overview_dialog_list_item_degree_class, parent, false);
        return new DegreeOverviewAdapter.DegreeClassViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DegreeOverviewAdapter.DegreeYearViewHolder) {
            ((DegreeOverviewAdapter.DegreeYearViewHolder) holder)._year.setText(_context.getResources().getString(R.string.year) + " " + getYearOfPosition(position));
        } else {
            DegreeClass currentClass = _degreeClasses[position];
            ((DegreeOverviewAdapter.DegreeClassViewHolder) holder)._degreeClassName.setText(_context.getResources().getString(currentClass.getNameResource()));
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
        return _degreeClassList.size() + _numberOfYears;
    }

    @Override
    public int getItemViewType(int position) {
        if (Arrays.binarySearch(_yearTitlePositions, position) >= 0) {
            return TYPE_DEGREE_CLASS_YEAR;
        }
        return TYPE_DEGREE_CLASS_ITEM;
    }


    // Really just a marker class to be able to inflate the textview
    public class DegreeYearViewHolder extends RecyclerView.ViewHolder {
        private TextView _year;

        public DegreeYearViewHolder(View view) {
            super(view);
            _year = (TextView) view.findViewById(R.id.year_label);
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
        }


    }
}
