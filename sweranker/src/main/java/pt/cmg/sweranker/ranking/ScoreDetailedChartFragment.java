package pt.cmg.sweranker.ranking;

import android.app.Activity;
import android.app.Fragment;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProviders;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import pt.cmg.sweranker.MainActivity;
import pt.cmg.sweranker.MainActivityViewModel;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.swebok.KnowledgeArea;
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
public class ScoreDetailedChartFragment extends Fragment implements LifecycleRegistryOwner, DegreeOverviewDialog.OnDegreeOverviewDialogFragmentListener {


    private LifecycleRegistry _lifecycle;

    @Override
    public LifecycleRegistry getLifecycle() {
        return _lifecycle;
    }


    private static final int SUBTITLE_COLUMN_COUNT = 2;
    private static final int SUBTITLE_ROW_COUNT = 8;

    private String _scoreId;

    /**
     * This array stores the colour mapping to each KA. Each KA has its own colour that identifies it graphically.
     * As it turns out, it is also useful for charting. As each KA has an integer ID each position of this array
     * stores the colour of the the KA whose id = index + 1 (because arrays are zero-based...)
     */
    private int[] _knowledgeAreaColours;


    private KnowledgeArea[] _knowledgeAreas;
    private KnowledgeAreaTopic[] _knowledgeAreaTopics;


    private View _myRootView;

    private TextView _noScoreAvailable;

    private ScrollView _contentArea;
    private ImageView _degreeImage;
    private TextView _overviewDegreeName;
    private TextView _overviewUniversityName;
    private TextView _overviewCombinationName;
    private TextView _showOverview;
    private ProgressBar _overviewProgressBar;

    private GridLayout _subtitleTable;

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
    private boolean _isDegreeCombination;

    private AnnualClassCombination _annualClassCombination;
    private boolean _isAnnualCombination;

    private DegreeClass _degreeClass;
    private boolean _isClassCombination;

    private MainActivityViewModel _sharedViewModel;

    /**
     * This is a reference to the parent activity that this fragment will be attached to on onAttach()
     * It is used to communicate with it.
     */
    private OnScoreDetailedChartFragmentInteractionListener _parentActivity;

    /**
     * Communication interface between this fragment and its parent Activity.
     */
    public interface OnScoreDetailedChartFragmentInteractionListener {

        /**
         * Loads the Chart fragment for this particular score Id. Note that due to
         * the implementation I made, the Degree Score Id and the Degree Combination Id
         * is actually the same, which is nice, but will really make me cry when looking
         * for it in the code somewhere in the future...
         */
        void loadChartFragment(View selectedView);

        void loadDegreeTopicMatcherFragment();

    }


    public ScoreDetailedChartFragment() {
        _lifecycle = new LifecycleRegistry(this);
        _isDegreeCombination = false;
        _isAnnualCombination = false;
        _isClassCombination = false;
    }

