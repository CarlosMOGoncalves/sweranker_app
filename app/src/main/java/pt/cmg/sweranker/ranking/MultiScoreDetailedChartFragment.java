package pt.cmg.sweranker.ranking;


import android.app.Activity;
import android.app.Fragment;
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
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
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
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import pt.cmg.sweranker.MainActivity;
import pt.cmg.sweranker.MainActivityViewModel;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.degrees.Degree;
import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;

public class MultiScoreDetailedChartFragment extends Fragment implements LifecycleRegistryOwner {


    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    private static final int SUBTITLE_COLUMN_COUNT = 2;
    private static final int SUBTITLE_ROW_COUNT = 8;

    private String _degreeScoreId1;
    private String _degreeScoreId2;

    private Degree _degree1;
    private Degree _degree2;

    private SweScore _degreeScore1;
    private SweScore _degreeScore2;

    private DegreeClassCombination _degreeCombination1;
    private DegreeClassCombination _degreeCombination2;


    /**
     * This array stores the colour mapping to each KA. Each KA has its own colour that identifies it graphically.
     * As it turns out, it is also useful for charting. As each KA has an integer ID each position of this array
     * stores the colour of the the KA whose id = index + 1 (because arrays are zero-based...)
     */
    private int[] _knowledgeAreaColours;


    private KnowledgeArea[] _knowledgeAreas;
    private KnowledgeAreaTopic[] _knowledgeAreaTopics;

    private View _myRootView;

    private TextView _overviewDegreeName;
    private TextView _overviewUniversityName;
    private TextView _overviewCombinationName;
    private TextView _showOverview;
    private TextView _overviewDegreeName2;
    private TextView _overviewUniversityName2;
    private TextView _overviewCombinationName2;
    private TextView _showOverview2;
    private ProgressBar _overviewProgressBar;

    private GridLayout _subtitleTable;

    private ProgressBar _percentProgressBar;
    private TabLayout _percentTabs;
    private ViewPager _percentViewPager;

    private ProgressBar _topKaProgressBar;
    private TabLayout _topKaTabs;
    private ViewPager _topKaViewPager;

    private ProgressBar _topKaTopicsProgressBar;
    private TabLayout _topKaTopicsTabs;
    private ViewPager _topKaTopicsViewPager;

    private ProgressBar _coverageChartProgressBar;
    private RadarChart _coverageChart;

    private MainActivityViewModel _sharedViewModel;


    public MultiScoreDetailedChartFragment() {
        // Required empty public constructor
    }

