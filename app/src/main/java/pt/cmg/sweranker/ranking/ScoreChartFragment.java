package pt.cmg.sweranker.ranking;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.realm.Realm;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.degrees.DegreeLoader;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaLoader;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;

/**
 * This massive Fragment is one of the most important ones, probably the most important.
 * This Fragment loads ONE score that was selected in the Master and display multiple graphical charts
 * with its score from different, yet complimentary, points of view.
 * <p>
 * With this, it is possible to finally understand the strengths and weaknesses of each degree combination,
 * the way they are structured, what to expect of them and their completeness in relation to SWEBOK.
 * In simple words, this is the graphical tool that is the point of this application.
 */
public class ScoreChartFragment extends Fragment {

    private static final int SUBTITLE_COLUMN_COUNT = 2;
    private static final int SUBTITLE_ROW_COUNT = 8;

    private static final String SCORE_ID = "degreeScoreId";

    private String _degreeScoreId;

    /**
     * This array stores the colour mapping to each KA. Each KA has its own colour that identifies it graphically.
     * As it turns out, it is also useful for charting. As each KA has an integer ID each position of this array
     * stores the colour of the the KA whose id = index + 1 (because arrays are zero-based...)
     */
    private int[] _knowledgeAreaColours;


    private KnowledgeArea[] _knowledgeAreas;
    private KnowledgeAreaTopic[] _knowledgeAreaTopics;


    private View _myRootView;

    private ImageView _degreeImage;
    private TextView _overviewDegreeName;
    private TextView _overviewUniversityName;
    private TextView _overviewCombinationName;
    private TextView _showOverview;
    private ProgressBar _overviewProgressBar;

    private GridLayout _legendTable;

    private ProgressBar _percentProgressBar;
    private PieChart _kaPercentDistributionChart;

    private ProgressBar _topKaProgressBar;
    private HorizontalBarChart _topKaChart;

    private ProgressBar _topcKaTopicsProgressBar;
    private HorizontalBarChart _topKaTopicsChart;

    private ProgressBar _coverageChartProgressBar;
    private RadarChart _coverageChart;

    private SweScore _degreeScore;
    private DegreeClassCombination _degreeCombination;

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


        _degreeImage = (ImageView) _myRootView.findViewById(R.id.degree_image);
        _overviewDegreeName = (TextView) _myRootView.findViewById(R.id.degree_overview_name);
        _overviewUniversityName = (TextView) _myRootView.findViewById(R.id.degree_overview_university);
        _overviewCombinationName = (TextView) _myRootView.findViewById(R.id.degree_overview_combo_name);
        _overviewProgressBar = (ProgressBar) _myRootView.findViewById(R.id.overview_progress);
        _showOverview = (TextView) _myRootView.findViewById(R.id.show_overview);


        _legendTable = (GridLayout) _myRootView.findViewById(R.id.chart_legend_table);

        _percentProgressBar = (ProgressBar) _myRootView.findViewById(R.id.percent_chart_progress);
        _percentProgressBar.setVisibility(View.VISIBLE);
        _kaPercentDistributionChart = (PieChart) _myRootView.findViewById(R.id.ka_distribution_chart);
        _kaPercentDistributionChart.setVisibility(View.INVISIBLE);

        _topKaProgressBar = (ProgressBar) _myRootView.findViewById(R.id.top_kas_chart_progress);
        _topKaProgressBar.setVisibility(View.VISIBLE);
        _topKaChart = (HorizontalBarChart) _myRootView.findViewById(R.id.top_kas_chart);

        _topcKaTopicsProgressBar = (ProgressBar) _myRootView.findViewById(R.id.top_ka_topics_chart_progress);
        _topcKaTopicsProgressBar.setVisibility(View.VISIBLE);
        _topKaTopicsChart = (HorizontalBarChart) _myRootView.findViewById(R.id.top_ka_topics_chart);