    public static ScoreDetailedChartFragment newInstance() {
        return new ScoreDetailedChartFragment();
    }


    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof OnScoreDetailedChartFragmentInteractionListener) {
            _parentActivity = (OnScoreDetailedChartFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement ScoreFragmentInteractionListener");
        }
    }

    // NOTE: this is here because onAttach(Context) was added only on API 23, so as long as Lollipop is min sdk this shall be here
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnScoreDetailedChartFragmentInteractionListener) {
            _parentActivity = (OnScoreDetailedChartFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement ScoreFragmentInteractionListener");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _sharedViewModel = ViewModelProviders.of((MainActivity) getActivity()).get(MainActivityViewModel.class);
        _scoreId = _sharedViewModel.getSelectedScoreId();

        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        _myRootView = inflater.inflate(R.layout.score_chart_fragment, container, false);
        _contentArea = _myRootView.findViewById(R.id.content_area);
        _noScoreAvailable = _myRootView.findViewById(R.id.no_score_text);

        if (_scoreId == null) {
            _contentArea.setVisibility(View.GONE);
            _noScoreAvailable.setText(R.string.no_scores_available_yet);
            _noScoreAvailable.setVisibility(View.VISIBLE);
        } else {
            _contentArea.setVisibility(View.VISIBLE);
            _noScoreAvailable.setVisibility(View.GONE);


            _degreeImage = _myRootView.findViewById(R.id.degree_image);

            _overviewDegreeName = _myRootView.findViewById(R.id.degree_overview_name);
            _overviewUniversityName = _myRootView.findViewById(R.id.degree_overview_university);
            _overviewCombinationName = _myRootView.findViewById(R.id.degree_overview_combo_name);
            _overviewProgressBar = _myRootView.findViewById(R.id.overview_progress);
            _showOverview = _myRootView.findViewById(R.id.show_overview);

            _subtitleTable = _myRootView.findViewById(R.id.chart_legend_table);

            _percentProgressBar = _myRootView.findViewById(R.id.percent_chart_progress);
            _percentProgressBar.setVisibility(View.VISIBLE);
            _kaPercentDistributionChart = _myRootView.findViewById(R.id.ka_distribution_chart);
            _kaPercentDistributionChart.setVisibility(View.INVISIBLE);

            _topKaProgressBar = _myRootView.findViewById(R.id.top_kas_chart_progress);
            _topKaProgressBar.setVisibility(View.VISIBLE);
            _topKaChart = _myRootView.findViewById(R.id.top_kas_chart);

            _topcKaTopicsProgressBar = _myRootView.findViewById(R.id.top_ka_topics_chart_progress);
            _topcKaTopicsProgressBar.setVisibility(View.VISIBLE);
            _topKaTopicsChart = _myRootView.findViewById(R.id.top_ka_topics_chart);

            _coverageChartProgressBar = _myRootView.findViewById(R.id.coverage_chart_progress);
            _coverageChartProgressBar.setVisibility(View.VISIBLE);
            _coverageChart = _myRootView.findViewById(R.id.coverage_chart);

        }

        _sharedViewModel.isLoaded().observe(this, isLoaded -> {
            if (isLoaded && (_sharedViewModel.getSelectedScoreId() != null)) {
                new SweScoreLoader().execute();
            }
        });


        return _myRootView;
    }


    private void updateDegreeOverViewInfo() {


        _degreeImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), _sharedViewModel.getDegree(_degreeScore.getDegreeId()).getImageResource()));
        _overviewDegreeName.setText(getResources().getString(_sharedViewModel.getDegree(_degreeScore.getDegreeId()).getNameResource()));
        _overviewUniversityName.setText(getResources().getString(_sharedViewModel.getDegree(_degreeScore.getDegreeId()).getUniversityResource()));

        if (_isClassCombination) {
            _overviewCombinationName.setText(getResources().getString(_degreeClass.getNameResource()));
        } else if (_isAnnualCombination) {
            _overviewCombinationName.setText(String.format(getResources().getString(R.string.degree_overview_annual_combination), _degreeScore.getId()));
        } else {
            int combinationNumber = Integer.valueOf(_degreeScore.getId().substring(3, _degreeScore.getId().length()));
            _overviewCombinationName.setText(String.format(getResources().getString(R.string.degree_overview_combination), combinationNumber));
        }

        _showOverview.setOnClickListener(view -> {
                    if (_isClassCombination) {
                        _sharedViewModel.setSelectedDegreeClass(_degreeClass);
                        _parentActivity.loadDegreeTopicMatcherFragment();
                    } else {
                        DegreeOverviewDialog.newInstance(this, getAnnualCombinations(), getDegreeClasses()).show(getFragmentManager(), "");
                    }
                }
        );
        _overviewProgressBar.setVisibility(View.INVISIBLE);
    }


    /**
     * This helper function simply loads the Degree Classes that are included in the target score,
     * that can either mean the classes that compose the degree combination, the annual combination
     * or the degree class itself.
     * It is used to construct the parameters that will be sent to the DegreeOverviewDialog.
     */
    private List<DegreeClass> getDegreeClasses() {

        List<DegreeClass> degreeClasses = new ArrayList<>();

        if (_isClassCombination) {
            degreeClasses.add(_degreeClass);
        }
        if (_isAnnualCombination) {
            for (DegreeClassId degreeId : _annualClassCombination.getDegreeClassIds()) {
                degreeClasses.add(_sharedViewModel.getDegreeClass(degreeId.getDegreeClassId()));
            }
        }
        if (_isDegreeCombination) {
            for (AnnualClassCombination annualCombination : _degreeCombination.getAnnualClassCombinations()) {
                for (DegreeClassId degreeId : annualCombination.getDegreeClassIds()) {
                    degreeClasses.add(_sharedViewModel.getDegreeClass(degreeId.getDegreeClassId()));
                }
            }
        }
        return degreeClasses;
    }


    /**
     * This helper function simply loads the Degree Classes that are included in the target score,
     * that can either mean the classes that compose the degree combination, the annual combination
     * or the degree class itself.
     * It is used to construct the parameters that will be sent to the DegreeOverviewDialog.
     */
    private List<AnnualClassCombination> getAnnualCombinations() {

        List<AnnualClassCombination> annualClassCombinations = new ArrayList<>();

        if (_isClassCombination) {
        }
        if (_isAnnualCombination) {
            annualClassCombinations.add(_annualClassCombination);
        }
        if (_isDegreeCombination) {
            annualClassCombinations.addAll(_degreeCombination.getAnnualClassCombinations());
        }

        return annualClassCombinations;
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
        radarData.setValueTextColor(getResources().getColor(R.color.scoreComparisonMaterialRed500));
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
     * Returns a Map view of Knowledge Areas data where each pair represents the KA id and the number of topics it has.
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

        _subtitleTable.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        _subtitleTable.setColumnCount(SUBTITLE_COLUMN_COUNT);
        _subtitleTable.setRowCount(SUBTITLE_ROW_COUNT);

        Drawable squareIcon = ContextCompat.getDrawable(getActivity(), R.drawable.square);

        for (int i = 0, column = 0, row = 0; i < _knowledgeAreas.length; i++, column++) {

            if (column == SUBTITLE_COLUMN_COUNT) {
                column = 0;
                row++;
            }

            TextView kaName = new TextView(getActivity());
            kaName.setText("KA" + _knowledgeAreas[i].getId() + "-" + getResources().getString(_knowledgeAreas[i].getNameResource()));
            kaName.setTextSize(getResources().getDimension(R.dimen.chart_subtitle_text_size));
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


            _subtitleTable.addView(kaName, i);
        }
    }


    /**
     * This function, which I got online, just creates a shape size and colour based on the subtitle TextView size.
     * Tricky, but works wonders.
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
        dataSet.setValueTextSize(getResources().getDimension(R.dimen.swebok_distribution_chart_values_text_size));
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
    private void updateTopKaChart(@IntRange(from = 0, to = 15) int numberOfKasToDisplay) {

        short[] kaCounters = _degreeScore.getKaCounters();

        // Intermediate object is useful for ORDERING and just that
        TreeSet<KAEntry> kaCountersOrdered = new TreeSet<>(ENTRY_COMPARATOR);
        for (int i = 0; i < kaCounters.length; i++) {
            kaCountersOrdered.add(new KAEntry(i, kaCounters[i]));
        }

        int maxTopicsFound = 0;
        for (KAEntry entry : kaCountersOrdered) {
            if (entry.value != 0) {
                maxTopicsFound++;
            } else {
                break;
            }
        }
        int topicsToBeDisplayed = maxTopicsFound > numberOfKasToDisplay ? numberOfKasToDisplay : maxTopicsFound;

        _topKaChart.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                calculateTopKAChartHeight(topicsToBeDisplayed)));

        // These two variables are meant to fill subtitles and colours in the chart
        String[] topKaNames = new String[topicsToBeDisplayed];
        int[] topKaColours = new int[topicsToBeDisplayed];

        Iterator<KAEntry> iterator = kaCountersOrdered.iterator();
        List<BarEntry> entries = new ArrayList<>(topicsToBeDisplayed);

        // Now to fill these variables I am iterating from the TOP to the BOTTOM because in the chart greater values of XAxis are in the top
        // of the chart, which is what I wanted in the first place. Yes, it's ugly. Yes, it is very confusing. Yes, I hate myself for doing it like this.
        for (int i = topicsToBeDisplayed - 1, j = 0; i >= 0; i--, j++) {
            KAEntry currentEntry = iterator.next();

            entries.add(new BarEntry((float) i, (float) currentEntry.value));
            topKaNames[i] = getResources().getString(_knowledgeAreas[currentEntry.id].getNameResource());

            // For some reason the colours are fed to the chart in the opposite indexing as the values, go figure...
            topKaColours[j] = ContextCompat.getColor(getActivity(), _knowledgeAreas[currentEntry.id].getColourResource());

        }


        BarDataSet dataSet = new BarDataSet(entries, "TopKA");
        dataSet.setValueFormatter(new PrefixedNonDecimalValueFormatter(getActivity().getString(R.string.topics_lowercase)));
        dataSet.setColors(topKaColours);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);


        _topKaChart.setDrawBorders(false);
        _topKaChart.getLegend().setEnabled(false);

        // This description part uses a formatted string that reads something along the lines of 'From a total of  X topics'
        Description chartDescription = new Description();
        chartDescription.setText(String.format(getResources().getString(R.string.bar_chart_description), _degreeScore.getTotalTopicCount()));
        chartDescription.setTextAlign(Paint.Align.RIGHT);
        chartDescription.setTextSize((int) getResources().getDimension(R.dimen.top_kas_chart_subtitle_text_size));
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
        // Very important line, this will make sure that the correct number of labels is shown whenever the chart only has a few to show, for example, when viewing an annual combo or class
        _topKaChart.getXAxis().setLabelCount(topicsToBeDisplayed, false);


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


    private int calculateTopKAChartHeight(int topicsToBeDisplayed) {

        float calculatedDimension = getResources().getDimension(R.dimen.top_kas_chart_height);

        if (topicsToBeDisplayed < 4) {
            calculatedDimension = calculatedDimension * 0.5f;
        } else if (topicsToBeDisplayed > 8) {
            calculatedDimension = calculatedDimension * 1.2f;
        }
        return (int) calculatedDimension;
    }


    /**
     * Feeds data and draws an Horizontal Bar Chart with a Top X KAs
     */
    private void updateTopKaTopicsChart(@IntRange(from = 0, to = 102) int numberOfTopicsToDisplay) {


        short[] topicCounter = _degreeScore.getTopicCounters();

        // Intermediate object is useful for ORDERING and just that
        TreeSet<KAEntry> kaTopicCountersOrdered = new TreeSet<>(ENTRY_COMPARATOR);
        for (int i = 0; i < topicCounter.length; i++) {
            kaTopicCountersOrdered.add(new KAEntry(i, topicCounter[i]));
        }

        int maxTopicsFound = 0;
        for (KAEntry entry : kaTopicCountersOrdered) {
            if (entry.value != 0) {
                maxTopicsFound++;
            } else {
                break;
            }
        }
        int topicsToBeDisplayed = maxTopicsFound > numberOfTopicsToDisplay ? numberOfTopicsToDisplay : maxTopicsFound;

        // This part will resize the chart height based on how many elements it has to show
        _topKaTopicsChart.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                calculateTopKATopicsChartHeight(topicsToBeDisplayed)));

        // These two variables are meant to fill subtitles and colours in the chart
        String[] topKaTopicNames = new String[topicsToBeDisplayed];
        int[] topKaTopicColours = new int[topicsToBeDisplayed];

        Iterator<KAEntry> iterator = kaTopicCountersOrdered.iterator();
        List<BarEntry> entries = new ArrayList<>();

        // Now to fill these variables I am iterating from the TOP to the BOTTOM because in the chart greater values of XAxis are in the top
        // of the chart, which is what I wanted in the first place. Yes, it's ugly. Yes, it is very confusing. Yes, I hate myself for doing it like this.
        for (int i = topicsToBeDisplayed - 1, j = 0; i >= 0; i--, j++) {
            KAEntry currentEntry = iterator.next();

            entries.add(new BarEntry((float) i, (float) currentEntry.value));
            topKaTopicNames[i] = getResources().getString(_knowledgeAreaTopics[currentEntry.id].getNameResource());

            // For some reason the colours are fed to the chart in the opposite indexing as the values, go figure...
            topKaTopicColours[j] = ContextCompat.getColor(getActivity(), _knowledgeAreas[_knowledgeAreaTopics[currentEntry.id].getKnowledgeAreaId() - 1].getColourResource());

        }


        BarDataSet dataSet = new BarDataSet(entries, "TopKATopics");
        dataSet.setValueFormatter(new PrefixedNonDecimalValueFormatter(getActivity().getString(R.string.times_matched)));
        dataSet.setColors(topKaTopicColours);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);


        _topKaTopicsChart.setDrawBorders(false);
        _topKaTopicsChart.getLegend().setEnabled(false);

        // This description part uses a formatted string that reads something along the lines of 'From a total of  X topics'
        Description chartDescription = new Description();
        chartDescription.setText(String.format(getResources().getString(R.string.bar_chart_description), _degreeScore.getTotalTopicCount()));
        chartDescription.setTextAlign(Paint.Align.RIGHT);
        chartDescription.setTextSize((int) getResources().getDimension(R.dimen.top_kas_chart_subtitle_text_size));
        _topKaTopicsChart.setDescription(chartDescription);

        _topKaTopicsChart.setFitBars(true);

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
        // Very important line, this will make sure that the correct number of labels is shown whenever the chart only has a few to show, for example, when viewing an annual combo or class
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
     * This is an auxiliary function that calculates a percent of the base value to be used as the actual value of the height
     * of the chart. This is useful so that it can resize a bit better depending on how many elements we are showing.
     *
     * @param topicsToBeDisplayed number of topics currently available to be seen
     * @return value, in pixels, of expected size for the height of the chart
     */
    private int calculateTopKATopicsChartHeight(int topicsToBeDisplayed) {

        float calculatedDimension = getResources().getDimension(R.dimen.top_ka_topics_chart_height);

        if (topicsToBeDisplayed < 4) {
            calculatedDimension = calculatedDimension * 0.4f;
        } else if (topicsToBeDisplayed < 8) {
            calculatedDimension = calculatedDimension * 0.8f;
        } else if (topicsToBeDisplayed > 15) {
            calculatedDimension = calculatedDimension * 1.2f;
        }
        return (int) calculatedDimension;
    }

    /**
     * This is a wrapper class.
     * It basically holds a classic PAIR object. It is very useful to load numerical ID objects and its values
     * into a Collection in a custom ordered way, which is basically the main reason it was used here.
     */
    private class KAEntry {
        private int id;
        private int value;

        KAEntry(int id, int value) {
            this.id = id;
            this.value = value;
        }
    }

    /**
     * Unbelievably useful.
     * I used this comparator so that I could order entries BY THEIR VALUE and not key, as is
     * normal with standard TreeMaps. As ordering will be needed for this charts, this seemed
     * like a nice approach.
     * <p>
     * This orders the values in DESCENDING ORDER and when they are the same, then the keys
     * are ordered in DESCENDING.
     */
    private static Comparator<KAEntry> ENTRY_COMPARATOR = (kaEntry1, kaEntry2) -> {
        if (kaEntry2.value == kaEntry1.value) {
            return kaEntry2.id - kaEntry1.id;
        }
        return kaEntry2.value - kaEntry1.value;
    };

    @Override
    public void onStart() {
        super.onStart();
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START);

    }

    @Override
    public void onResume() {
        super.onResume();
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
    }

    @Override
    public void onPause() {
        super.onPause();
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
    }

    @Override
    public void onStop() {
        super.onStop();
        // I need to remove the observer, because since the observer is set with a lambda it always counts as
        // a new observer, which means it adds to the LiveData observer counter, which means multiple calls to
        // the function.
        _sharedViewModel.getDegrees().removeObservers(this);
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void loadSelectedScoreFragment(String scoreId) {
        _sharedViewModel.setSelectedScoreId(scoreId);
        _parentActivity.loadChartFragment(null);

    }


    /**
     * All the heavier loading work is done here.
     */
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

            List<KnowledgeArea> kas = _sharedViewModel.getKnowledgeAreas().getValue();

            _knowledgeAreas = new KnowledgeArea[kas.size()];
            _knowledgeAreaColours = new int[kas.size()];
            for (KnowledgeArea knowledgeArea : kas) {
                // index is zero-based but KAs are 1-based so... there it goes
                _knowledgeAreas[knowledgeArea.getId() - 1] = knowledgeArea;
                _knowledgeAreaColours[knowledgeArea.getId() - 1] = ContextCompat.getColor(getActivity().getApplicationContext(), knowledgeArea.getColourResource());
            }

            List<KnowledgeAreaTopic> allTopics = _sharedViewModel.getKnowledgeAreaTopics();
            _knowledgeAreaTopics = new KnowledgeAreaTopic[allTopics.size()];
            for (KnowledgeAreaTopic kaTopic : allTopics) {
                _knowledgeAreaTopics[kaTopic.getId() - 1] = kaTopic;
            }


        }

        /**
         * Loads from the Realm database the selected score for this fragment and naturally closes the door afterwards
         */
        private void loadSelectedScore() {
            _degreeScore = _sharedViewModel.getScore(_scoreId);

            switch (_degreeScore.getScoreType()) {
                case SweScore.TYPE_CLASS_SCORE:
                    _degreeClass = _sharedViewModel.getDegreeClass(_scoreId);
                    _isClassCombination = true;
                    _isAnnualCombination = false;
                    _isDegreeCombination = true;
                    break;
                case SweScore.TYPE_ANNUAL_SCORE:
                    _annualClassCombination = _sharedViewModel.getAnnualClassCombination(_scoreId);
                    _isAnnualCombination = true;
                    _isDegreeCombination = false;
                    _isClassCombination = false;
                    break;
                case SweScore.TYPE_DEGREE_SCORE:
                    _degreeCombination = _sharedViewModel.getDegreeClassCombination(_scoreId);
                    _isDegreeCombination = true;
                    _isAnnualCombination = false;
                    _isClassCombination = false;
                    break;
                default:
                    throw new RuntimeException("You should really contact me if this happens...");
            }
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


    /**
     * This is just a simple formatter that appends a String at the end of the value.
     * It uses a Decimal Formatter with zero decimal values because target values are all integers.
     */
    private class PrefixedNonDecimalValueFormatter implements IValueFormatter {

        private String _prefix;
        private DecimalFormat _formatter = new DecimalFormat("###,###,###,##0");

        private PrefixedNonDecimalValueFormatter(String prefix) {
            _prefix = prefix;
        }

        @Override
        public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
            return _formatter.format(v) + " " + _prefix;
        }
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

}
