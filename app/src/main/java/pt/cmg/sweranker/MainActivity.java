package pt.cmg.sweranker;

import android.app.Activity;
import android.app.Fragment;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import pt.cmg.sweranker.degrees.Degree;
import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.degrees.DegreeClassFragment;
import pt.cmg.sweranker.degrees.DegreeClassMatch;
import pt.cmg.sweranker.degrees.DegreeDetailsFragment;
import pt.cmg.sweranker.degrees.DegreeTopicMatcherFragment;
import pt.cmg.sweranker.degrees.DegreesLoaderService;
import pt.cmg.sweranker.degrees.DegreesMasterFragment;
import pt.cmg.sweranker.dependencies.DaggerMainActivityComponent;
import pt.cmg.sweranker.dependencies.MainActivityComponent;
import pt.cmg.sweranker.dependencies.MainActivityModule;
import pt.cmg.sweranker.ranking.MultiScoreChartFragment;
import pt.cmg.sweranker.ranking.RankingService;
import pt.cmg.sweranker.ranking.ScoreChartFragment;
import pt.cmg.sweranker.ranking.ScoresMasterFragment;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;
import pt.cmg.sweranker.swebok.SwebokDetailedFragment;
import pt.cmg.sweranker.swebok.SwebokLoaderService;
import pt.cmg.sweranker.swebok.SwebokMasterFragment;
import pt.cmg.sweranker.ui.ImageSizeAndPlaceTransition;
import pt.cmg.sweranker.ui.OnEndTransitionListener;
import pt.cmg.sweranker.ui.OnStartTransitionListener;
import pt.cmg.sweranker.ui.UXUtils;

