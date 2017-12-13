package pt.cmg.sweranker.ranking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;
import pt.cmg.sweranker.ui.UnderlineDividerItemDecorator;


/**
 * This Dialog class is used to show all the Degree Classes used by a given Degree Combination.
 * It is a simple Dialog that just shows the formatted classes as a list with a dismiss button.
 */
public class DegreeOverviewDialog extends DialogFragment {

    private List<DegreeClass> _combinationClasses;
    private List<AnnualClassCombination> _annualCombinations;

    private OnDegreeOverviewDialogFragmentListener _listener;

    public interface OnDegreeOverviewDialogFragmentListener {

        void loadSelectedScoreFragment(String scoreId);
    }


    public DegreeOverviewDialog() {
    }


    public static DegreeOverviewDialog newInstance(OnDegreeOverviewDialogFragmentListener listener, List<AnnualClassCombination> annualCombinations, List<DegreeClass> combinationClasses) {
        DegreeOverviewDialog fragment = new DegreeOverviewDialog();
        fragment._combinationClasses = combinationClasses;
        fragment._annualCombinations = annualCombinations;
        fragment._listener = listener;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        dialogBuilder.setView(createOverViewList())
                .setNegativeButton("Dismiss", (dialog, id) ->
                        DegreeOverviewDialog.this.getDialog().cancel()
                );

        return dialogBuilder.create();
    }


    /**
     * Looks like much, but it just creates a Recycler View List that will show all the degree classes selected.
     * That, however is work for the Adapter, so this is mostly styling.
     */
    private RecyclerView createOverViewList() {
        RecyclerView _overviewList = (RecyclerView) getActivity().getLayoutInflater().inflate(R.layout.score_chart_degree_overview_dialog, null);

        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        _overviewList.setLayoutManager(linearLayoutManager);

        _overviewList.addItemDecoration(new ConstantSpacingItemDecorator(getActivity(), 2, ConstantSpacingItemDecorator.Side.BOTTOM));
        _overviewList.addItemDecoration(new UnderlineDividerItemDecorator.Builder(getActivity(),
                ContextCompat.getColor(getActivity(), R.color.darkerBackground),
                1)
                .targetViewHolderClass(DegreeOverviewAdapter.DegreeClassViewHolder.class)
                .build());
        _overviewList.setItemAnimator(new DefaultItemAnimator());

        DegreeOverviewAdapter adapter = new DegreeOverviewAdapter(this, _annualCombinations, _combinationClasses);
        _overviewList.setAdapter(adapter);

        return _overviewList;
    }

    /**
     * This adapter is used to modulate the data that will feed a RecyclerView list with both degree classes and
     * the years they belong to. It is slightly more complicated due to fact that this number of both the years
     * and the degree classes can change.
     * <p>
     * Since this adapter is very crucial to navigation and uses multiple view types that can change with the
     * amount of data fed to it, it requires some more work on building an underlying (I just love this word...)
     * abstraction to represent a simpler way to iterate over it.
     */
    public class DegreeOverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_DEGREE_CLASS_YEAR = 10;
        private static final int TYPE_DEGREE_CLASS_ITEM = 20;

        private DegreeOverviewDialog _parentDialog;
        private Context _context;

        /**
         * Keys -> the index (zero-based) of the Degree Class in the adapter position: Values -> the actual Degree Class
         */
        private Map<Integer, DegreeClass> _degreeClassesPerAdapterPosition;

        /**
         * Keys -> the index (zero-based) of the Annual Class Combination in the adapter position: Values -> the actual Annual Class Combination
         */
        private Map<Integer, AnnualClassCombination> _annualCombinationPerAdapterPosition;


        private DegreeOverviewAdapter(DegreeOverviewDialog dialog, List<AnnualClassCombination> annualCombinations, List<DegreeClass> degreeClasses) {
            _parentDialog = dialog;
            _context = dialog.getActivity();
            _degreeClassesPerAdapterPosition = new TreeMap<>();
            _annualCombinationPerAdapterPosition = new TreeMap<>();
            buildAdapterPositionStructures(annualCombinations, degreeClasses);
        }