    public static MultiScoreDetailedChartFragment newInstance() {
        return new MultiScoreDetailedChartFragment();
    }


    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        _sharedViewModel = ViewModelProviders.of((MainActivity) getActivity()).get(MainActivityViewModel.class);
    }

    // NOTE: this is here because onAttach(Context) was added only on API 23, so as long as Lollipop is min sdk this shall be here
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _sharedViewModel = ViewModelProviders.of((MainActivity) getActivity()).get(MainActivityViewModel.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // NOTE: HERE is where I limit the number of degrees to compare
        _degreeScoreId1 = _sharedViewModel.getMultiSelectedDegreeCombinationIds().get(0);
        _degreeScoreId2 = _sharedViewModel.getMultiSelectedDegreeCombinationIds().get(1);
        new SweScoreLoader().execute();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        _myRootView = inflater.inflate(R.layout.multi_score_chart_fragment, container, false);

        _overviewDegreeName = (TextView) _myRootView.findViewById(R.id.degree_overview_name);
        _overviewUniversityName = (TextView) _myRootView.findViewById(R.id.degree_overview_university);
        _overviewCombinationName = (TextView) _myRootView.findViewById(R.id.degree_overview_combo_name);
        _showOverview = (TextView) _myRootView.findViewById(R.id.show_overview);

        _overviewDegreeName2 = (TextView) _myRootView.findViewById(R.id.degree_overview_name_2);
        _overviewUniversityName2 = (TextView) _myRootView.findViewById(R.id.degree_overview_university_2);
        _overviewCombinationName2 = (TextView) _myRootView.findViewById(R.id.degree_overview_combo_name_2);
        _showOverview2 = (TextView) _myRootView.findViewById(R.id.show_overview_2);

        _overviewProgressBar = (ProgressBar) _myRootView.findViewById(R.id.overview_progress);

        _subtitleTable = (GridLayout) _myRootView.findViewById(R.id.chart_legend_table);

        _percentProgressBar = (ProgressBar) _myRootView.findViewById(R.id.ka_percent_chart_progress);
        _percentProgressBar.setVisibility(View.VISIBLE);
        _percentTabs = (TabLayout) _myRootView.findViewById(R.id.ka_percentile_tabs);
        _percentViewPager = (ViewPager) _myRootView.findViewById(R.id.ka_percentile_viewpager);

        _topKaProgressBar = (ProgressBar) _myRootView.findViewById(R.id.top_kas_chart_progress);
        _topKaProgressBar.setVisibility(View.VISIBLE);
        _topKaTabs = (TabLayout) _myRootView.findViewById(R.id.top_ka_tabs);
        _topKaViewPager = (ViewPager) _myRootView.findViewById(R.id.top_ka_viewpager);

        _topKaTopicsProgressBar = (ProgressBar) _myRootView.findViewById(R.id.top_ka_topics_chart_progress);
        _topKaTopicsProgressBar.setVisibility(View.VISIBLE);
        _topKaTopicsTabs = (TabLayout) _myRootView.findViewById(R.id.top_ka_topics_tabs);
        _topKaTopicsViewPager = (ViewPager) _myRootView.findViewById(R.id.top_ka_topics_viewpager);

        _coverageChartProgressBar = (ProgressBar) _myRootView.findViewById(R.id.coverage_chart_progress);
        _coverageChartProgressBar.setVisibility(View.VISIBLE);
        _coverageChart = (RadarChart) _myRootView.findViewById(R.id.coverage_chart);

        return _myRootView;
    }

    private void updateDegreeOverViewInfo() {

        int degreeCombinationNumber1 = Integer.valueOf(_degreeScore1.getId().substring(3, _degreeScore1.getId().length()));

        _overviewDegreeName.setText(getResources().getString(_degree1.getNameResource()));
        _overviewUniversityName.setText(getResources().getString(_degree1.getUniversityResource()));
        _overviewCombinationName.setText(String.format(getResources().getString(R.string.degree_overview_combination), degreeCombinationNumber1));
        _showOverview.setOnClickListener(view ->
                DegreeOverviewDialog.newInstance(getDegreeClasses(_degreeCombination1)).show(getFragmentManager(), "")
        );

        int degreeCombinationNumber2 = Integer.valueOf(_degreeScore2.getId().substring(3, _degreeScore2.getId().length()));

        _overviewDegreeName2.setText(getResources().getString(_degree2.getNameResource()));
        _overviewUniversityName2.setText(getResources().getString(_degree2.getUniversityResource()));
        _overviewCombinationName2.setText(String.format(getResources().getString(R.string.degree_overview_combination), degreeCombinationNumber2));
        _showOverview2.setOnClickListener(view ->
                DegreeOverviewDialog.newInstance(getDegreeClasses(_degreeCombination2)).show(getFragmentManager(), "")
        );

        _overviewProgressBar.setVisibility(View.INVISIBLE);
    }


    /**
     * This helper function simply loads from the parent activity ALL the classes that compos THIS particular
     * degree. This function is used to construct the parameters that will be sent to the DegreeOverviewDialog.
     */
    private List<DegreeClass> getDegreeClasses(DegreeClassCombination degreeCombination) {

        List<DegreeClass> degreeClasses = new ArrayList<>();

        for (AnnualClassCombination annualCombination : degreeCombination.getAnnualClassCombinations()) {
            for (DegreeClassId degreeId : annualCombination.getDegreeClassIds()) {
                degreeClasses.add(_sharedViewModel.getDegreeClass(degreeId.getDegreeClassId()));
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

        String[] kas = new String[15];
        for (int i = 0; i < 15; i++) {
            kas[i] = "KA" + _knowledgeAreas[i].getId();
        }

        // For the first Degree selected
        Map<Integer, Integer> coveredTopics = createCoveredTopicsByKaView(_degreeScore1);
        float percentCovered;
        List<RadarEntry> entries = new ArrayList<>();
        for (int i = 0; i < totalTopicsByKa.size(); i++) {
            percentCovered = (float) coveredTopics.get(i + 1) / (float) totalTopicsByKa.get(i + 1) * 100;
            entries.add(new RadarEntry(percentCovered));
        }

        // For the second Degree selected
        Map<Integer, Integer> coveredTopics2 = createCoveredTopicsByKaView(_degreeScore2);
        float percentCovered2;
        List<RadarEntry> entries2 = new ArrayList<>();
        for (int i = 0; i < totalTopicsByKa.size(); i++) {
            percentCovered2 = (float) coveredTopics2.get(i + 1) / (float) totalTopicsByKa.get(i + 1) * 100;
            entries2.add(new RadarEntry(percentCovered2));
        }

        RadarDataSet dataSet = new RadarDataSet(entries, "KACoverage");
        dataSet.setColor(getResources().getColor(R.color.scoreComparisonMaterialBlue500));

        // IMPORTANT: using a slight larger line with these two colours allows for the appearance of the line to become another colour: purple
        // which is nice to distinguish where the radar lines intertwine
        dataSet.setLineWidth(1.5f);

        RadarDataSet dataSet2 = new RadarDataSet(entries2, "KACoverage2");
        dataSet2.setColor(getResources().getColor(R.color.scoreComparisonMaterialRed500));

        RadarData radarData = new RadarData(dataSet);
        radarData.addDataSet(dataSet2);
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
     * <p>
     * The way this function works is: I already have a collection with all the KA Topics,
     * so I traverse the array that has all the KA Topics in a Score.
     * <p>
     * These can either be not matched(i.e. that particular topic was not matched even once by a Degree Class Topic)
     * or matched multiple times (i.e. it was matched AT LEAST once by one or more Degree Class Topics).
     * <p>
     * If it was matched at least once ( if(topicCounters[i])>0 ) then I add 1 to the matched
     * map. When I need to calculate percentages I just need to divide each value of those by the total
     * number of topics per KA. And that's how I met your mother, kids.
     *
     * @return
     */
    private Map<Integer, Integer> createCoveredTopicsByKaView(SweScore sweScore) {
        Map<Integer, Integer> result = new HashMap<>();

        for (int i = 0; i < _knowledgeAreas.length; i++) {

            // The last KA is irrelevant because it is the OTHER
            if (_knowledgeAreas[i].getId() == 16) {
                continue;
            }
            result.put(_knowledgeAreas[i].getId(), 0);
        }

        short[] topicCounters = sweScore.getTopicCounters();
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


            _subtitleTable.addView(kaName, i);
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

        _percentViewPager.setAdapter(new KaDistributionComparisonAdapter());
        _percentTabs.setupWithViewPager(_percentViewPager);
        // Initial colouring
        _percentTabs.setTabTextColors(getResources().getColor(R.color.unselectedTextColor), getResources().getColor(R.color.scoreComparisonMaterialBlue500));
        _percentTabs.setSelectedTabIndicatorColor(getResources().getColor(R.color.scoreComparisonMaterialBlue500));
        // On selected tab colouring
        _percentTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                _percentViewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {
                    _percentTabs.setSelectedTabIndicatorColor(getResources().getColor(R.color.scoreComparisonMaterialBlue500));
                    _percentTabs.setTabTextColors(getResources().getColor(R.color.unselectedTextColor), getResources().getColor(R.color.scoreComparisonMaterialBlue500));
                } else {
                    _percentTabs.setSelectedTabIndicatorColor(getResources().getColor(R.color.scoreComparisonMaterialRed500));
                    _percentTabs.setTabTextColors(getResources().getColor(R.color.unselectedTextColor), getResources().getColor(R.color.scoreComparisonMaterialRed500));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        _percentProgressBar.setVisibility(View.INVISIBLE);
    }


    /**
     * This adapter is needed because I am using a Page Viewer and avoiding the common Fragment approach.
     */
    private class KaDistributionComparisonAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {

            PieChart pieChart;
            if (position == 0) {
                pieChart = createKADistributionChart(_degreeScore1);
                collection.addView(pieChart);
            } else {
                pieChart = createKADistributionChart(_degreeScore2);
                collection.addView(pieChart);
            }
            return pieChart;
        }


        private PieChart createKADistributionChart(SweScore degreeScore) {
            PieChart kaDistributionChart = new PieChart(getActivity());

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            kaDistributionChart.setLayoutParams(layoutParams);

            float[] kaPercents = degreeScore.getKaPercents();

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
            kaDistributionChart.setData(pieChartData);

            // Both description and subtitle will be displayed apart so I am disabling them both
            kaDistributionChart.getDescription().setEnabled(false);
            kaDistributionChart.getLegend().setEnabled(false);
            // Also labels, in a smart phone they would likely be useless. I will try to find another way to display it.
            kaDistributionChart.setDrawEntryLabels(false);

            kaDistributionChart.setHoleRadius(40f);
            kaDistributionChart.setTransparentCircleRadius(45f);

            kaDistributionChart.animateX(1000);

            kaDistributionChart.invalidate();

            kaDistributionChart.setTouchEnabled(true);
            kaDistributionChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry entry, Highlight highlight) {
                    kaDistributionChart.setCenterText(((PieEntry) entry).getLabel());
                    kaDistributionChart.setCenterTextColor(_knowledgeAreaColours[(Integer) entry.getData()]);
                }

                @Override
                public void onNothingSelected() {
                    kaDistributionChart.setCenterText("");
                }
            });

            return kaDistributionChart;
        }


        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(_degree1.getNameResource()) + " 1";
            }

            return getString(_degree2.getNameResource()) + " 2";
        }
    }


    /**
     * Feeds data and draws an Horizontal Bar Chart with a Top X KAs
     */
    private void updateTopKaChart(@IntRange(from = 0, to = 15) int numberOfKasToDisplay) {

        _topKaViewPager.setAdapter(new TopKaComparisonAdapter(numberOfKasToDisplay));
        _topKaTabs.setupWithViewPager(_topKaViewPager);

        //Initial colouring - defaults
        _topKaTabs.setTabTextColors(getResources().getColor(R.color.unselectedTextColor), getResources().getColor(R.color.scoreComparisonMaterialBlue500));
        _topKaTabs.setSelectedTabIndicatorColor(getResources().getColor(R.color.scoreComparisonMaterialBlue500));
        // On selected tab colouring
        _topKaTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                _topKaViewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {
                    _topKaTabs.setSelectedTabIndicatorColor(getResources().getColor(R.color.scoreComparisonMaterialBlue500));
                    _topKaTabs.setTabTextColors(getResources().getColor(R.color.unselectedTextColor), getResources().getColor(R.color.scoreComparisonMaterialBlue500));
                } else {
                    _topKaTabs.setSelectedTabIndicatorColor(getResources().getColor(R.color.scoreComparisonMaterialRed500));
                    _topKaTabs.setTabTextColors(getResources().getColor(R.color.unselectedTextColor), getResources().getColor(R.color.scoreComparisonMaterialRed500));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        _topKaProgressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * This adapter is needed because I am using a Page Viewer and avoiding the common Fragment approach.
     */
    private class TopKaComparisonAdapter extends PagerAdapter {

        private int _numberOfKasToDisplay;

        private TopKaComparisonAdapter(int numberOfKasToDisplay) {
            _numberOfKasToDisplay = numberOfKasToDisplay;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {

            HorizontalBarChart horizontalBarChart;
            if (position == 0) {
                horizontalBarChart = createTopKAChart(_degreeScore1, _numberOfKasToDisplay);
                collection.addView(horizontalBarChart);
            } else {
                horizontalBarChart = createTopKAChart(_degreeScore2, _numberOfKasToDisplay);
                collection.addView(horizontalBarChart);
            }
            return horizontalBarChart;
        }

        /**
         * This has the actual work of creating the Horizontal Bar Chart, adapting it to the screen sizing, filling it with data and
         * finally styling it. It is a single function because it is needed multiple times, one for each Degree Combination Score being compared.
         *
         * @param degreeScore
         * @param numberOfKasToDisplay The number of bars that will be displayed. The higher the more complete picture of the KA  distribution,
         *                             but also the more difficult to see in the screen.
         * @return
         */
        private HorizontalBarChart createTopKAChart(SweScore degreeScore, int numberOfKasToDisplay) {
            HorizontalBarChart horizontalBarChart = new HorizontalBarChart(getActivity());

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) Utils.convertDpToPixel(500f));
            horizontalBarChart.setLayoutParams(layoutParams);

            short[] kaCounters = degreeScore.getKaCounters();

            // This part is tricky, I am getting a TreeMap to take advantage of the ordering it makes
            // So now I have a Map where keys are the actual values ORDERED and the values are the array indexes which are equivalent
            // to the KA ids minus 1. Dear future me, don't hate me, remember that this native array stuff was implemented for performance reasons
            // as well as simplicity because of the Realm database.
            TreeMap<Integer, Integer> countersAndKaIds = new TreeMap<>();
            for (int i = 0; i < kaCounters.length; i++) {
                countersAndKaIds.put((int) kaCounters[i], i);
            }

            // These two variables are meant to fill subtitles and colours in the chart
            String[] topKaNames = new String[numberOfKasToDisplay];
            int[] topKaColours = new int[numberOfKasToDisplay];

            // Now tricking intensifies. I am iterating in the reverse order (where the bigger numbers are).
            // Yes I could simply have implemented a comparator to order them in reverse when inserting in the TreeMap, there are millions of solutions
            Iterator<Integer> iterator = countersAndKaIds.descendingKeySet().iterator();
            int currentKeyWhichIsActuallyAValue;
            int currentKaId;
            List<BarEntry> entries = new ArrayList<>();

            // Now to fill these variables I am iterating from the TOP to the BOTTOM because in the chart greater values of XAxis are in the top
            // of the chart, which is what I wanted in the first place. Yes, it's ugly. Yes, it is very confusing. Yes, I hate myself for doing it like this.
            for (int i = numberOfKasToDisplay - 1, j = 0; i >= 0; i--, j++) {
                currentKeyWhichIsActuallyAValue = iterator.next();
                currentKaId = countersAndKaIds.get(currentKeyWhichIsActuallyAValue);

                entries.add(new BarEntry((float) i, (float) currentKeyWhichIsActuallyAValue));
                topKaNames[i] = getResources().getString(_knowledgeAreas[currentKaId].getNameResource());

                // For some reason the colours are fed to the chart in the opposite indexing as the values, go figure...
                topKaColours[j] = ContextCompat.getColor(getActivity(), _knowledgeAreas[currentKaId].getColourResource());

            }


            BarDataSet dataSet = new BarDataSet(entries, "TopKA");
            dataSet.setValueFormatter(new PrefixedNonDecimalValueFormatter(getActivity().getString(R.string.topics_lowercase)));
            dataSet.setColors(topKaColours);

            BarData data = new BarData(dataSet);
            data.setBarWidth(0.9f);


            horizontalBarChart.setDrawBorders(false);
            horizontalBarChart.getLegend().setEnabled(false);

            // This description part uses a formatted string that reads something along the lines of 'From a total of  X topics'
            Description chartDescription = new Description();
            chartDescription.setText(String.format(getResources().getString(R.string.bar_chart_description), degreeScore.getTotalTopicCount()));
            chartDescription.setTextAlign(Paint.Align.RIGHT);
            chartDescription.setTextSize((int) getResources().getDimension(R.dimen.chart_top_ka_description_text_size));
            horizontalBarChart.setDescription(chartDescription);

            horizontalBarChart.setFitBars(true);

            // All the gibberish below simply deactivates most of the grid lines to get the effect I wanted
            horizontalBarChart.getXAxis().setEnabled(true);
            horizontalBarChart.getXAxis().setDrawAxisLine(false);
            horizontalBarChart.getXAxis().setDrawGridLines(false);
            // This puts the text to the LEFT of the chart
            horizontalBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            // And this part replaces the X values by actual labels
            horizontalBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(topKaNames));

            horizontalBarChart.getAxisLeft().setEnabled(false);
            horizontalBarChart.getAxisLeft().setDrawGridLines(false);
            horizontalBarChart.getAxisLeft().setDrawZeroLine(true);
            horizontalBarChart.getAxisLeft().setDrawLabels(false);
            horizontalBarChart.getAxisLeft().setAxisMinimum(0f);

            horizontalBarChart.getAxisRight().setEnabled(false);
            horizontalBarChart.getAxisRight().setDrawLabels(false);
            horizontalBarChart.getAxisRight().setDrawGridLines(false);

            horizontalBarChart.animateY(2000);

            // This will disable zooming in the scale which pretty much destroys the chart
            horizontalBarChart.setScaleEnabled(false);

            horizontalBarChart.setData(data);

            horizontalBarChart.invalidate();

            return horizontalBarChart;
        }


        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(_degree1.getNameResource()) + " 1";
            }

            return getString(_degree2.getNameResource()) + " 2";
        }
    }

    /**
     * Creates the dual Top KA Topics Horizontal chart that is used to compare two degree combination scores
     * concerning their most matched KA Topics.
     */
    private void updateTopKaTopicsChart(@IntRange(from = 0, to = 102) int numberOfTopicsToDisplay) {

        _topKaTopicsViewPager.setAdapter(new TopKaTopicsComparisonAdapter(numberOfTopicsToDisplay));
        _topKaTopicsTabs.setupWithViewPager(_topKaTopicsViewPager);

        //Initial colouring - defaults
        _topKaTopicsTabs.setTabTextColors(getResources().getColor(R.color.unselectedTextColor), getResources().getColor(R.color.scoreComparisonMaterialBlue500));
        _topKaTopicsTabs.setSelectedTabIndicatorColor(getResources().getColor(R.color.scoreComparisonMaterialBlue500));
        // On selected tab colouring
        _topKaTopicsTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                _topKaTopicsViewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {
                    _topKaTopicsTabs.setSelectedTabIndicatorColor(getResources().getColor(R.color.scoreComparisonMaterialBlue500));
                    _topKaTopicsTabs.setTabTextColors(getResources().getColor(R.color.unselectedTextColor), getResources().getColor(R.color.scoreComparisonMaterialBlue500));
                } else {
                    _topKaTopicsTabs.setSelectedTabIndicatorColor(getResources().getColor(R.color.scoreComparisonMaterialRed500));
                    _topKaTopicsTabs.setTabTextColors(getResources().getColor(R.color.unselectedTextColor), getResources().getColor(R.color.scoreComparisonMaterialRed500));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        _topKaTopicsProgressBar.setVisibility(View.INVISIBLE);

    }


    /**
     * This adapter is needed because I am using a Page Viewer and avoiding the common Fragment approach.
     */
    private class TopKaTopicsComparisonAdapter extends PagerAdapter {

        private int _numberOfKaTopicsToDisplay;

        private TopKaTopicsComparisonAdapter(int numberOfKaTopicsToDisplay) {
            _numberOfKaTopicsToDisplay = numberOfKaTopicsToDisplay;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {

            HorizontalBarChart horizontalBarChart;
            if (position == 0) {
                horizontalBarChart = createTopKaTopicsChart(_degreeScore1, _numberOfKaTopicsToDisplay);
                collection.addView(horizontalBarChart);
            } else {
                horizontalBarChart = createTopKaTopicsChart(_degreeScore2, _numberOfKaTopicsToDisplay);

                collection.addView(horizontalBarChart);
            }

            return horizontalBarChart;
        }


        /**
         * This has the actual work of creating the Horizontal Bar Chart, adapting it to the screen sizing, filling ti with data and
         * finally styling it. It is a single function because it is needed multiple times, one for each Degree Combination Score being compared.
         *
         * @param degreeScore
         * @param numberOfKaTopicsToDisplay The number of bars that will be displayed. The higher the more complete picture of the KA topics distribution,
         *                                  but also the more difficult to see in the screen.
         * @return
         */
        private HorizontalBarChart createTopKaTopicsChart(SweScore degreeScore, int numberOfKaTopicsToDisplay) {

            HorizontalBarChart topKaTopicsChart = new HorizontalBarChart(getActivity());

            // NOTE: this hardcoded value should actually be calculated. But alas, I will just call this BETA like Google.
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) Utils.convertDpToPixel(800f));
            topKaTopicsChart.setLayoutParams(layoutParams);

            short[] topicCounter = degreeScore.getTopicCounters();

            // This part is tricky, I am getting a TreeMap to take advantage of the ordering it makes
            // So now I have a Map where keys are the actual values ORDERED and the values are the array indexes which are equivalent
            // to the KA ids minus 1. Dear future me, don't hate me, remember that this native array stuff was implemented for performance reasons
            // as well as simplicity because of the Realm database.
            TreeMap<Integer, Integer> countersAndKaTopicIds = new TreeMap<>();
            for (int i = 0; i < topicCounter.length; i++) {
                countersAndKaTopicIds.put((int) topicCounter[i], i);
            }

            // These two variables are meant to fill subtitles and colours in the chart
            String[] topKaTopicNames = new String[numberOfKaTopicsToDisplay];
            int[] topKaTopicColours = new int[numberOfKaTopicsToDisplay];

            // Now tricking intensifies. I am iterating in the reverse order (where the bigger numbers are).
            // Yes I could simply have implemented a comparator to order them in reverse when inserting in the TreeMap, there are millions of solutions
            Iterator<Integer> iterator = countersAndKaTopicIds.descendingKeySet().iterator();
            int currentKeyWhichIsActuallyAValue;
            int currentKATopicId;
            List<BarEntry> entries = new ArrayList<>();

            // Now to fill these variables I am iterating from the TOP to the BOTTOM because in the chart greater values of XAxis are in the top
            // of the chart, which is what I wanted in the first place. Yes, it's ugly. Yes, it is very confusing. Yes, I hate myself for doing it like this.
            for (int i = numberOfKaTopicsToDisplay - 1, j = 0; i >= 0; i--, j++) {
                currentKeyWhichIsActuallyAValue = iterator.next();
                currentKATopicId = countersAndKaTopicIds.get(currentKeyWhichIsActuallyAValue);

                entries.add(new BarEntry((float) i, (float) currentKeyWhichIsActuallyAValue));
                topKaTopicNames[i] = getResources().getString(_knowledgeAreaTopics[currentKATopicId].getNameResource());

                // For some reason the colours are fed to the chart in the opposite indexing as the values, go figure...
                topKaTopicColours[j] = ContextCompat.getColor(getActivity(), _knowledgeAreas[_knowledgeAreaTopics[currentKATopicId].getKnowledgeAreaId() - 1].getColourResource());

            }


            BarDataSet dataSet = new BarDataSet(entries, "TopKATopics");
            dataSet.setValueFormatter(new PrefixedNonDecimalValueFormatter(getActivity().getString(R.string.times_matched)));
            dataSet.setValueTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            dataSet.setColors(topKaTopicColours);

            BarData data = new BarData(dataSet);
            data.setBarWidth(0.8f);


            topKaTopicsChart.setDrawBorders(false);

            topKaTopicsChart.getLegend().setEnabled(false);

            // This description part uses a formatted string that reads something along the lines of 'From a total of  X topics'
            Description chartDescription = new Description();
            chartDescription.setText(String.format(getResources().getString(R.string.bar_chart_description), degreeScore.getTotalTopicCount()));
            chartDescription.setTextAlign(Paint.Align.RIGHT);
            chartDescription.setTextSize((int) getResources().getDimension(R.dimen.chart_top_ka_description_text_size));
            topKaTopicsChart.setDescription(chartDescription);

            topKaTopicsChart.setFitBars(true);

            topKaTopicsChart.setDrawValueAboveBar(false);

            // All the gibberish below simply deactivates most of the grid lines to get the effect I wanted
            topKaTopicsChart.getXAxis().setEnabled(true);
            topKaTopicsChart.getXAxis().setDrawAxisLine(false);
            topKaTopicsChart.getXAxis().setDrawGridLines(false);
            // This puts the text to the LEFT of the chart
            topKaTopicsChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            // And this part replaces the X values by actual labels
            topKaTopicsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(topKaTopicNames));
            topKaTopicsChart.getXAxis().setLabelCount(topKaTopicNames.length);

            topKaTopicsChart.getAxisLeft().setEnabled(false);
            //_topKaTopicsChart.getAxisLeft().setDrawAxisLine(false);
            topKaTopicsChart.getAxisLeft().setDrawGridLines(false);
            topKaTopicsChart.getAxisLeft().setDrawZeroLine(true);
            topKaTopicsChart.getAxisLeft().setDrawLabels(false);
            topKaTopicsChart.getAxisLeft().setAxisMinimum(0f);

            topKaTopicsChart.getAxisRight().setEnabled(false);
            topKaTopicsChart.getAxisRight().setDrawLabels(false);
            topKaTopicsChart.getAxisRight().setDrawGridLines(false);

            topKaTopicsChart.animateY(2000);

            // This will disable zooming in the scale which pretty much destroys the chart
            topKaTopicsChart.setScaleEnabled(false);

            topKaTopicsChart.setData(data);

            topKaTopicsChart.invalidate();

            return topKaTopicsChart;
        }


        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(_degree1.getNameResource()) + " 1";
            }

            return getString(_degree2.getNameResource()) + " 2";
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
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
            _degreeScore1 = _sharedViewModel.getDegreeScore(_degreeScoreId1);
            _degree1 = _sharedViewModel.getDegree(_degreeScore1.getDegreeId());

            _degreeScore2 = _sharedViewModel.getDegreeScore(_degreeScoreId2);
            _degree2 = _sharedViewModel.getDegree(_degreeScore2.getDegreeId());

            _degreeCombination1 = _sharedViewModel.getDegreeClassCombination(_degreeScoreId1);
            _degreeCombination2 = _sharedViewModel.getDegreeClassCombination(_degreeScoreId2);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Very important. Now that all the variables are loaded I can start filling those charts
            // That's what each of these functions to. Only after the data that feeds the charts is available.
            updateDegreeOverViewInfo();
            updateCoverageChart();
            updateSubtitleChart();
            updateKADistributionChart();
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
