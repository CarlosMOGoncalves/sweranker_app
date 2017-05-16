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
 * Created by Carlos on 16/05/2017.
 */

public class DegreeOverviewDialog extends DialogFragment {

    private int _combinationId;
    private List<DegreeClass> _combinationClasses;

    public DegreeOverviewDialog() {
    }


    public static DegreeOverviewDialog newInstance(int combinationNumber, List<DegreeClass> combinationClasses) {
        DegreeOverviewDialog fragment = new DegreeOverviewDialog();
        fragment._combinationId = combinationNumber;
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
