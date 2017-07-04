package pt.cmg.sweranker.degrees;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import pt.cmg.sweranker.MainActivity;
import pt.cmg.sweranker.MainActivityViewModel;
import pt.cmg.sweranker.ProgressHandler;
import pt.cmg.sweranker.R;

/**
 * Created by Carlos on 25/01/2017.
 */

public class DegreeDetailsFragment extends Fragment implements LifecycleRegistryOwner {


    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    private View _myView;

    private int _degreeId;
    private Degree _degree;

    private MainActivityViewModel _sharedViewModel;

    private DegreeDetailsFragmentInteractionListener _parentActivity;

    private ProgressHandler _handler;
    private ProgressDialog _progressDialog;

    public DegreeDetailsFragment() {
        _handler = new PHandler(Looper.getMainLooper());
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DegreeDetailsFragment.
     */
    public static DegreeDetailsFragment newInstance() {
        return new DegreeDetailsFragment();
    }


    /**
     * Communication Interface used to communicate between this fragment and its parent Activity.
     * <p>
     * Write here any method needed to trigger in the Activity
     */
    public interface DegreeDetailsFragmentInteractionListener {
        /**
         * Loads the class fragment whose item was chosen.
         */
        void loadDegreeClassFragment(View selectedView);
    }

    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof DegreeDetailsFragmentInteractionListener) {
            _parentActivity = (DegreeDetailsFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement DegreeDetailsFragmentInteractionListener");
        }

    }

    @Override
    public void onAttach(Activity parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof DegreeDetailsFragmentInteractionListener) {
            _parentActivity = (DegreeDetailsFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement DegreeDetailsFragmentInteractionListener");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        _sharedViewModel = ViewModelProviders.of((MainActivity) getActivity()).get(MainActivityViewModel.class);
        _degree = _sharedViewModel.getSelectedDegree();
        _degreeId = _degree.getId();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.app_bar_degree_calculate_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_calculate:
                createAndShowFilterDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Creates and displays the Calculation Prompt Dialog.
     * This is a very very important functionality that launches a load of background processing
     * that will crunch the matching data and calculate a score for each degree class, then
     * for each yearly combination and finally for every possible degree combination.
     * <p>
     * TODO: a way to inform the user that processing is taking place, namely using notifications.
     */
    private void createAndShowFilterDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());


        dialogBuilder
                .setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.calculation_dialog_message))
                .setPositiveButton(getResources().getString(R.string.proceed), (dialog, which) -> {
                    // The positive button launches a calculation
                    _sharedViewModel.calculateDegreeScores(_degree, _handler);
//                    new ProgressDialog(getActivity()).show();
                    dialog.cancel();

                })
                .setNegativeButton(getResources().getString(R.string.cancel), (dialog, id) ->
                        dialog.cancel()
                );

        dialogBuilder.create().show();
    }


    public class PHandler extends ProgressHandler {

        public PHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void startProgress() {
            post(() -> _progressDialog.startProgress());
        }

        @Override
        public void startProgressAction(String text, int maxProgressValue) {
            post(() -> _progressDialog.startProgressAction(text, maxProgressValue));

        }

        @Override
        public void updateProgressAction(int progressIncrement) {
            post(() -> _progressDialog.updateProgressAction(progressIncrement));
        }

        @Override
        public void terminateProgress() {
            post(() -> _progressDialog.terminateProgress());
        }

    }

    private class ProgressDialog extends Dialog {

        private TextView updateText;
        private ProgressBar progressBar;

        private ProgressDialog(Context context) {
            super(context);
            this.setContentView(R.layout.dialog);
            Window window = this.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // set the custom dialog components - title, ProgressBar and button
            updateText = (TextView) this.findViewById(R.id.progress_text);
            updateText.setText("Heavy lifting the earth.");
            progressBar = (ProgressBar) this.findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        public void startProgress() {
            this.show();
        }

        public void startProgressAction(String text, int maxProgressValue) {
            updateText.setText(text);
            progressBar.setMax(maxProgressValue);
            progressBar.setProgress(0);
        }

        public void updateProgressAction(int progressIncrement) {
            progressBar.setProgress(progressBar.getProgress() + progressIncrement);
        }

        public void terminateProgress() {
            this.dismiss();
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _myView = inflater.inflate(R.layout.degree_details_fragment, container, false);

        ImageView degreeImage = (ImageView) _myView.findViewById(R.id.degree_image);
        TextView universityName = (TextView) _myView.findViewById(R.id.university_name);
        TextView degreeName = (TextView) _myView.findViewById(R.id.degree_name);
        TextView isDegreeEvaluated = (TextView) _myView.findViewById(R.id.evaluated_status);

        degreeImage.setImageDrawable(this.getResources().getDrawable(_degree.getImageResource(), null));
        universityName.setText(this.getResources().getText(_degree.getUniversityResource()));
        degreeName.setText(this.getResources().getText(_degree.getFullNameResource()));

        if (_sharedViewModel.isDegreeMatched(_degreeId)) {
            isDegreeEvaluated.setText(R.string.matched);
            isDegreeEvaluated.setTextColor((this.getResources().getColor(R.color.materialAffirmative)));
        } else {
            isDegreeEvaluated.setText(R.string.notMatched);
            isDegreeEvaluated.setTextColor((this.getResources().getColor(R.color.materialNegative)));
        }

        ViewPager viewPager = (ViewPager) _myView.findViewById(R.id.degree_viewPager);
        viewPager.setAdapter(new DegreeViewPagerAdapter(this.getActivity(), _degree, (view, degreeClass) -> {
            _sharedViewModel.setSelectedDegreeClass(degreeClass);
            _parentActivity.loadDegreeClassFragment(view);
        }));

        TabLayout tabLayout = (TabLayout) _myView.findViewById(R.id.degree_tabs);
        tabLayout.setupWithViewPager(viewPager);

        _progressDialog = new ProgressDialog(getActivity());

        return _myView;
    }


}
