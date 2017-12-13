package pt.cmg.sweranker;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import pt.cmg.sweranker.degrees.Degree;
import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.degrees.DegreeClassFragment;
import pt.cmg.sweranker.degrees.DegreeDetailsFragment;
import pt.cmg.sweranker.degrees.DegreeTopicMatcherFragment;
import pt.cmg.sweranker.degrees.DegreesMasterFragment;
import pt.cmg.sweranker.dependencies.DaggerMainActivityComponent;
import pt.cmg.sweranker.dependencies.MainActivityComponent;
import pt.cmg.sweranker.dependencies.MainActivityModule;
import pt.cmg.sweranker.ranking.MultiScoreDetailedChartFragment;
import pt.cmg.sweranker.ranking.ScoreDetailedChartFragment;
import pt.cmg.sweranker.ranking.ScoresMasterFragment;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.SwebokDetailedFragment;
import pt.cmg.sweranker.swebok.SwebokMasterFragment;
import pt.cmg.sweranker.ui.OnStartTransitionListener;
import pt.cmg.sweranker.ui.UXUtils;

public class MainActivity extends AppCompatActivity implements
        LifecycleRegistryOwner,
        NavigationView.OnNavigationItemSelectedListener,
        SwebokMasterFragment.OnSwebokFragmentInteractionListener,
        DegreesMasterFragment.DegreesFragmentInteractionListener,
        DegreeDetailsFragment.DegreeDetailsFragmentInteractionListener,
        DegreeClassFragment.DegreeClassFragmentInteractionListener,
        ScoresMasterFragment.ScoreFragmentInteractionListener,
        ScoreDetailedChartFragment.OnScoreDetailedChartFragmentInteractionListener {


    private Toolbar _toolbar;

    private MainActivityComponent _component;

    @Inject
    ViewModelProvider.Factory viewmodelFactory;

    private MainActivityViewModel _viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.global_layout);

        _component = DaggerMainActivityComponent.builder()
                .applicationComponent(SweRankerApplication.get(this).getComponent())
                .mainActivityModule(new MainActivityModule(this))
                .build();
        _component.inject(this);

        _viewModel = ViewModelProviders.of(this, viewmodelFactory).get(MainActivityViewModel.class);
        _viewModel.init();
        resetToolbar();
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getFragmentManager().getBackStackEntryCount() == 0) {
                    resetToolbar();
                }
            }
        });
        initialiseRandomScoreView();

    }

    /**
     * Resets the toolbar for this activity to its initial state.
     * This can be used on onCreate to initialise the toolbar or anytime, in case the toolbar was changed
     * by any reason.
     */
    private void resetToolbar() {

        _toolbar = findViewById(R.id.toolbar);

        // this sets the title. Although it is standard behaviour, in case the title was changed somehow, this is needed
        // to return to the original title.
        _toolbar.setTitle(getResources().getString(this.getApplicationInfo().labelRes));
        setSupportActionBar(_toolbar);

        // Also standard, also (probably) needed to reset.
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        DrawerLayout drawer = findViewById(R.id.nav_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, _toolbar, R.string.openDrawer, R.string.closeDrawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * This serves as the current first screen of the App.
     * It is basically the Score Details Fragment in a Random Mode.
     * TODO: I should probably chose a different first screen to the app...
     */
    private void initialiseRandomScoreView() {
        _viewModel.setRandomlySelectedDegreeScore();

        Fragment randomScoreFragment = ScoreDetailedChartFragment.newInstance();

        _toolbar.setTitle(getString(R.string.todays_degree));

        Transition exitTransition = new Fade(Fade.OUT);
        exitTransition.setDuration(100);
        randomScoreFragment.setExitTransition(exitTransition);
        exitTransition.addListener(new OnStartTransitionListener() {
            @Override
            public void onStartTransition(Transition transition) {
                // Resets default title
                _toolbar.setTitle(getResources().getString(getApplicationInfo().labelRes));
            }
        });


        getFragmentManager().popBackStackImmediate();
        getFragmentManager()
                .beginTransaction()
                .add(R.id.content_area, randomScoreFragment, "RandomScore")
                .commit();
    }

    /**
     * Transforms the standard toolbar into a much loved and simpler "BACK" toolbar with a custom name, if needed.
     * This is basically the toolbar but with a back arrow instead of the usual menu.
     *
     * @param toolbarTitle The title to be displayed in the Back-only toolbar
     */
    private void changeToolbarIntoBackButton(String toolbarTitle) {

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _toolbar.setNavigationOnClickListener(view -> getFragmentManager().popBackStack());
        if (!toolbarTitle.isEmpty()) {
            _toolbar.setTitle(toolbarTitle);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.swebok_nav) {
            // Careful with his, it is here because of the animations on KA details.
            // When pressed the menu and selected one item the animations would not run.
            getFragmentManager().popBackStackImmediate();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_area, SwebokMasterFragment.newInstance(), "Swebok")
                    .commit();
        } else if (id == R.id.curricula_nav) {
            getFragmentManager().popBackStackImmediate();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_area, DegreesMasterFragment.newInstance(), "Degrees")
                    .commit();
        } else if (id == R.id.scores_nav) {
            getFragmentManager().popBackStackImmediate();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_area, ScoresMasterFragment.newInstance(), "Scores")
                    .commit();
        }

        DrawerLayout drawer = findViewById(R.id.nav_drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void loadDetailedKnowledgeAreaFragment(View knowledgeAreaCardView, int colour) {
        ImageView image = knowledgeAreaCardView.findViewById(R.id.ka_image);

        // Gets the colour that will be changed between fragments, the source, which is the KA decorative colour
        int imageBackgroundColour = colour;
        // ... and the original colours of the Action Bars and Status Bar so we can return back to the original colours
        int actionBarOriginalColour = _toolbar.getSolidColor();
        int statusBarOriginalColour = this.getWindow().getStatusBarColor();

        long transitionDuration = 500;

        Fragment kaDetailsFragment = SwebokDetailedFragment.newInstance();
        KnowledgeArea knowledgeArea = _viewModel.getSelectedKnowledgeArea();
        Activity myActivity = this;


        /**
         * TODO: DAMN ANDROID 8 - the shared elements transitions stopped working, I don't know why, but it just doesn't trigger the Return Shared Elemetn Transition...
         * TODO: I will have to review this... for now I just changed to a simpler whole fragment transition...
         */
//        Transition imageEnterTransition = createImageEnterSharedElementTransition(kaDetailsFragment, transitionDuration);
        // Very important! Adding a listener in order to change the Action Bar colour and the Status Bar colour when this transition finishes.
//        imageEnterTransition.addListener(new OnEndTransitionListener() {
//            @Override
//            public void onEndTransition(Transition transition) {
//                UXUtils.animateActionBarColourChange(_toolbar, imageBackgroundColour, 0, 0);
//                UXUtils.animateStatusBarColourChange(myActivity, imageBackgroundColour, 0, 0);
//                changeToolbarIntoBackButton(getResources().getString(knowledgeArea.getNameResource()));
//            }
//        });


//        Transition sharedImageExitTransition = createImageExitSharedElementTransition(kaDetailsFragment, transitionDuration);
        // Very important! Adding a listener in order to change the Action Bar colour and the Status Bar colour when this transition starts.
//        sharedImageExitTransition.addListener(new OnStartTransitionListener() {
//            @Override
//            public void onStartTransition(Transition transition) {
//                UXUtils.animateStatusBarColourChange(myActivity, statusBarOriginalColour, 0, 0);
//                UXUtils.animateActionBarColourChange(_toolbar, actionBarOriginalColour, 0, 0);
//                resetToolbar();
//            }
//        });

        // This just creates a transition for the rest of the content, i.e. not shared.
        Transition enterTransition = new Slide(Gravity.RIGHT);
        enterTransition.setDuration(transitionDuration);
        kaDetailsFragment.setEnterTransition(enterTransition);
        enterTransition.addListener(new OnStartTransitionListener() {
            @Override
            public void onStartTransition(Transition transition) {
                UXUtils.animateActionBarColourChange(_toolbar, imageBackgroundColour, 0, 0);
                UXUtils.animateStatusBarColourChange(myActivity, imageBackgroundColour, 0, 0);
                changeToolbarIntoBackButton(getResources().getString(knowledgeArea.getNameResource()));
            }
        });

        // And when the user presses Back on the toolbar I use a transition listener to change the toolbar back to its original state. Neat.
        Transition exitTransition = new Slide(Gravity.RIGHT);
        exitTransition.setDuration(transitionDuration);
        kaDetailsFragment.setReturnTransition(exitTransition);
        exitTransition.addListener(new OnStartTransitionListener() {
            @Override
            public void onStartTransition(Transition transition) {
                UXUtils.animateStatusBarColourChange(myActivity, statusBarOriginalColour, 0, 0);
                UXUtils.animateActionBarColourChange(_toolbar, actionBarOriginalColour, 0, 0);
            }
        });

        getFragmentManager()
                .beginTransaction()
                .addSharedElement(image, "ka_image")
                .replace(R.id.content_area, kaDetailsFragment, "KADetail")
                .addToBackStack(null)
                .commit();
    }

    /**
     * Creates and sets a shared element transition from source fragment {@link SwebokMasterFragment} to the target detailed fragment {@link SwebokDetailedFragment}.
     * The shared element is the KA decorative image and it will be set on the fragment transaction.
     * This function only creates the Transition animation for the image.
     */
//    private Transition createImageEnterSharedElementTransition(Fragment targetFragment, long transitionDuration) {
//
//        Transition sharedImageEnterTransition = new ImageSizeAndPlaceTransition();
//        sharedImageEnterTransition.setDuration(transitionDuration);
//        targetFragment.setSharedElementEnterTransition(sharedImageEnterTransition);
//        return sharedImageEnterTransition;
//
//    }

    /**
     * Creates and sets a shared element transition from target fragment {@link SwebokDetailedFragment} BACK TO the source fragment {@link SwebokMasterFragment}.
     * The shared element is the KA decorative image and it will be set on the fragment transaction.
     * This function only creates the Transition animation for the image.
     */
//    private Transition createImageExitSharedElementTransition(Fragment targetFragment, long transitionDuration) {
//
//        Transition sharedImageExitTransition = new ImageSizeAndPlaceTransition();
//        sharedImageExitTransition.setDuration(transitionDuration);
//        targetFragment.setSharedElementReturnTransition(sharedImageExitTransition);
//        return sharedImageExitTransition;
//    }
    @Override
    public void loadDetailedDegreeFragment(View degreeCard) {

        ImageView image = degreeCard.findViewById(R.id.university_image);
        TextView universityName = degreeCard.findViewById(R.id.university_name);
        TextView degreeName = degreeCard.findViewById(R.id.degree_name);

        Degree degree = _viewModel.getSelectedDegree();


        long transitionDuration = 500;

        Fragment degreeDetailsFragment = DegreeDetailsFragment.newInstance();

        // This is important. I used a simples transition but the real magic is that I change the
        // toolbar into a back button toolbar so that it is easier to navigate.
        Transition enterTransition = new Slide(Gravity.RIGHT);
        enterTransition.setDuration(transitionDuration);
        degreeDetailsFragment.setEnterTransition(enterTransition);
        enterTransition.addListener(new OnStartTransitionListener() {
            @Override
            public void onStartTransition(Transition transition) {
                changeToolbarIntoBackButton(getResources().getString(degree.getNameResource()));
            }
        });


        // TODO: as transições shared não funcionam muito bem com o Android 8. Atenção quando rever isto
        Transition exitTransition = new Slide(Gravity.RIGHT);
        exitTransition.setDuration(transitionDuration);
        degreeDetailsFragment.setReturnTransition(exitTransition);

        getFragmentManager()
                .beginTransaction()
                .addSharedElement(image, "degree_image")
                .addSharedElement(universityName, "university_name")
                .addSharedElement(degreeName, "degree_name")
                .replace(R.id.content_area, degreeDetailsFragment, "DegreeDetail")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void loadDegreeClassFragment(View selectedView) {

        Fragment degreeClassFragment = DegreeClassFragment.newInstance();

        Degree degree = _viewModel.getSelectedDegree();
        DegreeClass degreeClass = _viewModel.getSelectedDegreeClass();

        // This is important. I used a simples transition but the real magic is that I change the
        // toolbar into a back button toolbar so that it is easier to navigate.
        Transition enterTransition = new Slide(Gravity.RIGHT);
        enterTransition.setDuration(400);
        degreeClassFragment.setEnterTransition(enterTransition);
        enterTransition.addListener(new OnStartTransitionListener() {
            @Override
            public void onStartTransition(Transition transition) {
                changeToolbarIntoBackButton(getResources().getString(degreeClass.getNameResource()));
            }
        });

        // Since I am navigating back to the degree, I will change again the toolbar to its name
        Transition exitTransition = new Slide(Gravity.RIGHT);
        exitTransition.setDuration(400);
        degreeClassFragment.setReturnTransition(exitTransition);
        exitTransition.addListener(new OnStartTransitionListener() {
            @Override
            public void onStartTransition(Transition transition) {
                // When we get back, we get back to degree so it is too soon to reset the toolbar
                changeToolbarIntoBackButton(getResources().getString(degree.getNameResource()));
            }
        });


        getFragmentManager()
                .beginTransaction()
                .add(R.id.content_area, degreeClassFragment)
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void loadDegreeTopicMatcherFragment() {

        Fragment classEvaluator = DegreeTopicMatcherFragment.newInstance();

        DegreeClass degreeClass = _viewModel.getSelectedDegreeClass();

        // This is important. I used a simples transition but the real magic is that I change the
        // toolbar into a back button toolbar so that it is easier to navigate.
        Transition enterTransition = new Slide(Gravity.RIGHT);
        enterTransition.setDuration(400);
        classEvaluator.setEnterTransition(enterTransition);
        enterTransition.addListener(new OnStartTransitionListener() {
            @Override
            public void onStartTransition(Transition transition) {
                changeToolbarIntoBackButton(getResources().getString(degreeClass.getNameResource()));
            }
        });

        Transition exitTransition = new Slide(Gravity.RIGHT);
        exitTransition.setDuration(400);
        classEvaluator.setReturnTransition(exitTransition);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.content_area, classEvaluator)
                .addToBackStack(null)
                .commit();

    }


    @Override
    public void loadChartFragment(View v) {

        Fragment chartFragment = ScoreDetailedChartFragment.newInstance();

        // This is important. I used a simples transition but the real magic is that I change the
        // toolbar into a back button toolbar so that it is easier to navigate.
        Transition enterTransition = new Slide(Gravity.RIGHT);
        enterTransition.setDuration(400);
        chartFragment.setEnterTransition(enterTransition);
        enterTransition.addListener(new OnStartTransitionListener() {
            @Override
            public void onStartTransition(Transition transition) {
                changeToolbarIntoBackButton("Scores");
            }
        });

        Transition exitTransition = new Slide(Gravity.RIGHT);
        exitTransition.setDuration(300);
        chartFragment.setReturnTransition(exitTransition);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.content_area, chartFragment, "ChartFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void loadCompareScoresFragment() {

        Fragment chartFragment = MultiScoreDetailedChartFragment.newInstance();

        // This is important. I used a simples transition but the real magic is that I change the
        // toolbar into a back button toolbar so that it is easier to navigate.
        Transition enterTransition = new Slide(Gravity.RIGHT);
        enterTransition.setDuration(400);
        chartFragment.setEnterTransition(enterTransition);
        enterTransition.addListener(new OnStartTransitionListener() {
            @Override
            public void onStartTransition(Transition transition) {
                changeToolbarIntoBackButton("Comparator");
            }
        });

        Transition exitTransition = new Slide(Gravity.RIGHT);
        exitTransition.setDuration(300);
        chartFragment.setReturnTransition(exitTransition);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content_area, chartFragment, "MultiChartFragment")
                .addToBackStack(null)
                .commit();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }
}
