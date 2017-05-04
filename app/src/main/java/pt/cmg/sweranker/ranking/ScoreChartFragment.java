package pt.cmg.sweranker.ranking;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaLoader;

public class ScoreChartFragment extends Fragment {

    private static final String SCORE_ID = "degreeScoreId";

    private String _degreeScoreId;
    private View _myRootView;
    private ImageButton _overviewButton;

    private ProgressBar _percentProgressBar;
    private PieChart _kaPercentDistributionChart;

    private ProgressBar _topKaProgressBar;

    private SweScore _currentScore;

    private OnScoreChartFragmentInteractionListener _parentActivity;

    public ScoreChartFragment() {
        // Required empty public constructor
    }

    public static ScoreChartFragment newInstance(String degreeScoreId) {
        ScoreChartFragment fragment = new ScoreChartFragment();
        Bundle args = new Bundle();
        args.putString(SCORE_ID, degreeScoreId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof OnScoreChartFragmentInteractionListener) {
            _parentActivity = (OnScoreChartFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement OnScoreChartFragmentInteractionListener");
        }
    }

    // NOTE: this is here because onAttach(Context) was added only on API 23, so as long as Lollipop is min sdk this shall be here
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnScoreChartFragmentInteractionListener) {
            _parentActivity = (OnScoreChartFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnScoreChartFragmentInteractionListener");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            _degreeScoreId = getArguments().getString(SCORE_ID);
            new SweScoreLoader().execute();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        _myRootView = inflater.inflate(R.layout.score_chart_fragment, container, false);
        _overviewButton = (ImageButton) _myRootView.findViewById(R.id.overview_button);
        _overviewButton.setOnClickListener(view -> Toast.makeText(getActivity(), "Allahu akbar", Toast.LENGTH_SHORT).show());

        _percentProgressBar = (ProgressBar) _myRootView.findViewById(R.id.percent_chart_progress);
        _percentProgressBar.setVisibility(View.VISIBLE);

        _kaPercentDistributionChart = (PieChart) _myRootView.findViewById(R.id.ka_distribution_chart);
        _kaPercentDistributionChart.setVisibility(View.INVISIBLE);

        _topKaProgressBar = (ProgressBar) _myRootView.findViewById(R.id.top_kas_chart_progress);
        _topKaProgressBar.setVisibility(View.VISIBLE);

        return _myRootView;
    }


    private void updateKADistributionChart() {

        float[] kaPercents = _currentScore.getKaPercents();

        List<PieEntry> entries = new ArrayList<>();
        List<KnowledgeArea> kas = _parentActivity.getKnowledgeAreas();
        int[] kaColours = new int[kas.size()];
        for (KnowledgeArea kA : kas) {
            // index is zero-based but KAs are 1-based so... there it goes
            entries.add(new PieEntry(kaPercents[kA.getId() - 1], new String(getActivity().getApplicationContext().getString(kA.getNameResource()))));

            kaColours[kA.getId() - 1] = ContextCompat.getColor(getActivity().getApplicationContext(), kA.getColourResource());
        }

        PieDataSet dataSet = new PieDataSet(entries, "KA Percentiles");
        dataSet.setColors(kaColours);
        dataSet.setValueFormatter(new PercentFormatter());

        PieData pieChartData = new PieData(dataSet);

        Description noDescription = new Description();
        noDescription.setText("");

        Legend chartLegend = _kaPercentDistributionChart.getLegend();
        chartLegend.setWordWrapEnabled(true);
        chartLegend.setMaxSizePercent(0.50f);
        chartLegend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        chartLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        chartLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);

        _kaPercentDistributionChart.setData(pieChartData);
        _kaPercentDistributionChart.setDescription(noDescription);
        _kaPercentDistributionChart.setDrawEntryLabels(false);
        _kaPercentDistributionChart.setHoleRadius(40f);
        _kaPercentDistributionChart.setTransparentCircleRadius(45f);


        _kaPercentDistributionChart.invalidate();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        _parentActivity = null;
    }


    private class SweScoreLoader extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... rien) {

            Realm database = Realm.getDefaultInstance();

            SweScore currentScore = database.where(SweScore.class)
                    .equalTo(SweScoreFields.SCORE_TYPE, SweScore.TYPE_DEGREE_SCORE)
                    .equalTo(SweScoreFields.ID, _degreeScoreId)
                    .findFirst();

            _currentScore = new SweScore(currentScore);

            database.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            _percentProgressBar.setVisibility(View.INVISIBLE);
            updateKADistributionChart();
            _kaPercentDistributionChart.setVisibility(View.VISIBLE);
        }
    }

    public interface OnScoreChartFragmentInteractionListener extends KnowledgeAreaLoader {
    }
}
