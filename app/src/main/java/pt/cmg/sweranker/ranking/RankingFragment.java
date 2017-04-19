package pt.cmg.sweranker.ranking;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import pt.cmg.sweranker.R;

public class RankingFragment extends Fragment {

    public static final String ACTION_RECEIVER = "pt.cmg.sweranker.CALCULATION_FINISHED";

    private BroadcastReceiver _calculationsFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _progressBar.setVisibility(View.GONE);
            _rankingsGrid.setVisibility(View.VISIBLE);

            _adapter = new RankingsAdapter(context, null);
            _adapter.notifyDataSetChanged();
        }
    };


    /**
     * This is a reference to the parent activity that this fragment will be attached to on onAttach()
     * It is used to communicate with it.
     */
    private RankingFragmentInteractionListener _parentActivity;

    private RecyclerView _rankingsGrid;
    private View _myRootView;
    private ProgressBar _progressBar;
    private RankingsAdapter _adapter;
    private List<SweScore> _sampleScores;


    public RankingFragment() {
        // Required empty public constructor
    }


    public static RankingFragment newInstance() {
        RankingFragment fragment = new RankingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _sampleScores = loadSampleScores();
    }


    private List<SweScore> loadSampleScores() {

        Realm database = Realm.getDefaultInstance();

        // Starting with them ordered descending from KA = 3
        RealmResults<KAPercent> sortedPercents = database.where(KAPercent.class)
                .equalTo("kaId", 3)
                .findAllSorted("kaPercent", Sort.DESCENDING);

        RealmResults<SweScore> scores = database.where(SweScore.class)
                .equalTo("scoreType", SweScore.TYPE_DEGREE_SCORE)
                .findAll();

        RealmResults<SweScore> ascores = database.where(SweScore.class)
                .equalTo("scoreType", SweScore.TYPE_ANNUAL_SCORE)
                .findAll();

        RealmResults<SweScore> dscores = database.where(SweScore.class)
                .findAll();

        List<SweScore> resultingScores = new ArrayList<>();

        int counter = 0;

        Iterator<KAPercent> iterator = sortedPercents.iterator();
        for (int i = 0; i < 40; i++) {
            resultingScores.add(sortedPercents.get(i).getScore());
            counter++;
        }

        database.close();
        return resultingScores;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        _myRootView = inflater.inflate(R.layout.ranking_fragment, container, false);

        _progressBar = (ProgressBar) _myRootView.findViewById(R.id.progress_bar);

        _rankingsGrid = (RecyclerView) _myRootView.findViewById(R.id.rankings_grid);

        _adapter = new RankingsAdapter(this.getActivity(), null);

//        GridLayoutManager mLayoutManager = new GridLayoutManager(this.getActivity(), 4);
//        _rankingsGrid.setLayoutManager(mLayoutManager);
//        _rankingsGrid.addItemDecoration(new ConstantSpacingItemDecorator(this.getActivity(),
//                10,
//                ConstantSpacingItemDecorator.Side.LEFT,
//                ConstantSpacingItemDecorator.Side.RIGHT,
//                ConstantSpacingItemDecorator.Side.ALL_SIDES));
//        _rankingsGrid.setItemAnimator(new DefaultItemAnimator());
//        _rankingsGrid.setAdapter(_adapter);


        return _myRootView;
    }


    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof RankingFragmentInteractionListener) {
            _parentActivity = (RankingFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement RankingFragmentInteractionListener");
        }

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this.getActivity());
        IntentFilter intentFilter = new IntentFilter(ACTION_RECEIVER);
        broadcastManager.registerReceiver(_calculationsFinishedReceiver, intentFilter);

    }

    // NOTE: this is here because onAttach(Context) was added only on API 23, so as long as Lollipop is min sdk this shall be here
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof RankingFragmentInteractionListener) {
            _parentActivity = (RankingFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement RankingFragmentInteractionListener");
        }

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this.getActivity());
        IntentFilter intentFilter = new IntentFilter(ACTION_RECEIVER);
        broadcastManager.registerReceiver(_calculationsFinishedReceiver, intentFilter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _parentActivity = null;
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(_calculationsFinishedReceiver);
    }

    public interface RankingFragmentInteractionListener extends RankingLoader {


    }
}
