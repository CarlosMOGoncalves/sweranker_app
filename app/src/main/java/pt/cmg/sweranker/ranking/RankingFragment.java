package pt.cmg.sweranker.ranking;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.Sort;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.degrees.Degree;
import pt.cmg.sweranker.degrees.DegreeLoader;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaLoader;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;

public class RankingFragment extends Fragment {

    public static final String ACTION_RECEIVER = "pt.cmg.sweranker.CALCULATION_FINISHED";

    /**
     * This is a reference to the parent activity that this fragment will be attached to on onAttach()
     * It is used to communicate with it.
     */
    private RankingFragmentInteractionListener _parentActivity;

    private RecyclerView _rankingsGrid;
    private View _myRootView;

    private View _filterDialog;
    private Spinner _filterDialogSelectedKA;
    private Spinner _filterDialogSelectedOrder;
    private Spinner _filterDialogSelectedDegree;
    private Spinner _filterDialogLimitSpinner;

    private ProgressBar _progressBar;
    private ScoresAndImagesAdapter _adapter;

    private Map<Integer, Degree> _degrees;
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<Degree> degrees = _parentActivity.getAllDegrees();
        _degrees = new LinkedHashMap<>();
        for (Degree d : degrees) {
            _degrees.put(d.getId(), d);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.app_bar_score_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_filter:
                createAndShowFilterDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void createAndShowFilterDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());


        dialogBuilder.setView(createFilterDialogView())
                .setCustomTitle(getActivity().getLayoutInflater().inflate(R.layout.ranking_fragment_filter_dialog_title, null))
                .setPositiveButton(getResources().getString(R.string.apply), (dialog, which) -> {

                    // In the adapter this is the same
                    int kaId = (int) _filterDialogSelectedKA.getSelectedItemId();
                    // This conversion is inferred by the below Array Adapter.
                    Sort order = _filterDialogSelectedOrder.getSelectedItemId() == 0 ? Sort.ASCENDING : Sort.DESCENDING;
                    // In the Adapter this is also the same
                    int degreeId = (int) _filterDialogSelectedDegree.getSelectedItemId();
                    // Also inferred from below, ain't nobody got time for parameterization.
                    int limit = _filterDialogLimitSpinner.getSelectedItemId() == 3 ? 0 : Integer.valueOf((String) _filterDialogLimitSpinner.getSelectedItem());

                    // The positive button launches a new search with new parameters
                    new DegreeComboQueryLoader(kaId, order, degreeId, limit).execute();

                    dialog.cancel();

                })
                .setNegativeButton(getResources().getString(R.string.dismiss), (dialog, id) ->
                        dialog.cancel()
                );

