package pt.cmg.sweranker.ranking;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
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

import pt.cmg.sweranker.MainActivity;
import pt.cmg.sweranker.MainActivityViewModel;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.degrees.Degree;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;

public class ScoresMasterFragment extends Fragment implements LifecycleRegistryOwner {


    private LifecycleRegistry _lifecycle;

    @Override
    public LifecycleRegistry getLifecycle() {
        return _lifecycle;
    }


    /**
     * Communication interface between this fragment and its parent Activity.
     */
    public interface ScoreFragmentInteractionListener {

        /**
         * Loads the Chart fragment for this particular score Id. Note that due to
         * the implementation I made, the Degree Score Id and the Degree Combination Id
         * is actually the same, which is nice, but will really make me cry when looking
         * for it in the code somewhere in the future...
         */
        void loadChartFragment(View selectedView);


        /**
         * Loads the scores comparison fragment with the selected degree combinations while in
         * Action Mode.
         */
        void loadCompareScoresFragment();

    }

    /**
     * This is a reference to the parent activity that this fragment will be attached to on onAttach()
     * It is used to communicate with it.
     */
    private ScoreFragmentInteractionListener _parentActivity;

    private RecyclerView _rankingsGrid;
    private View _myRootView;

    private View _filterDialog;
    private Spinner _filterDialogSelectedKA;
    private Spinner _filterDialogSelectedOrder;
    private Spinner _filterDialogSelectedDegree;
    private Spinner _filterDialogLimitSpinner;

    // This is useful to create visual effects when this fragment enters Action Mode
    private ActionMode _actionMode;

    private TextView _noScoresText;
    private ProgressBar _progressBar;
    private ScoresAndImagesAdapter _adapter;

    private Map<Integer, Degree> _degreesById;

    private MainActivityViewModel _sharedViewModel;

    public ScoresMasterFragment() {
        _lifecycle = new LifecycleRegistry(this);
    }


