package pt.cmg.sweranker.ranking;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.Sort;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.degrees.Degree;
import pt.cmg.sweranker.degrees.DegreeLoader;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;

public class RankingFragment extends Fragment {

    public static final String ACTION_RECEIVER = "pt.cmg.sweranker.CALCULATION_FINISHED";

    private BroadcastReceiver _calculationsFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _progressBar.setVisibility(View.GONE);
            _rankingsGrid.setVisibility(View.VISIBLE);

            _adapter = new ScoresAndImagesAdapter(context, null);
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
    private ScoresAndImagesAdapter _adapter;

    private Map<Integer, Degree> _degrees;
    private Realm _database;
    private List<SweScore> _sampleScores;
    private LinkedHashMap<String, Integer> _combinationNameAndImage;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<Degree> degrees = _parentActivity.loadDegrees();
        _degrees = new LinkedHashMap<>();
        for (Degree d : degrees) {
            _degrees.put(d.getId(), d);
        }

        _database = Realm.getDefaultInstance();
        _sampleScores = _database.where(SweScore.class)
                .equalTo(SweScoreFields.SCORE_TYPE, SweScore.TYPE_DEGREE_SCORE)
                .equalTo(SweScoreFields.DEGREE_ID, 1)
                .findAllSorted(SweScoreFields.KA_PERCENT1, Sort.DESCENDING);

        _combinationNameAndImage = new LinkedHashMap<>();
        for (int i = 0; i < 10; i++) {
            SweScore currentScore = _sampleScores.get(i);
            _combinationNameAndImage.put(new String(currentScore.getId()), _degrees.get((int) currentScore.getDegreeId()).getImageResource());
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        _myRootView = inflater.inflate(R.layout.ranking_fragment, container, false);

        _progressBar = (ProgressBar) _myRootView.findViewById(R.id.progress_bar);
        _progressBar.setVisibility(View.INVISIBLE);

        _rankingsGrid = (RecyclerView) _myRootView.findViewById(R.id.rankings_grid);

        _adapter = new ScoresAndImagesAdapter(this.getActivity(), _combinationNameAndImage);

        GridLayoutManager mLayoutManager = new GridLayoutManager(this.getActivity(), 4);
        _rankingsGrid.setLayoutManager(mLayoutManager);
        _rankingsGrid.addItemDecoration(new ConstantSpacingItemDecorator(this.getActivity(),
                2,
                ConstantSpacingItemDecorator.Side.LEFT,
                ConstantSpacingItemDecorator.Side.RIGHT,
                ConstantSpacingItemDecorator.Side.ALL_SIDES));
        _rankingsGrid.setItemAnimator(new DefaultItemAnimator());
        _rankingsGrid.setAdapter(_adapter);


        return _myRootView;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        _database.close();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _parentActivity = null;
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(_calculationsFinishedReceiver);
    }

    public interface RankingFragmentInteractionListener extends DegreeLoader {


    }
}