public class MainActivity extends AppCompatActivity implements
        LifecycleRegistryOwner,
        NavigationView.OnNavigationItemSelectedListener,
        SwebokMasterFragment.OnSwebokFragmentInteractionListener,
        SwebokDetailedFragment.OnKaDetailsFragmentInteractionListener,
        DegreesMasterFragment.DegreesFragmentInteractionListener,
        DegreeDetailsFragment.DegreeDetailsFragmentInteractionListener,
        DegreeClassFragment.DegreeClassFragmentInteractionListener,
        DegreeTopicMatcherFragment.OnDegreeMatcherFragmentInteraction,
        ScoresMasterFragment.RankingFragmentInteractionListener,
        ScoreChartFragment.OnScoreChartFragmentInteractionListener,
        MultiScoreChartFragment.OnMultiScoreChartFragmentInteractionListener {


    private SwebokLoaderService _swebokService;
    private DegreesLoaderService _degreesService;
    private RankingService _rankingService;
    boolean _isSwebokServiceBound = false;
    boolean _isDegreesServiceBound = false;
    boolean _isRankingServiceBound = false;

    private Toolbar _toolbar;
    private List<KnowledgeArea> _tempKnowledgeAreas;
    private List<Degree> _tempDegrees;


    private ServiceConnection _swebokServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SwebokLoaderService.SwebokLoaderBinder binder = (SwebokLoaderService.SwebokLoaderBinder) service;
            _swebokService = binder.getService();
            _isSwebokServiceBound = true;
            _tempKnowledgeAreas = _swebokService.getKnowledgeAreas();
            startDegreesService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _swebokService = null;
            _isSwebokServiceBound = false;
        }
    };

    private ServiceConnection _degreesServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DegreesLoaderService.DegreesLoaderBinder binder = (DegreesLoaderService.DegreesLoaderBinder) service;
            _degreesService = binder.getService();
            _isDegreesServiceBound = true;
            _tempDegrees = _degreesService.getDegrees();
            startRankingService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _degreesService = null;
            _isDegreesServiceBound = false;
        }
    };

    private ServiceConnection _rankingServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RankingService.RankingBinder binder = (RankingService.RankingBinder) service;
            _rankingService = binder.getService();
            _isRankingServiceBound = true;
            _rankingService.setKnowledgeAreas(_tempKnowledgeAreas);
            _rankingService.setDegreeClasses(_tempDegrees);
            _tempKnowledgeAreas = null;
            _tempDegrees = null;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _rankingService = null;
            _isRankingServiceBound = false;
        }
    };


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


    }

    /**
     * Resets the toolbar for this activity to its initial state.
     * This can be used on onCreate to initialise the toolbar or anytime, in case the toolbar was changed
     * by any reason.
     */
    private void resetToolbar() {

        _toolbar = (Toolbar) findViewById(R.id.toolbar);

        // this sets the title. Although it is standard behaviour, in case the title was changed somehow, this is needed
        // to return to the original title.
        _toolbar.setTitle(getResources().getString(this.getApplicationInfo().labelRes));
        setSupportActionBar(_toolbar);

        // Also standard, also (probably) needed to reset.
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.nav_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, _toolbar, R.string.openDrawer, R.string.closeDrawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Transforms the standard toolbar into a much loved and simpler "BACK" toolbar with a custom name, if needed.
     * This is basically the toolbar but with a back arrow instead of the usual menu.
     *
     * @param toolbarTitle The title to be displayed in the Back-only toolbar
     */
    private void changeToolbarIntoBackButton(String toolbarTitle) {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> getFragmentManager().popBackStack());
        if (!toolbarTitle.isEmpty()) {
            toolbar.setTitle(toolbarTitle);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
//        startSwebokService();
    }

    private void startSwebokService() {
        Intent startSwebokServiceIntent = new Intent(this, SwebokLoaderService.class);
        startService(startSwebokServiceIntent);
        bindService(startSwebokServiceIntent, _swebokServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void startDegreesService() {
        Intent startDegreesServiceIntent = new Intent(this, DegreesLoaderService.class);
        startService(startDegreesServiceIntent);
        bindService(startDegreesServiceIntent, _degreesServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void startRankingService() {
        Intent startDegreeMatcherService = new Intent(this, RankingService.class);
        startService(startDegreeMatcherService);
        bindService(startDegreeMatcherService, _rankingServiceConnection, Context.BIND_AUTO_CREATE);
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
            getFragmentManager().beginTransaction().replace(R.id.content_area, SwebokMasterFragment.newInstance(), "Swebok").commit();
        } else if (id == R.id.curricula_nav) {
            getFragmentManager().popBackStackImmediate();
            getFragmentManager().beginTransaction().replace(R.id.content_area, DegreesMasterFragment.newInstance(), "Degrees").commit();
        } else if (id == R.id.rankings_nav) {
            getFragmentManager().popBackStackImmediate();
            getFragmentManager().beginTransaction().replace(R.id.content_area, ScoresMasterFragment.newInstance(), "Degrees").commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.nav_drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public List<KnowledgeArea> getKnowledgeAreas() {
        List<KnowledgeArea> kas = new ArrayList<>();
        if (_isSwebokServiceBound) {
            kas = _swebokService.getKnowledgeAreas();
        }
        return kas;
    }

    @Override
    public void loadDetailedKnowledgeAreaFragment(View knowledgeAreaCardView) {
        ImageView image = (ImageView) knowledgeAreaCardView.findViewById(R.id.ka_image);

        // Gets the colour that will be changed between fragments, the source, which is the KA decorative colour
        int imageBackgroundColour = ((ColorDrawable) image.getBackground()).getColor();
        // ... and the original colours of the Action Bars and Status Bar so we can return back to the original colours
        int actionBarOriginalColour = _toolbar.getSolidColor();
        int statusBarOriginalColour = this.getWindow().getStatusBarColor();

        long transitionDuration = 500;

        Fragment kaDetailsFragment = SwebokDetailedFragment.newInstance();
        KnowledgeArea knowledgeArea = _viewModel.getSelectedKnowledgeArea();
        Activity myActivity = this;

        Transition imageEnterTransition = createImageEnterSharedElementTransition(kaDetailsFragment, transitionDuration);

        // Very important! Adding a listener in order to change the Action Bar colour and the Status Bar colour when this transition finishes.
        imageEnterTransition.addListener(new OnEndTransitionListener() {
            @Override
            public void onEndTransition(Transition transition) {
                UXUtils.animateActionBarColourChange(_toolbar, imageBackgroundColour, 0, 0);
                UXUtils.animateStatusBarColourChange(myActivity, imageBackgroundColour, 0, 0);
                changeToolbarIntoBackButton(getResources().getString(knowledgeArea.getNameResource()));
            }
        });


        Transition sharedImageExitTransition = createImageExitSharedElementTransition(kaDetailsFragment, transitionDuration);
        // Very important! Adding a listener in order to change the Action Bar colour and the Status Bar colour when this transition starts.
        sharedImageExitTransition.addListener(new OnStartTransitionListener() {
            @Override
            public void onStartTransition(Transition transition) {
                UXUtils.animateStatusBarColourChange(myActivity, statusBarOriginalColour, 0, 0);
                UXUtils.animateActionBarColourChange(_toolbar, actionBarOriginalColour, 0, 0);
                resetToolbar();
            }
        });

        // This just creates a transition for the rest of the content, i.e. not shared.
        Transition contentTransition = new Slide();
        contentTransition.setDuration(transitionDuration);
        kaDetailsFragment.setEnterTransition(contentTransition);
        kaDetailsFragment.setExitTransition(contentTransition);

        getFragmentManager().beginTransaction().addSharedElement(image, "ka_image").replace(R.id.content_area, kaDetailsFragment, "KADetail").addToBackStack(null).commit();
    }

    /**
     * Creates and sets a shared element transition from source fragment {@link SwebokMasterFragment} to the target detailed fragment {@link SwebokDetailedFragment}.
     * The shared element is the KA decorative image and it will be set on the fragment transaction.
     * This function only creates the Transition animation for the image.
     */
    private Transition createImageEnterSharedElementTransition(Fragment targetFragment, long transitionDuration) {

        Transition sharedImageEnterTransition = new ImageSizeAndPlaceTransition();
        sharedImageEnterTransition.setDuration(transitionDuration);
        targetFragment.setSharedElementEnterTransition(sharedImageEnterTransition);
        return sharedImageEnterTransition;

    }

    /**
     * Creates and sets a shared element transition from target fragment {@link SwebokDetailedFragment} BACK TO the source fragment {@link SwebokMasterFragment}.
     * The shared element is the KA decorative image and it will be set on the fragment transaction.
     * This function only creates the Transition animation for the image.
     */
    private Transition createImageExitSharedElementTransition(Fragment targetFragment, long transitionDuration) {

        Transition sharedImageExitTransition = new ImageSizeAndPlaceTransition();
        sharedImageExitTransition.setDuration(transitionDuration);
        targetFragment.setSharedElementReturnTransition(sharedImageExitTransition);
        return sharedImageExitTransition;
    }


    @Override
    public KnowledgeArea getKnowledgeArea(int knowledgeAreaIdToLoad) {
        KnowledgeArea ka = new KnowledgeArea();
        if (_isSwebokServiceBound) {
            ka = _swebokService.getKnowledgeArea(knowledgeAreaIdToLoad);
        }
        return ka;
    }

    @Override
    public List<KnowledgeAreaTopic> getAllKnowledgeAreaTopics() {
        List<KnowledgeAreaTopic> allTopics = new ArrayList<>();
        if (_isSwebokServiceBound) {
            allTopics = _swebokService.getKnowledgeAreaTopics();
        }
        return allTopics;
    }

    @Override
    public List<Degree> getAllDegrees() {
        List<Degree> degrees = new ArrayList<>();
        if (_isDegreesServiceBound) {
            degrees = _degreesService.getDegrees();
        }
        return degrees;
    }

    @Override
    public void loadDetailedDegreeFragment(View degreeCard) {

        ImageView image = (ImageView) degreeCard.findViewById(R.id.university_image);
        TextView universityName = (TextView) degreeCard.findViewById(R.id.university_name);
        TextView degreeName = (TextView) degreeCard.findViewById(R.id.degree_name);

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

        // And when the user presses Back on the toolbar I use a transition listener to change the
        // toolbar back to its original state. Neat.
        Transition exitTransition = new Slide(Gravity.RIGHT);
        exitTransition.setDuration(transitionDuration);
        degreeDetailsFragment.setReturnTransition(exitTransition);
        exitTransition.addListener(new OnStartTransitionListener() {
            @Override
            public void onStartTransition(Transition transition) {
                resetToolbar();
            }
        });

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
    public Degree getDegree(int degreeId) {

        Degree degree = new Degree();
        if (_isDegreesServiceBound) {
            degree = _degreesService.getDegree(degreeId);
        }
        return degree;
    }


    @Override
    public DegreeClass getDegreeClass(int degreeId, String degreeClassId) {

        DegreeClass degreeClass = new DegreeClass();
        if (_isDegreesServiceBound) {
            degreeClass = _degreesService.getDegreeClass(degreeId, degreeClassId);
        }
        return degreeClass;
    }

    @Override
    public DegreeClass getDegreeClass(String degreeClassId) {

        DegreeClass degreeClass = new DegreeClass();
        if (_isDegreesServiceBound) {
            degreeClass = _degreesService.getDegreeClass(degreeClassId);
        }
        return degreeClass;
    }


    @Override
    public void loadDegreeClassFragment(View selectedView, DegreeClass degreeClass) {

        Fragment degreeClassFragment = DegreeClassFragment.newInstance();

        Degree degree = _viewModel.getDegree(degreeClass.getDegreeId()).getValue();

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

        // And when the user presses Back on the toolbar I use a transition listener to change the
        // toolbar back to its original state. Neat.
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
                .replace(R.id.content_area, degreeClassFragment, "DegreeClass")
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void loadDegreeEvaluatorFragment(String degreeClassId) {

        Fragment classEvaluator = DegreeTopicMatcherFragment.newInstance(degreeClassId);

        DegreeClass degreeClass = getDegreeClass(degreeClassId);

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

        // And when the user presses Back on the toolbar I use a transition listener to change the
        // toolbar back to its original state. Neat.
        Transition exitTransition = new Slide(Gravity.RIGHT);
        exitTransition.setDuration(400);
        classEvaluator.setReturnTransition(exitTransition);
        exitTransition.addListener(new OnStartTransitionListener() {
            @Override
            public void onStartTransition(Transition transition) {
                // When we get back, we get back to degree so it is too soon to reset the toolbar
                changeToolbarIntoBackButton(getResources().getString(degreeClass.getNameResource()));
            }
        });


        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content_area, classEvaluator, "ClassEvaluator")
                .addToBackStack(null)
                .commit();

    }


    @Override
    public void saveMatch(DegreeClassMatch newlySubmittedMatch) {
        if (_rankingService.saveMatch(newlySubmittedMatch)) {
            getFragmentManager().popBackStackImmediate();
        }
    }

    @Override
    public boolean isDegreeMatched(int degreeId) {
        return _rankingService.hasCompleteMatch(degreeId);
    }

    @Override
    public boolean hasMatch(String degreeClassId) {
        return _rankingService.hasMatches(degreeClassId);
    }

    @Override
    public DegreeClassMatch getDegreeClassMatches(String degreeClassId) {
        return _rankingService.getDegreeClassMatches(degreeClassId);
    }


    @Override
    public void loadChartFragment(View v, String degreeScoreId) {

        Fragment chartFragment = ScoreChartFragment.newInstance(degreeScoreId);

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

        // And when the user presses Back on the toolbar I use a transition listener to change the
        // toolbar back to its original state. Neat.
        Transition exitTransition = new Slide(Gravity.RIGHT);
        exitTransition.setDuration(300);
        chartFragment.setReturnTransition(exitTransition);
        exitTransition.addListener(new OnStartTransitionListener() {
            @Override
            public void onStartTransition(Transition transition) {
                resetToolbar();
            }
        });

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content_area, chartFragment, "ChartFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void loadCompareScoresFragment(List<String> degreeScoreIds) {

        Fragment chartFragment = MultiScoreChartFragment.newInstance(degreeScoreIds.get(0), degreeScoreIds.get(1));

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

        // And when the user presses Back on the toolbar I use a transition listener to change the
        // toolbar back to its original state. Neat.
        Transition exitTransition = new Slide(Gravity.RIGHT);
        exitTransition.setDuration(300);
        chartFragment.setReturnTransition(exitTransition);
        exitTransition.addListener(new OnStartTransitionListener() {
            @Override
            public void onStartTransition(Transition transition) {
                resetToolbar();
            }
        });

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content_area, chartFragment, "MultiChartFragment")
                .addToBackStack(null)
                .commit();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopServices();
    }

    private void stopServices() {
        if (_isRankingServiceBound) {
            unbindService(_rankingServiceConnection);
        }
        if (_isDegreesServiceBound) {
            unbindService(_degreesServiceConnection);
        }
        if (_isSwebokServiceBound) {
            unbindService(_swebokServiceConnection);
        }

    }


    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }
}
