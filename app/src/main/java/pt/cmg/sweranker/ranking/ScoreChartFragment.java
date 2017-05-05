package pt.cmg.sweranker.ranking;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
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

    /**
     * This array stores the colour mapping to each KA. Each KA has its own colour that identifies it graphically.
     * As it turns out, it is also useful for charting. As each KA has an integer ID each position of this array
     * stores the colour of the the KA whose id = index + 1 (because arrays are zero-based...)
     */
    private int[] _knowledgeAreaColours;


    private KnowledgeArea[] _knowledgeAreas;


    private View _myRootView;
    private ImageButton _overviewButton;

    private GridLayout _legendTable;

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

        _legendTable = (GridLayout) _myRootView.findViewById(R.id.chart_legend_table);

        _kaPercentDistributionChart = (PieChart) _myRootView.findViewById(R.id.ka_distribution_chart);
        _kaPercentDistributionChart.setVisibility(View.INVISIBLE);

        _topKaProgressBar = (ProgressBar) _myRootView.findViewById(R.id.top_kas_chart_progress);
        _topKaProgressBar.setVisibility(View.VISIBLE);

        return _myRootView;
    }


    private void updateChartLegend() {

        _legendTable.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        _legendTable.setColumnCount(2);
        _legendTable.setRowCount(8);

        Drawable squareIcon = ContextCompat.getDrawable(getActivity(), R.drawable.square);

        for (int i = 0, column = 0, row = 0; i < _knowledgeAreas.length; i++, column++) {

            if (column == 2) {
                column = 0;
                row++;
            }

            TextView kaName = new TextView(getActivity());
            kaName.setText(getActivity().getApplicationContext().getString(_knowledgeAreas[i].getNameResource()));
            kaName.setTextSize(10.0f);
            kaName.setCompoundDrawables(calculateSideDrawable(squareIcon, kaName, _knowledgeAreaColours[i]), null, null, null);


            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = GridLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.rightMargin = 2;
            layoutParams.topMargin = 2;
            layoutParams.columnSpec = GridLayout.spec(column, 1f);
            layoutParams.rowSpec = GridLayout.spec(row, 0f);
            layoutParams.setGravity(Gravity.START);
            kaName.setLayoutParams(layoutParams);


            _legendTable.addView(kaName, i);
        }
    }


    /**
     * This function, which I basically got online just creates a shape size and colour based on the subtitle TextView size.
     * Tricky, but works wonders.
     *
     * @param shapeToDraw
     * @param subtitleView
     * @param colour
     * @return
     */
    private Drawable calculateSideDrawable(Drawable shapeToDraw, TextView subtitleView, int colour) {

        // Tricky, I am creating multiple drawables from the same one, so loading the same wouldn't do
        Drawable mySquare = shapeToDraw.getConstantState().newDrawable().mutate();

        // This part calculates an acceptable size for the shape based on a percentage over the TextView size
        int pixelDrawableSize = (int) Math.round(subtitleView.getLineHeight() * 0.9);
        mySquare.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize);

        // This just changes the colour to the specified one
        mySquare.setColorFilter(colour, PorterDuff.Mode.MULTIPLY);

        return mySquare;
    }

    /**
     * This function creates the data set to be displayed on the KA Distribution chart and styles it.
     * It follows closely the API of the MPAndroidChart to that end.
     */
    private void updateKADistributionChart() {

        float[] kaPercents = _currentScore.getKaPercents();

        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < _knowledgeAreas.length; i++) {
            // again the ids are zero-based but the damn KA ids are one-based to this trick is needed in the '_knowledgeAreas[i].getId() -1'
            entries.add(new PieEntry(kaPercents[_knowledgeAreas[i].getId() - 1], new String(getActivity().getApplicationContext().getString(_knowledgeAreas[i].getNameResource()))));
        }

        PieDataSet dataSet = new PieDataSet(entries, "KA Percentiles");
        dataSet.setValueTextSize(10f);
        dataSet.setColors(_knowledgeAreaColours);
        dataSet.setValueFormatter(new PercentFormatter());

        PieData pieChartData = new PieData(dataSet);
        _kaPercentDistributionChart.setData(pieChartData);

        // Both description and subtitle will be displayed apart so I am disabling them both
        _kaPercentDistributionChart.getDescription().setEnabled(false);
        _kaPercentDistributionChart.getLegend().setEnabled(false);
        // Also labels, in a smartphone they would likely be useless. I will try to find another way to display it.
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

            loadKnowledgeAreasVariables();
            loadSelectedScore();
            return null;
        }


        /**
         * Creates two useful views on the KnowledgeAreas data that will be used throughout this fragment.
         */
        private void loadKnowledgeAreasVariables() {

            List<KnowledgeArea> kas = _parentActivity.getKnowledgeAreas();

            _knowledgeAreaColours = new int[kas.size()];
            for (KnowledgeArea knowledgeArea : kas) {
                // index is zero-based but KAs are 1-based so... there it goes
                _knowledgeAreaColours[knowledgeArea.getId() - 1] = ContextCompat.getColor(getActivity().getApplicationContext(), knowledgeArea.getColourResource());
            }

            _knowledgeAreas = new KnowledgeArea[kas.size()];
            for (KnowledgeArea knowledgeArea : kas) {
                _knowledgeAreas[knowledgeArea.getId() - 1] = knowledgeArea;
            }
        }

        /**
         * Loads from the Realm database the selected score for this fragment and naturally closes the door afterwards
         */
        private void loadSelectedScore() {

            Realm database = Realm.getDefaultInstance();

            SweScore currentScore = database.where(SweScore.class)
                    .equalTo(SweScoreFields.SCORE_TYPE, SweScore.TYPE_DEGREE_SCORE)
                    .equalTo(SweScoreFields.ID, _degreeScoreId)
                    .findFirst();

            _currentScore = new SweScore(currentScore);

            database.close();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateKADistributionChart();
            updateChartLegend();
            _percentProgressBar.setVisibility(View.INVISIBLE);
            _kaPercentDistributionChart.setVisibility(View.VISIBLE);
        }
    }

    public interface OnScoreChartFragmentInteractionListener extends KnowledgeAreaLoader {
    }
}