    public static ScoresMasterFragment newInstance() {
        return new ScoresMasterFragment();
    }

    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof ScoreFragmentInteractionListener) {
            _parentActivity = (ScoreFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement ScoreFragmentInteractionListener");
        }
    }

    // NOTE: this is here because onAttach(Context) was added only on API 23, so as long as Lollipop is min sdk this shall be here
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ScoreFragmentInteractionListener) {
            _parentActivity = (ScoreFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement ScoreFragmentInteractionListener");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _sharedViewModel = ViewModelProviders.of((MainActivity) getActivity()).get(MainActivityViewModel.class);

        List<Degree> degrees = _sharedViewModel.getDegrees().getValue();
        _degreesById = new LinkedHashMap<>();
        for (Degree d : degrees) {
            _degreesById.put(d.getId(), d);
        }
        setHasOptionsMenu(true);

        _lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
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

    /**
     * This will create and show a Dialog that has the purpose of filtering the results.
     * This is a very very important piece of UI as most of the functionality benefits from this filtering.
     * The usage of a simple AlertDialog seems appropriate, at least for now, because what is really important
     * is what happens below the surface... and by that I mean querying the model.
     */
    private void createAndShowFilterDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());


        dialogBuilder.setView(createFilterDialogView())
                .setCustomTitle(getActivity().getLayoutInflater().inflate(R.layout.scores_master_filter_dialog_title, null))
                .setPositiveButton(getResources().getString(R.string.apply), (dialog, which) -> {

                    // In the adapter this is the same
                    int kaId = (int) _filterDialogSelectedKA.getSelectedItemId();
                    // This conversion is inferred by the below Array Adapter.
                    ScoresRepository.Sort order = _filterDialogSelectedOrder.getSelectedItemId() == 0 ? ScoresRepository.Sort.ASCENDING : ScoresRepository.Sort.DESCENDING;
                    // In the Adapter this is also the same
                    int degreeId = (int) _filterDialogSelectedDegree.getSelectedItemId();
                    // Also inferred from below, ain't nobody got time for parameterization.
                    int limit = _filterDialogLimitSpinner.getSelectedItemId() == 3 ? 0 : Integer.valueOf((String) _filterDialogLimitSpinner.getSelectedItem());

                    // The positive button launches a new search with new parameters
                    _progressBar.setVisibility(View.VISIBLE);
                    _rankingsGrid.setVisibility(View.INVISIBLE);

                    // Pretty tricky, this will actually UPDATE A LIVEDATA object that in turn will trigger the filling of the grid with new results
                    _sharedViewModel.applyFilterToScores(kaId, order, degreeId, limit);

                    dialog.cancel();

                })
                .setNegativeButton(getResources().getString(R.string.dismiss), (dialog, id) ->
                        dialog.cancel()
                );

        dialogBuilder.create().show();
    }

    private View createFilterDialogView() {
        _filterDialog = getActivity().getLayoutInflater().inflate(R.layout.scores_master_filter_dialog, null);

        _filterDialogSelectedKA = _filterDialog.findViewById(R.id.knowledge_area_spinner);
        _filterDialogSelectedKA.setAdapter(new KASpinnerAdapter(_sharedViewModel.getKnowledgeAreas().getValue()));

        String[] orders = new String[]{getActivity().getString(R.string.order_ascending), getActivity().getString(R.string.order_descending)};
        _filterDialogSelectedOrder = _filterDialog.findViewById(R.id.order_spinner);
        _filterDialogSelectedOrder.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.scores_master_filter_dialog_spinner_item, orders));

        String[] limits = new String[]{"1", "10", "50", getActivity().getString(R.string.filter_all_label)};
        _filterDialogLimitSpinner = _filterDialog.findViewById(R.id.results_number_spinner);
        _filterDialogLimitSpinner.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.scores_master_filter_dialog_spinner_item, limits));
        _filterDialogLimitSpinner.setSelection(1);

        _filterDialogSelectedDegree = _filterDialog.findViewById(R.id.degree_spinner);
        _filterDialogSelectedDegree.setAdapter(new DegreeSpinnerAdapter(_sharedViewModel.getDegrees().getValue()));

        return _filterDialog;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        _myRootView = inflater.inflate(R.layout.scores_master_fragment, container, false);

        _noScoresText = _myRootView.findViewById(R.id.no_scores_text);
        _progressBar = _myRootView.findViewById(R.id.progress_bar);

        _rankingsGrid = _myRootView.findViewById(R.id.rankings_grid);

        _progressBar.setVisibility(View.VISIBLE);
        _rankingsGrid.setVisibility(View.GONE);

        initialiseScoresGrid();

        _sharedViewModel.getOrderedScoresImages().observe(this, combinationImages -> {
            if (combinationImages == null || combinationImages.isEmpty()) {
                _progressBar.setVisibility(View.GONE);
                _noScoresText.setText(R.string.no_scores_available_yet);
                _noScoresText.setVisibility(View.VISIBLE);
            } else {
                resetScoresGrid(combinationImages);
            }
        });

        return _myRootView;
    }


    private void initialiseScoresGrid() {
        _rankingsGrid.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        _rankingsGrid.addItemDecoration(new ConstantSpacingItemDecorator(getActivity(),
                5,
                ConstantSpacingItemDecorator.Side.ALL_SIDES));
        _rankingsGrid.setItemAnimator(new DefaultItemAnimator());
    }

    private void resetScoresGrid(LinkedHashMap<String, Integer> combinationNameAndImage) {

        _adapter = new ScoresAndImagesAdapter(getActivity(),
                combinationNameAndImage, new ScoresGridListener());

        _rankingsGrid.setAdapter(_adapter);
        _adapter.notifyDataSetChanged();

        _progressBar.setVisibility(View.INVISIBLE);
        _rankingsGrid.setVisibility(View.VISIBLE);
        _noScoresText.setVisibility(View.INVISIBLE);
    }

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

    private class ScoresGridListener implements ScoresAndImagesAdapter.OnScoresGridAdapterListener {

        @Override
        public void loadDegreeChartsFragment(View rootView, String degreeCombinationId) {
            _sharedViewModel.setSelectedDegreeCombinationId(degreeCombinationId);
            _parentActivity.loadChartFragment(rootView);
        }

        @Override
        public void startActionMode(View selectedCardView, String degreeCombinationId) {
            _actionMode = ((MainActivity) getActivity()).startSupportActionMode(new ScoresGridMultiSelectCallback());
            _actionMode.setTitle(getResources().getString(R.string.action_mode_title));
            _actionMode.getMenu().getItem(0).setVisible(false);
        }

        @Override
        public void onItemSelectedInActionMode(View selectedCardView, int numberOfSelectedItems) {
            _actionMode.setTitle(getResources().getString(R.string.action_mode_title_complete));
            _actionMode.getMenu().getItem(0).setVisible(true);
        }

        @Override
        public void onItemUnselectedInActionMode(View selectedCardView, int numberOfSelectedItems) {
            if (numberOfSelectedItems == 0) {
                _actionMode.setTitle("");
                _actionMode.finish();
            } else {
                _actionMode.setTitle(getResources().getString(R.string.action_mode_title));
                _actionMode.getMenu().getItem(0).setVisible(false);
            }
        }

    }

    private class ScoresGridMultiSelectCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.multi_select_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.erase_selection:
                    _adapter.clearSelection();
                    mode.finish();
                    return true;
                case R.id.apply_selection:
                    _sharedViewModel.setMultiSelectedDereeCombinationIds(_adapter.getSelectedDegreeIds());
                    _parentActivity.loadCompareScoresFragment();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            _adapter.clearSelection();
            _actionMode = null;
        }
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
        _sharedViewModel.getOrderedScoresImages().removeObservers(this);
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
        _parentActivity = null;
    }


    /**
     * This is a simple Adapter for a Spinner. It basically shows the Knowledge Area names.
     * I didn't want to use the standard ArrayAdapter.
     */
    private class KASpinnerAdapter extends BaseAdapter {

        private List<KnowledgeArea> _knowledgeAreas;
        private Context _context;

        private KASpinnerAdapter(List<KnowledgeArea> knowledgeAreas) {
            _context = getActivity();
            _knowledgeAreas = knowledgeAreas;
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
            convertView = LayoutInflater.from(_context).inflate(R.layout.scores_master_filter_dialog_spinner_item, null);
            ((TextView) convertView.findViewById(R.id.list_item)).setText(_context.getString(_knowledgeAreas.get(position).getNameResource()));
            return convertView;
        }
    }


    /**
     * This is a simple Adapter for a Spinner. It basically shows the Degree names.
     * I didn't want to use the standard ArrayAdapter.
     */
    private class DegreeSpinnerAdapter extends BaseAdapter {
        private List<Degree> _degrees;
        private Context _context;

        private DegreeSpinnerAdapter(List<Degree> degrees) {
            _context = getActivity();
            _degrees = degrees;
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
            convertView = LayoutInflater.from(_context).inflate(R.layout.scores_master_filter_dialog_spinner_item, null);
            if (position == 0) {
                ((TextView) convertView.findViewById(R.id.list_item)).setText(R.string.filter_all_label);
            } else {
                ((TextView) convertView.findViewById(R.id.list_item)).setText(_context.getString(_degrees.get(position - 1).getNameResource()) + " (" + _context.getString(_degrees.get(position - 1).getUniversityResource()) + ")");
            }

            return convertView;
        }
    }

}