        _coverageChartProgressBar = (ProgressBar) _myRootView.findViewById(R.id.coverage_chart_progress);
        _coverageChartProgressBar.setVisibility(View.VISIBLE);
        _coverageChart = (RadarChart) _myRootView.findViewById(R.id.coverage_chart);

        return _myRootView;
    }

    private void updateDegreeOverViewInfo() {

        int combinationNumber = Integer.valueOf(_degreeScore.getId().substring(3, _degreeScore.getId().length()));

        _degreeImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), _parentActivity.getDegree(_degreeScore.getDegreeId()).getImageResource()));
        _overviewDegreeName.setText(getResources().getString(_parentActivity.getDegree(_degreeScore.getDegreeId()).getNameResource()));
        _overviewUniversityName.setText(getResources().getString(_parentActivity.getDegree(_degreeScore.getDegreeId()).getUniversityResource()));
        _overviewCombinationName.setText(String.format(getResources().getString(R.string.degree_overview_combination), combinationNumber));
        _showOverview.setOnClickListener(view ->
                DegreeOverviewDialog.newInstance(combinationNumber, getDegreeClasses()).show(getFragmentManager(), "")
        );

        _overviewProgressBar.setVisibility(View.INVISIBLE);
    }


    /**
     * This helper function simply loads from the parent activity ALL the classes that compos THIS particular
     * degree. This function is used to construct the parameters that will be sent to the DegreeOverviewDialog.
     *
     * @return
     */
    private List<DegreeClass> getDegreeClasses() {

        List<DegreeClass> degreeClasses = new ArrayList<>();

        for (AnnualClassCombination annualCombination : _degreeCombination.getAnnualClassCombinations()) {
            for (DegreeClassId degreeId : annualCombination.getDegreeClassIds()) {
                degreeClasses.add(_parentActivity.getDegreeClass(degreeId.getDegreeClassId()));
            }
        }

        return degreeClasses;
    }

    /**
     * Composes and styles the Coverage Radar Chart.
     * This Chart shows how much of each Knowledge Area was achieved at the end of a particular degree combination.
     * It a graphical Radar chart where each Area can be covered from 0% to 100%.
     * Naturally the more, the better, although a complete coverage of a certain Knowledge Area doesn't tell the whole picture.
     * You can have an Area covered at 100% but its topics were mentioned only once during the whole degree.
     * That's what the other charts are for.
     */
    private void updateCoverageChart() {

        Map<Integer, Integer> totalTopicsByKa = createTotalTopicByKaView();
        Map<Integer, Integer> coveredTopics = createCoveredTopicsByKaView();
        String[] kas = new String[15];
        for (int i = 0; i < 15; i++) {
            kas[i] = "KA" + _knowledgeAreas[i].getId();
        }

        float percentCovered;
        List<RadarEntry> entries = new ArrayList<>();
        for (int i = 0; i < totalTopicsByKa.size(); i++) {
            percentCovered = (float) coveredTopics.get(i + 1) / (float) totalTopicsByKa.get(i + 1) * 100;
            entries.add(new RadarEntry(percentCovered));
        }


        RadarDataSet dataSet = new RadarDataSet(entries, "KA Coverage");
        dataSet.setColor(getResources().getColor(R.color.radarChartMaterialGreenA400));
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.radarChartMaterialGreen50));

        // Isto é a cor das linhas que aparecem quando se carrega no chart
        //dataSet.setHighLightColor(getResources().getColor(R.color.cardColour3));
        // Não faço ideia destes aqui abaixo...
        //dataSet.setHighlightCircleStrokeColor(getResources().getColor(R.color.cardColour5));
        //dataSet.setHighlightCircleFillColor(getResources().getColor(R.color.cardColour7));

        RadarData radarData = new RadarData(dataSet);
        radarData.setValueTextColor(getResources().getColor(R.color.radarChartMaterialRed500));
        radarData.setValueFormatter(new RadarDataValuesFormatter());

        _coverageChart.getDescription().setEnabled(false);
        _coverageChart.getLegend().setEnabled(false);


        _coverageChart.getYAxis().setAxisMinimum(0f);
        _coverageChart.getYAxis().setAxisMaximum(90f);
        _coverageChart.getYAxis().setDrawLabels(true);
        _coverageChart.getYAxis().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        _coverageChart.getYAxis().setCenterAxisLabels(true);
        _coverageChart.getYAxis().setGranularityEnabled(true);
        _coverageChart.getYAxis().setGranularity(25f);

        _coverageChart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        _coverageChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(kas));
        _coverageChart.getXAxis().setTextColor(getResources().getColor(R.color.radarChartMaterialBrown600));

        _coverageChart.setData(radarData);
        _coverageChart.animateY(2000);

        _coverageChart.invalidate();

        _coverageChartProgressBar.setVisibility(View.INVISIBLE);
        _coverageChart.setVisibility(View.VISIBLE);
    }


    /**
     * Simple percent formatter that omits the 100% value because it messes the chart UI.
     */
    private class RadarDataValuesFormatter implements IValueFormatter {
        private DecimalFormat _formatter = new DecimalFormat("##0.00");

        @Override
        public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
            if (v == 100f) {
                return "";
            }
            return _formatter.format(v) + "%";
        }
    }

    /**
     * Returns a Map view of Knowledge Areas data where each pair represents the KA id and the number of topics it has.
     *
     * @return
     */
    private Map<Integer, Integer> createTotalTopicByKaView() {
        Map<Integer, Integer> result = new HashMap<>();

        for (int i = 0; i < _knowledgeAreas.length; i++) {

            // The last KA is irrelevant because it is the OTHER
            if (_knowledgeAreas[i].getId() == 16) {
                continue;
            }
            result.put(_knowledgeAreas[i].getId(), _knowledgeAreas[i].getTopics().size());
        }
        return result;
    }


    /**
     * Similar to the above function but this one counts the total amount of topics that
     * were actually covered by the degree. This will be useful to find a percent of coverage.
     *
     * @return
     */
    private Map<Integer, Integer> createCoveredTopicsByKaView() {
        Map<Integer, Integer> result = new HashMap<>();

        for (int i = 0; i < _knowledgeAreas.length; i++) {

            // The last KA is irrelevant because it is the OTHER
            if (_knowledgeAreas[i].getId() == 16) {
                continue;
            }
            result.put(_knowledgeAreas[i].getId(), 0);
        }

        short[] topicCounters = _degreeScore.getTopicCounters();
        for (int i = 0; i < topicCounters.length; i++) {

            // Last one gets out
            if (i == 101) {
                continue;
            }
            if (topicCounters[i] > 0) {
                int currentTotal = result.get(_knowledgeAreaTopics[i].getKnowledgeAreaId());
                result.put(_knowledgeAreaTopics[i].getKnowledgeAreaId(), ++currentTotal);
            }
        }

        return result;
    }


    /**
     * This initialises and draws a simple subtitle graphic layout that displays the colours and names of all KAs.
     */
    private void updateSubtitleChart() {

        _legendTable.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        _legendTable.setColumnCount(SUBTITLE_COLUMN_COUNT);
        _legendTable.setRowCount(SUBTITLE_ROW_COUNT);

        Drawable squareIcon = ContextCompat.getDrawable(getActivity(), R.drawable.square);

        for (int i = 0, column = 0, row = 0; i < _knowledgeAreas.length; i++, column++) {

            if (column == SUBTITLE_COLUMN_COUNT) {
                column = 0;
                row++;
            }

            TextView kaName = new TextView(getActivity());
            kaName.setText("KA" + _knowledgeAreas[i].getId() + "-" + getResources().getString(_knowledgeAreas[i].getNameResource()));
            kaName.setTextSize(getResources().getDimension(R.dimen.chart_subtitle));
            kaName.setCompoundDrawables(calculateSideDrawable(squareIcon, kaName, _knowledgeAreaColours[i]), null, null, null);

            // All of this here below is just needed to set the alignment of the subtitles. Thank you stackoverflow.
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
     * This function, which I got online, just creates a shape size and colour based on the subtitle TextView size.
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

        float[] kaPercents = _degreeScore.getKaPercents();

        // The Entries of this chart basically get all the values for each different "Entity" that we are measuring
        List<PieEntry> entries = new ArrayList<>();
        for (int knowledgeAreaIndex = 0; knowledgeAreaIndex < _knowledgeAreas.length; knowledgeAreaIndex++) {
            // Again the ids are zero-based but the damn KA ids are one-based to this trick is needed in the '_knowledgeAreas[i].getId() -1'
            // Also, I am passing an integer index object to the Entry so that I can use it later on the chart to write the KA name in the centre of the chart.
            PieEntry newEntry = new PieEntry(kaPercents[_knowledgeAreas[knowledgeAreaIndex].getId() - 1], knowledgeAreaIndex);
            newEntry.setLabel(getResources().getString(_knowledgeAreas[knowledgeAreaIndex].getNameResource()));

            entries.add(newEntry);
        }


        PieDataSet dataSet = new PieDataSet(entries, "KA Percentiles");
        dataSet.setValueTextSize(getResources().getDimension(R.dimen.chart_ka_distribution_values_text_size));
        dataSet.setColors(_knowledgeAreaColours);
        dataSet.setValueFormatter(new PercentFormatter());

        PieData pieChartData = new PieData(dataSet);
        _kaPercentDistributionChart.setData(pieChartData);

        // Both description and subtitle will be displayed apart so I am disabling them both
        _kaPercentDistributionChart.getDescription().setEnabled(false);
        _kaPercentDistributionChart.getLegend().setEnabled(false);
        // Also labels, in a smart phone they would likely be useless. I will try to find another way to display it.
        _kaPercentDistributionChart.setDrawEntryLabels(false);

        _kaPercentDistributionChart.setHoleRadius(40f);
        _kaPercentDistributionChart.setTransparentCircleRadius(45f);

        _kaPercentDistributionChart.animateX(1000);

        _kaPercentDistributionChart.invalidate();

        _kaPercentDistributionChart.setTouchEnabled(true);
        _kaPercentDistributionChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight highlight) {
                _kaPercentDistributionChart.setCenterText(((PieEntry) entry).getLabel());
                _kaPercentDistributionChart.setCenterTextColor(_knowledgeAreaColours[(Integer) entry.getData()]);
            }

            @Override
            public void onNothingSelected() {
                _kaPercentDistributionChart.setCenterText("");
            }
        });

        // Now hide the progress bar and show the chart in all its glory
        _percentProgressBar.setVisibility(View.INVISIBLE);
        _kaPercentDistributionChart.setVisibility(View.VISIBLE);
    }


    /**
     * Feeds data and draws an Horizontal Bar Chart with a Top X KAs
     */
    private void updateTopKaChart(@IntRange(from = 0, to = 15) int nummberOfKasToDisplay) {


        short[] kaCounters = _degreeScore.getKaCounters();

        // This part is tricky, I am getting a TreeMap to take advantage of the ordering it makes
        // So now I have a Map where keys are the actual values ORDERED and the values are the array indexes which are equivalent
        // to the KA ids minus 1. Dear future me, don't hate me, remember that this native array stuff was implemented for performance reasons
        // as well as simplicity because of the Realm database.
        TreeMap<Integer, Integer> countersAndKaIds = new TreeMap<>();
        for (int i = 0; i < kaCounters.length; i++) {
            countersAndKaIds.put((int) kaCounters[i], i);
        }

        // These two variables are meant to fill subtitles and colours in the chart
        String[] topKaNames = new String[nummberOfKasToDisplay];
        int[] topKaColours = new int[nummberOfKasToDisplay];

        // Now tricking intensifies. I am iterating in the reverse order (where the bigger numbers are).
        // Yes I could simply have implemented a comparator to order them in reverse when inserting in the TreeMap, there are millions of solutions
        Iterator<Integer> iterator = countersAndKaIds.descendingKeySet().iterator();
        int currentKeyWhichIsActuallyAValue;
        int currentKaId;
        List<BarEntry> entries = new ArrayList<>();

        // Now to fill these variables I am iterating from the TOP to the BOTTOM because in the chart greater values of XAxis are in the top
        // of the chart, which is what I wanted in the first place. Yes, it's ugly. Yes, it is very confusing. Yes, I hate myself for doing it like this.
        for (int i = nummberOfKasToDisplay - 1, j = 0; i >= 0; i--, j++) {
            currentKeyWhichIsActuallyAValue = iterator.next();
            currentKaId = countersAndKaIds.get(currentKeyWhichIsActuallyAValue);

            entries.add(new BarEntry((float) i, (float) currentKeyWhichIsActuallyAValue));
            topKaNames[i] = getResources().getString(_knowledgeAreas[currentKaId].getNameResource());

            // For some reason the colours are fed to the chart in the opposite indexing as the values, go figure...
            topKaColours[j] = ContextCompat.getColor(getActivity(), _knowledgeAreas[currentKaId].getColourResource());

        }


        BarDataSet dataSet = new BarDataSet(entries, "TopKA");
        dataSet.setValueFormatter(new KATopicValueFormatter());
        dataSet.setColors(topKaColours);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);


        _topKaChart.setDrawBorders(false);
        _topKaChart.getLegend().setEnabled(false);

        // This description part uses a formatted string that reads something along the lines of 'From a total of  X topics'
        Description chartDescription = new Description();
        chartDescription.setText(String.format(getResources().getString(R.string.bar_chart_description), _degreeScore.getTotalTopicCount()));
        chartDescription.setTextAlign(Paint.Align.RIGHT);
        chartDescription.setTextSize((int) getResources().getDimension(R.dimen.chart_top_ka_description_text_size));
        _topKaChart.setDescription(chartDescription);

        _topKaChart.setFitBars(true);

        // All the gibberish below simply deactivates most of the grid lines to get the effect I wanted
        _topKaChart.getXAxis().setEnabled(true);
        //_topKaChart.getXAxis().setDrawAxisLine(false);
        //_topKaChart.getXAxis().setDrawLabels(true);
        _topKaChart.getXAxis().setDrawAxisLine(false);
        _topKaChart.getXAxis().setDrawGridLines(false);
        // This puts the text to the LEFT of the chart
        _topKaChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        // And this part replaces the X values by actual labels
        _topKaChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(topKaNames));

        _topKaChart.getAxisLeft().setEnabled(false);
        //_topKaChart.getAxisLeft().setDrawAxisLine(false);
        _topKaChart.getAxisLeft().setDrawGridLines(false);
        _topKaChart.getAxisLeft().setDrawZeroLine(true);
        _topKaChart.getAxisLeft().setDrawLabels(false);
        _topKaChart.getAxisLeft().setAxisMinimum(0f);

        _topKaChart.getAxisRight().setEnabled(false);
        _topKaChart.getAxisRight().setDrawLabels(false);
        //_topKaChart.getAxisRight().setDrawZeroLine(true);
        _topKaChart.getAxisRight().setDrawGridLines(false);

        _topKaChart.animateY(2000);

        // This will disable zooming in the scale which pretty much destroys the chart
        _topKaChart.setScaleEnabled(false);

        _topKaChart.setData(data);

        _topKaChart.invalidate();


        _topKaProgressBar.setVisibility(View.INVISIBLE);
        _topKaChart.setVisibility(View.VISIBLE);
    }

    /**
     * Feeds data and draws an Horizontal Bar Chart with a Top X KAs
     */
    private void updateTopKaTopicsChart(@IntRange(from = 0, to = 102) int nummberOfTopicsToDisplay) {


        short[] topicCounter = _degreeScore.getTopicCounters();

        // This part is tricky, I am getting a TreeMap to take advantage of the ordering it makes
        // So now I have a Map where keys are the actual values ORDERED and the values are the array indexes which are equivalent
        // to the KA ids minus 1. Dear future me, don't hate me, remember that this native array stuff was implemented for performance reasons
        // as well as simplicity because of the Realm database.
        TreeMap<Integer, Integer> countersAndKaTopicIds = new TreeMap<>();
        for (int i = 0; i < topicCounter.length; i++) {
            countersAndKaTopicIds.put((int) topicCounter[i], i);
        }

        // These two variables are meant to fill subtitles and colours in the chart
        String[] topKaTopicNames = new String[nummberOfTopicsToDisplay];
        int[] topKaTopicColours = new int[nummberOfTopicsToDisplay];

        // Now tricking intensifies. I am iterating in the reverse order (where the bigger numbers are).
        // Yes I could simply have implemented a comparator to order them in reverse when inserting in the TreeMap, there are millions of solutions
        Iterator<Integer> iterator = countersAndKaTopicIds.descendingKeySet().iterator();
        int currentKeyWhichIsActuallyAValue;
        int currentKATopicId;
        List<BarEntry> entries = new ArrayList<>();

        // Now to fill these variables I am iterating from the TOP to the BOTTOM because in the chart greater values of XAxis are in the top
        // of the chart, which is what I wanted in the first place. Yes, it's ugly. Yes, it is very confusing. Yes, I hate myself for doing it like this.
        for (int i = nummberOfTopicsToDisplay - 1, j = 0; i >= 0; i--, j++) {
            currentKeyWhichIsActuallyAValue = iterator.next();
            currentKATopicId = countersAndKaTopicIds.get(currentKeyWhichIsActuallyAValue);

            entries.add(new BarEntry((float) i, (float) currentKeyWhichIsActuallyAValue));
            topKaTopicNames[i] = getResources().getString(_knowledgeAreaTopics[currentKATopicId].getNameResource());

            // For some reason the colours are fed to the chart in the opposite indexing as the values, go figure...
            topKaTopicColours[j] = ContextCompat.getColor(getActivity(), _knowledgeAreas[_knowledgeAreaTopics[currentKATopicId].getKnowledgeAreaId() - 1].getColourResource());

        }


        BarDataSet dataSet = new BarDataSet(entries, "TopKATopics");
        dataSet.setValueFormatter(new TopicValueFormatter());
        dataSet.setValueTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        dataSet.setColors(topKaTopicColours);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);


        _topKaTopicsChart.setDrawBorders(false);

        _topKaTopicsChart.getLegend().setEnabled(false);

        // This description part uses a formatted string that reads something along the lines of 'From a total of  X topics'
        Description chartDescription = new Description();
        chartDescription.setText(String.format(getResources().getString(R.string.bar_chart_description), _degreeScore.getTotalTopicCount()));
        chartDescription.setTextAlign(Paint.Align.RIGHT);
        chartDescription.setTextSize((int) getResources().getDimension(R.dimen.chart_top_ka_description_text_size));
        _topKaTopicsChart.setDescription(chartDescription);

        _topKaTopicsChart.setFitBars(true);

        _topKaTopicsChart.setDrawValueAboveBar(false);

        // All the gibberish below simply deactivates most of the grid lines to get the effect I wanted
        _topKaTopicsChart.getXAxis().setEnabled(true);
        //_topKaTopicsChart.getXAxis().setDrawAxisLine(false);
        //_topKaTopicsChart.getXAxis().setDrawLabels(true);
        _topKaTopicsChart.getXAxis().setDrawAxisLine(false);
        _topKaTopicsChart.getXAxis().setDrawGridLines(false);
        // This puts the text to the LEFT of the chart
        _topKaTopicsChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        // And this part replaces the X values by actual labels
        _topKaTopicsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(topKaTopicNames));
        _topKaTopicsChart.getXAxis().setLabelCount(topKaTopicNames.length);

        _topKaTopicsChart.getAxisLeft().setEnabled(false);
        //_topKaTopicsChart.getAxisLeft().setDrawAxisLine(false);
        _topKaTopicsChart.getAxisLeft().setDrawGridLines(false);
        _topKaTopicsChart.getAxisLeft().setDrawZeroLine(true);
        _topKaTopicsChart.getAxisLeft().setDrawLabels(false);
        _topKaTopicsChart.getAxisLeft().setAxisMinimum(0f);

        _topKaTopicsChart.getAxisRight().setEnabled(false);
        _topKaTopicsChart.getAxisRight().setDrawLabels(false);
        //_topKaTopicsChart.getAxisRight().setDrawZeroLine(true);
        _topKaTopicsChart.getAxisRight().setDrawGridLines(false);

        _topKaTopicsChart.animateY(2000);

        // This will disable zooming in the scale which pretty much destroys the chart
        _topKaTopicsChart.setScaleEnabled(false);

        _topKaTopicsChart.setData(data);

        _topKaTopicsChart.invalidate();


        _topcKaTopicsProgressBar.setVisibility(View.INVISIBLE);
        _topKaTopicsChart.setVisibility(View.VISIBLE);
    }


    /**
     * This is just a simple formatter that appends the resource 'topics' at the end of the value.
     * It uses a Decimal Formatter because the values are floats by default.
     */
    private class KATopicValueFormatter implements IValueFormatter {

        private DecimalFormat _formatter = new DecimalFormat("###,###,###,##0");

        @Override
        public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
            return _formatter.format(v) + " " + getActivity().getString(R.string.topics_lowercase);
        }
    }

    /**
     * This is just a simple formatter that appends the resource 'topics' at the end of the value.
     * It uses a Decimal Formatter because the values are floats by default.
     */
    private class TopicValueFormatter implements IValueFormatter {

        private DecimalFormat _formatter = new DecimalFormat("###,###,###,##0");

        @Override
        public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
            return _formatter.format(v) + " " + getActivity().getString(R.string.times_matched);
        }
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
         * Loads all the useful variables for this fragment. This basically means all the data of the Knowledge Areas.
         */
        private void loadKnowledgeAreasVariables() {

            List<KnowledgeArea> kas = _parentActivity.getKnowledgeAreas();

            _knowledgeAreas = new KnowledgeArea[kas.size()];
            _knowledgeAreaColours = new int[kas.size()];
            for (KnowledgeArea knowledgeArea : kas) {
                // index is zero-based but KAs are 1-based so... there it goes
                _knowledgeAreas[knowledgeArea.getId() - 1] = knowledgeArea;
                _knowledgeAreaColours[knowledgeArea.getId() - 1] = ContextCompat.getColor(getActivity().getApplicationContext(), knowledgeArea.getColourResource());
            }

            List<KnowledgeAreaTopic> allTopics = _parentActivity.getAllKnowledgeAreaTopics();
            _knowledgeAreaTopics = new KnowledgeAreaTopic[allTopics.size()];
            for (KnowledgeAreaTopic kaTopic : allTopics) {
                _knowledgeAreaTopics[kaTopic.getId() - 1] = kaTopic;
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

            _degreeScore = new SweScore(currentScore);

            DegreeClassCombination degreeCombination = database.where(DegreeClassCombination.class)
                    .equalTo(DegreeClassCombinationFields.COMBINATION_ID, _degreeScoreId)
                    .findFirst();

            _degreeCombination = database.copyFromRealm(degreeCombination);

            database.close();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Very important. Now that all the variables are loaded I can start filling those charts
            // That's what each of these functions to. Only after the data that feeds the charts is available.
            updateDegreeOverViewInfo();
            updateCoverageChart();
            updateKADistributionChart();
            updateSubtitleChart();
            updateTopKaChart(5);
            updateTopKaTopicsChart(10);


        }
    }

    public interface OnScoreChartFragmentInteractionListener extends KnowledgeAreaLoader, DegreeLoader {
    }
}