        dialogBuilder.create().show();
    }

    private View createFilterDialogView() {
        _filterDialog = getActivity().getLayoutInflater().inflate(R.layout.ranking_fragment_filter_dialog, null);

        _filterDialogSelectedKA = (Spinner) _filterDialog.findViewById(R.id.knowledge_area_spinner);
        _filterDialogSelectedKA.setAdapter(new KASpinnerAdapter());

        String[] orders = new String[]{getActivity().getString(R.string.order_ascending), getActivity().getString(R.string.order_descending)};
        _filterDialogSelectedOrder = (Spinner) _filterDialog.findViewById(R.id.order_spinner);
        _filterDialogSelectedOrder.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.ranking_fragment_filter_dialog_spinner_item, orders));

        String[] limits = new String[]{"1", "10", "50", getActivity().getString(R.string.filter_all_label)};
        _filterDialogLimitSpinner = (Spinner) _filterDialog.findViewById(R.id.results_number_spinner);
        _filterDialogLimitSpinner.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.ranking_fragment_filter_dialog_spinner_item, limits));
        _filterDialogLimitSpinner.setSelection(1);

        _filterDialogSelectedDegree = (Spinner) _filterDialog.findViewById(R.id.degree_spinner);
        _filterDialogSelectedDegree.setAdapter(new DegreeSpinnerAdapter());

        return _filterDialog;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        _myRootView = inflater.inflate(R.layout.ranking_fragment, container, false);

        _progressBar = (ProgressBar) _myRootView.findViewById(R.id.progress_bar);

        _rankingsGrid = (RecyclerView) _myRootView.findViewById(R.id.rankings_grid);


        // This is needed so it doesn't load new images every time this fragment is made visible.
        if (_combinationNameAndImage == null) {
            _progressBar.setVisibility(View.VISIBLE);
            _rankingsGrid.setVisibility(View.GONE);
            new DegreeComboQueryLoader().execute();
        } else {
            initialiseRankingGrid();
        }

        return _myRootView;
    }


    private void initialiseRankingGrid() {

        _adapter = new ScoresAndImagesAdapter(getActivity(),
                _combinationNameAndImage,
                (rootView, degreeCombinationId) -> _parentActivity.loadChartFragment(rootView, degreeCombinationId));

        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 4);
        _rankingsGrid.setLayoutManager(mLayoutManager);
        _rankingsGrid.addItemDecoration(new ConstantSpacingItemDecorator(getActivity(),
                2,
                ConstantSpacingItemDecorator.Side.ALL_SIDES));
        _rankingsGrid.setItemAnimator(new DefaultItemAnimator());
        _rankingsGrid.setAdapter(_adapter);
        _adapter.notifyDataSetChanged();

        _progressBar.setVisibility(View.INVISIBLE);
        _rankingsGrid.setVisibility(View.VISIBLE);
    }


    /**
     * This AsyncTask's job is to load the scores from the system to finally show them in an ordered way.
     * It executes a parameterised query against the Realm database.
     * These parameters are passed by the Filter Dialog where the user can set how many results he wants
     * from the system, as well as their order in relation to a specific Knowledge Area.
     */
    private class DegreeComboQueryLoader extends AsyncTask<Void, Void, Void> {

        private String _kaFieldName;
        private Sort _sortOrder;
        private int _degreeid;
        private int _limit;


        private DegreeComboQueryLoader() {
            _kaFieldName = SweScoreFields.KA_PERCENT1;
            _sortOrder = Sort.DESCENDING;
            _degreeid = 0;
            _limit = 10;
        }

        private DegreeComboQueryLoader(int kaId, Sort order, int degreeId, int limit) {
            _kaFieldName = getKaFieldName(kaId);
            _sortOrder = order;
            _degreeid = degreeId;
            _limit = limit;
        }

        /**
         * Just translates an ID to a specific field name used for the query.
         *
         * @param kaId
         * @return
         */
        private String getKaFieldName(int kaId) {
            // This is ugly and most likely shouldn't be here. However I am almost finished with this and
            // I won't spend my time now organising this.
            switch (kaId) {
                case 1:
                    return SweScoreFields.KA_PERCENT1;
                case 2:
                    return SweScoreFields.KA_PERCENT2;
                case 3:
                    return SweScoreFields.KA_PERCENT3;
                case 4:
                    return SweScoreFields.KA_PERCENT4;
                case 5:
                    return SweScoreFields.KA_PERCENT5;
                case 6:
                    return SweScoreFields.KA_PERCENT6;
                case 7:
                    return SweScoreFields.KA_PERCENT7;
                case 8:
                    return SweScoreFields.KA_PERCENT8;
                case 9:
                    return SweScoreFields.KA_PERCENT9;
                case 10:
                    return SweScoreFields.KA_PERCENT10;
                case 11:
                    return SweScoreFields.KA_PERCENT11;
                case 12:
                    return SweScoreFields.KA_PERCENT12;
                case 13:
                    return SweScoreFields.KA_PERCENT13;
                case 14:
                    return SweScoreFields.KA_PERCENT14;
                case 15:
                    return SweScoreFields.KA_PERCENT15;
                case 16:
                    return SweScoreFields.KA_PERCENT16;
                default:
                    return SweScoreFields.KA_PERCENT1;
            }
        }

        @Override
        protected void onPreExecute() {

            _progressBar.setVisibility(View.VISIBLE);
            _rankingsGrid.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Realm databaseConnection = Realm.getDefaultInstance();

            List<SweScore> sampleScores = databaseConnection.where(SweScore.class)
                    .equalTo(SweScoreFields.SCORE_TYPE, SweScore.TYPE_DEGREE_SCORE)
                    .equalTo(SweScoreFields.DEGREE_ID, _degreeid == 0 ? 1 : _degreeid)
                    .findAllSorted(_kaFieldName, _sortOrder);

            _combinationNameAndImage = new LinkedHashMap<>();
            int resultsLimit = _limit == 0 ? sampleScores.size() : _limit;
            if (!sampleScores.isEmpty()) {
                for (int i = 0; i < resultsLimit; i++) {
                    SweScore currentScore = sampleScores.get(i);
                    _combinationNameAndImage.put(new String(currentScore.getId()), _degrees.get((int) currentScore.getDegreeId()).getImageResource());
                }
            }
            databaseConnection.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            initialiseRankingGrid();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _parentActivity = null;
    }


    private class KASpinnerAdapter extends BaseAdapter {

        private List<KnowledgeArea> _knowledgeAreas;
        private Context _context;

        private KASpinnerAdapter() {
            _context = getActivity();
            _knowledgeAreas = _parentActivity.getKnowledgeAreas();
        }

        @Override
        public int getCount() {
            return _knowledgeAreas.size();
        }

        @Override
        public Object getItem(int position) {
            return _knowledgeAreas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return _knowledgeAreas.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(_context).inflate(R.layout.ranking_fragment_filter_dialog_spinner_item, null);
            ((TextView) convertView.findViewById(R.id.list_item)).setText(_context.getString(_knowledgeAreas.get(position).getNameResource()));
            return convertView;
        }
    }


    private class DegreeSpinnerAdapter extends BaseAdapter {
        private List<Degree> _degrees;
        private Context _context;

        private DegreeSpinnerAdapter() {
            _context = getActivity();
            _degrees = _parentActivity.getAllDegrees();
        }

        @Override
        public int getCount() {
            return _degrees.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            if (position == 0) {
                return null;
            }
            return _degrees.get(position - 1);
        }

        @Override
        public long getItemId(int position) {
            if (position == 0) {
                return 0L;
            }
            return _degrees.get(position - 1).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(_context).inflate(R.layout.ranking_fragment_filter_dialog_spinner_item, null);
            if (position == 0) {
                ((TextView) convertView.findViewById(R.id.list_item)).setText(R.string.filter_all_label);
            } else {
                ((TextView) convertView.findViewById(R.id.list_item)).setText(_context.getString(_degrees.get(position - 1).getNameResource()) + " (" + _context.getString(_degrees.get(position - 1).getUniversityResource()) + ")");
            }

            return convertView;
        }
    }

    public interface RankingFragmentInteractionListener extends DegreeLoader, KnowledgeAreaLoader {

        /**
         * Loads the Chart fragment for this particular score Id. Note that due to
         * the implementation I made, the Degree Score Id and the Degree Combination Id
         * is actually the same, which is nice, but will really make me cry when looking
         * for it in the code somewhere in the future...
         *
         * @param v
         * @param degreeScoreId
         */
        void loadChartFragment(View v, String degreeScoreId);

    }
}
