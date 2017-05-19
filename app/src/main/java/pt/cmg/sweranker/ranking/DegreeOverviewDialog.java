package pt.cmg.sweranker.ranking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

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

    public DegreeOverviewDialog() {
    }


    public static DegreeOverviewDialog newInstance(List<DegreeClass> combinationClasses) {
        DegreeOverviewDialog fragment = new DegreeOverviewDialog();
        fragment._combinationClasses = combinationClasses;
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

        DegreeOverviewAdapter adapter = new DegreeOverviewAdapter(getActivity(), _combinationClasses);
        _overviewList.setAdapter(adapter);

        return _overviewList;
    }
}
