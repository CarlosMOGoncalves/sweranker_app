package pt.cmg.sweranker.degrees;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmObject;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.ranking.AnnualClassCombination;
import pt.cmg.sweranker.ranking.CombinationUtils;
import pt.cmg.sweranker.ranking.DegreeClassCombination;
import pt.cmg.sweranker.ranking.combinationstrategies.ClassCombinationStrategy;

/**
 * Created by Carlos on 25/01/2017.
 */

public class DegreeDetailsFragment extends Fragment {

    private static final String DEGREE_ID = "degree_id";

    private View _myView;

    private int _degreeId;
    private Degree _degree;

    private DegreeDetailsFragmentInteractionListener _parentActivity;

    public DegreeDetailsFragment() {

    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DegreeDetailsFragment.
     */
    public static DegreeDetailsFragment newInstance(int degreeId) {
        DegreeDetailsFragment fragment = new DegreeDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(DEGREE_ID, degreeId);
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Communication Interface used to communicate between this fragment and its parent Activity.
     * <p>
     * Write here any method needed to trigger in the Activity
     */
    public interface DegreeDetailsFragmentInteractionListener extends DegreeLoader, DegreeMatcherLoader {


        /**
         * Loads the class fragment whose item was chosen.
         *
         * @param degreeId
         * @param degreeClassId
         */
        void loadDegreeClassFragment(int degreeId, String degreeClassId);
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

    // NOTE: this is here because onAttach(Context) was added only on API 23, so as long as Lollipop is min sdk this shall be here
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DegreeDetailsFragmentInteractionListener) {
            _parentActivity = (DegreeDetailsFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement DegreeDetailsFragmentInteractionListener");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            _degreeId = getArguments().getInt(DEGREE_ID);
            _degree = _parentActivity.loadDegree(_degreeId);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _myView = inflater.inflate(R.layout.degree_details_fragment, container, false);

        ImageView degreeImage = (ImageView) _myView.findViewById(R.id.degree_image);
        TextView universityName = (TextView) _myView.findViewById(R.id.university_name);
        TextView degreeName = (TextView) _myView.findViewById(R.id.degree_name);
        TextView isDegreeEvaluated = (TextView) _myView.findViewById(R.id.evaluated_status);
        Button calculateScoreButton = (Button) _myView.findViewById(R.id.init_calculation);

        degreeImage.setImageDrawable(this.getResources().getDrawable(_degree.getImageResource(), null));
        universityName.setText(this.getResources().getText(_degree.getUniversityResource()));
        degreeName.setText(this.getResources().getText(_degree.getFullNameResource()));

        if (_parentActivity.isDegreeMatched(_degreeId)) {
            isDegreeEvaluated.setText(R.string.matched);
            isDegreeEvaluated.setTextColor((this.getResources().getColor(R.color.materialAffirmative)));
        } else {
            isDegreeEvaluated.setText(R.string.notMatched);
            isDegreeEvaluated.setTextColor((this.getResources().getColor(R.color.materialNegative)));
        }

        calculateScoreButton.setOnClickListener(view -> new YearlyRankingCalculator().execute());

        ViewPager viewPager = (ViewPager) _myView.findViewById(R.id.degree_viewPager);
        viewPager.setAdapter(new DegreeViewPagerAdapter(this.getActivity(), _degree, new DegreeViewPagerAdapter.OnDegreeClassItemSelected() {
            @Override
            public void onDegreeClassClicked(int degreeId, String degreeClassId) {
                _parentActivity.loadDegreeClassFragment(degreeId, degreeClassId);
            }
        }));

        TabLayout tabLayout = (TabLayout) _myView.findViewById(R.id.degree_tabs);
        tabLayout.setupWithViewPager(viewPager);

        return _myView;
    }


    private class YearlyRankingCalculator extends AsyncTask<Void, Void, Void> {


        private Realm databaseConnection;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            databaseConnection = Realm.getDefaultInstance();
        }

        @Override
        protected Void doInBackground(Void... params) {


            List<AnnualClassCombination> annualCombinations = calculateAnnualCombinations(_degree);

            saveObjectsByBatch(databaseConnection, annualCombinations);

            List<AnnualClassCombination> savedAnnualCombinations = databaseConnection.where(AnnualClassCombination.class).equalTo("degreeId", _degreeId).findAll();

            List<DegreeClassCombination> allDegreeCombinations = calculateAllDegreeClassCombinations(_degree, savedAnnualCombinations);

            saveObjectsByBatch(databaseConnection, allDegreeCombinations);

            List<DegreeClassCombination> savedCombination = databaseConnection.where(DegreeClassCombination.class).equalTo("degreeId", _degreeId).findAll();

            return null;

        }

        private List<DegreeClassCombination> calculateAllDegreeClassCombinations(Degree degree, List<AnnualClassCombination> annualCombinations) {
            return CombinationUtils.generateAllDegreeCombinations(degree, annualCombinations);
        }


        private List<AnnualClassCombination> calculateAnnualCombinations(Degree degree) {
            List<AnnualClassCombination> annualCombinations = new ArrayList<>();

            for (Map.Entry<Integer, ClassCombinationStrategy> classCombinationStrategy : degree.getClassCombinationStrategies().entrySet()) {

                Integer yearOfDegree = classCombinationStrategy.getKey();
                ClassCombinationStrategy combinationStrategy = classCombinationStrategy.getValue();

                // Here, using each year's strategy to unfold all possible combinations for this year
                annualCombinations.addAll(combinationStrategy.getAnnualClassCombinations(degree.getClasses().get(yearOfDegree)));

            }

            // Last pass to set the degree id
            for (AnnualClassCombination annualCombination : annualCombinations) {
                annualCombination.setDegreeId(_degreeId);
            }

            return annualCombinations;
        }


        private <E extends RealmObject> void saveObjectsByBatch(Realm realmInstance, List<E> realmObjects) {
            int batchSize = 10000;
            ListIterator<E> iterator = realmObjects.listIterator(realmObjects.size());
            List<E> batch = new ArrayList<>(batchSize);

            while (iterator.hasPrevious()) {

                // Add 10000 to batch
                for (int i = 0; i < batchSize && iterator.hasPrevious(); i++) {
                    batch.add(iterator.previous());
                    iterator.set(null);
                }

                realmInstance.executeTransaction(realm -> realm.copyToRealmOrUpdate(batch));

                // This part is just a small sleep to give some time to trigger the GC and clean the mess it is creating.
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                batch.clear();
            }


        }

        /**
         * Uses all combinations of all the years that compose a degree to further unfold the class combinations to ALL possible ones.
         * This is heavy processing method, a good multi-threading strategy must be developed.
         * <br/>
         * Returns a Map where:
         * <p>
         * Keys -> A generated unique degree combination ID , Values -> a single complete combination of classes of all years that compose the degree
         * </p>
         *
         * @param degreeCombinationsByYear
         * @return
         */
        private Map<Integer, DegreeClassCombination> calculateAllDegreeClassCombinations(Degree degree, Map<Integer, List<AnnualClassCombination>> degreeCombinationsByYear) {
            return CombinationUtils.generateAndSaveAllDegreeCombinations(degree, degreeCombinationsByYear);
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            databaseConnection.close();
        }
    }


}