        /**
         * This is the very core of this mess. This will fill both index pointer variables on one sweep.
         * By iterating over the pre-built data structures for the effect it will build a complete index based
         * solution that will place the YEARs and CLASSES in their right place.
         */
        private void buildAdapterPositionStructures(List<AnnualClassCombination> annualClassCombinations, List<DegreeClass> degreeClasses) {
            Map<Integer, AnnualClassCombination> combinationsPerYear = createAnnualCombinationPerYearView(annualClassCombinations);
            Map<Integer, List<DegreeClass>> classesPerYear = calculateClassesPerYearView(degreeClasses);

            int adapterPosition = 0;
            for (AnnualClassCombination annualCombo : combinationsPerYear.values()) {

                _annualCombinationPerAdapterPosition.put(adapterPosition, annualCombo);
                adapterPosition++;

                for (DegreeClass degreeClass : classesPerYear.get(annualCombo.getYear())) {
                    _degreeClassesPerAdapterPosition.put(adapterPosition, degreeClass);
                    adapterPosition++;
                }
            }
        }

        /**
         * Creates a data view where Keys are the Years of a combination and the Values are the matching
         * combinations.
         * They ARE ORDERED. This is guaranteed by the TreeMap implementation.
         * If you change this, all Hell will break loose.
         */
        private Map<Integer, AnnualClassCombination> createAnnualCombinationPerYearView(List<AnnualClassCombination> annualClassCombinations) {
            Map<Integer, AnnualClassCombination> annualCombosPerYear = new TreeMap<>();

            for (AnnualClassCombination annualClassCombination : annualClassCombinations) {
                annualCombosPerYear.put(annualClassCombination.getYear(), annualClassCombination);
            }
            return annualCombosPerYear;
        }

        /**
         * Creates a data view where Keys are the Years of a DegreeClass and the Values are list of all the
         * DegreeClasses of that year.
         * They ARE ORDERED. This is guaranteed by the TreeMap implementation.
         * If you change this, all Hell will break loose.
         */
        private Map<Integer, List<DegreeClass>> calculateClassesPerYearView(List<DegreeClass> degreeClasses) {

            Map<Integer, List<DegreeClass>> classesPerYear = new TreeMap<>();

            for (DegreeClass degreeClass : degreeClasses) {
                // If its bigger, tough luck, I won't accept it.
                if (!classesPerYear.containsKey(degreeClass.getYear())) {
                    classesPerYear.put(degreeClass.getYear(), new ArrayList<>());
                }
                classesPerYear.get(degreeClass.getYear()).add(degreeClass);
            }

            return classesPerYear;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView;
            if (viewType == TYPE_DEGREE_CLASS_YEAR) {
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.score_chart_degree_overview_dialog_list_item_year, parent, false);
                return new DegreeYearViewHolder(itemView);
            }

            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.score_chart_degree_overview_dialog_list_item_degree_class, parent, false);
            return new DegreeClassViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof DegreeYearViewHolder) {
                ((DegreeYearViewHolder) holder)._year.setText(_context.getResources().getString(R.string.year) + " " + _annualCombinationPerAdapterPosition.get(position).getYear());
            } else {
                ((DegreeClassViewHolder) holder)._degreeClassName.setText(_context.getResources().getString(_degreeClassesPerAdapterPosition.get(position).getNameResource()));
            }
        }

        @Override
        public int getItemCount() {
            return _annualCombinationPerAdapterPosition.size() + _degreeClassesPerAdapterPosition.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (_annualCombinationPerAdapterPosition.containsKey(position)) {
                return TYPE_DEGREE_CLASS_YEAR;
            }
            return TYPE_DEGREE_CLASS_ITEM;
        }


        /**
         * ViewHolder pattern to hold a degree year text view
         */
        private class DegreeYearViewHolder extends RecyclerView.ViewHolder {
            private TextView _year;

            private DegreeYearViewHolder(View view) {
                super(view);
                _year = view.findViewById(R.id.year_label);
                view.setOnClickListener(v -> {
                    int adapterPosition = getAdapterPosition();
                    _parentDialog.dismiss();
                    _listener.loadSelectedScoreFragment(_annualCombinationPerAdapterPosition.get(adapterPosition).getId());
                });
            }
        }

        /**
         * ViewHolder pattern to hold a degree class text view
         */
        private class DegreeClassViewHolder extends RecyclerView.ViewHolder {

            private TextView _degreeClassName;

            private DegreeClassViewHolder(View view) {
                super(view);
                _degreeClassName = view.findViewById(R.id.class_name);

                view.setOnClickListener(v -> {
                    int adapterPosition = getAdapterPosition();
                    _parentDialog.dismiss();
                    _listener.loadSelectedScoreFragment(_degreeClassesPerAdapterPosition.get(adapterPosition).getId());
                });
            }


        }
    }

}
