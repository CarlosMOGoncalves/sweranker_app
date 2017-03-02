package pt.cmg.sweranker;

import android.app.Activity;
import android.app.Fragment;
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

import pt.cmg.sweranker.degrees.Degree;
import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.degrees.DegreeClassFragment;
import pt.cmg.sweranker.degrees.DegreeClassMatch;
import pt.cmg.sweranker.degrees.DegreeDetailsFragment;
import pt.cmg.sweranker.degrees.DegreeMatcherService;
import pt.cmg.sweranker.degrees.DegreeTopicMatcherFragment;
import pt.cmg.sweranker.degrees.DegreesFragment;
import pt.cmg.sweranker.degrees.DegreesLoaderService;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.SwebokKADetailsFragment;
import pt.cmg.sweranker.swebok.SwebokKAsFragment;
import pt.cmg.sweranker.swebok.SwebokLoaderService;
import pt.cmg.sweranker.ui.ImageSizeAndPlaceTransition;
import pt.cmg.sweranker.ui.OnEndTransitionListener;
import pt.cmg.sweranker.ui.OnStartTransitionListener;
import pt.cmg.sweranker.ui.UXUtils;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        SwebokKAsFragment.OnSwebokFragmentInteractionListener,
        SwebokKADetailsFragment.OnKaDetailsFragmentInteractionListener,
        DegreesFragment.DegreesFragmentInteractionListener,
        DegreeDetailsFragment.DegreeDetailsFragmentInteractionListener,
        DegreeClassFragment.DegreeClassFragmentInteractionListener,
        DegreeTopicMatcherFragment.OnDegreeMatcherFragmentInteraction {


    private SwebokLoaderService _swebokLoaderService;
    private DegreesLoaderService _degreesLoaderService;
    private DegreeMatcherService _matcherService;
    boolean _isSwebokServiceBound = false;
    boolean _isDegreesServiceBound = false;
    boolean _isMatcherServiceBound = false;
    private Toolbar _toolbar;


    private ServiceConnection _swebokServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SwebokLoaderService.SwebokLoaderBinder binder = (SwebokLoaderService.SwebokLoaderBinder) service;
            _swebokLoaderService = binder.getService();
            _isSwebokServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _swebokLoaderService = null;
            _isSwebokServiceBound = false;
        }
    };

    private ServiceConnection _degreesServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DegreesLoaderService.DegreesLoaderBinder binder = (DegreesLoaderService.DegreesLoaderBinder) service;
            _degreesLoaderService = binder.getService();
            _isDegreesServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _degreesLoaderService = null;
            _isDegreesServiceBound = false;
        }
    };

    private ServiceConnection _matcherServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DegreeMatcherService.DegreeMatcherBinder binder = (DegreeMatcherService.DegreeMatcherBinder) service;
            _matcherService = binder.getService();
            _isMatcherServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _matcherService = null;
            _isMatcherServiceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
     * @param toolbarTitle
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
        Intent startSwebokServiceIntent = new Intent(this, SwebokLoaderService.class);
        startService(startSwebokServiceIntent);
        bindService(startSwebokServiceIntent, _swebokServiceConnection, Context.BIND_AUTO_CREATE);

        Intent startDegreesServiceIntent = new Intent(this, DegreesLoaderService.class);
        startService(startDegreesServiceIntent);
        bindService(startDegreesServiceIntent, _degreesServiceConnection, Context.BIND_AUTO_CREATE);

        Intent startDegreeMatcherService = new Intent(this, DegreeMatcherService.class);
        startService(startDegreeMatcherService);
        bindService(startDegreeMatcherService, _matcherServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.swebok_nav) {
            // Careful with his, it is here because of the animations on KA details.
            // When pressed the menu and selected one item the animations would not run.
            getFragmentManager().popBackStackImmediate();
            getFragmentManager().beginTransaction().replace(R.id.content_area, SwebokKAsFragment.newInstance(), "Swebok").commit();
        } else if (id == R.id.curricula_nav) {
            // Careful with his, it is here because of the animations on KA details.
            // When pressed the menu and selected one item the animations would not run.
            getFragmentManager().popBackStackImmediate();
            getFragmentManager().beginTransaction().replace(R.id.content_area, DegreesFragment.newInstance(), "Degrees").commit();
        } else if (id == R.id.rankings_nav) {
            Toast.makeText(getApplicationContext(), "Rankings", Toast.LENGTH_LONG).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.nav_drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public List<KnowledgeArea> getKnowledgeAreas() {
        List<KnowledgeArea> kas = new ArrayList<>();
        if (_isSwebokServiceBound) {
            kas = _swebokLoaderService.getKnowledgeAreas();
        }
        return kas;
    }

    @Override
    public void loadDetailedKnowledgeAreaFragment(View knowledgeAreaCardView, int knowledgeAreaId) {
        ImageView image = (ImageView) knowledgeAreaCardView.findViewById(R.id.ka_image);

        // Gets the colour that will be changed between fragments, the source, which is the KA decorative colour
        int imageBackgroundColour = ((ColorDrawable) image.getBackground()).getColor();
        // ... and the original colours of the Action Bars and Status Bar so we can return back to the original colours
        int actionBarOriginalColour = _toolbar.getSolidColor();
        int statusBarOriginalColour = this.getWindow().getStatusBarColor();

        long transitionDuration = 500;

        Fragment kaDetailsFragment = SwebokKADetailsFragment.newInstance(knowledgeAreaId);
        Activity myActivity = this;

        Transition imageEnterTransition = createImageEnterSharedElementTransition(kaDetailsFragment, transitionDuration);

        KnowledgeArea knowledgeArea = getKnowledgeArea(knowledgeAreaId);

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
     * Creates and sets a shared element transition from source fragment {@link SwebokKAsFragment} to the target detailed fragment {@link SwebokKADetailsFragment}.
     * The shared element is the KA decorative image and it will be set on the fragment transaction.
     * This function only creates the Transition animation for the image.
     *
     * @param targetFragment
     * @param transitionDuration
     */
    private Transition createImageEnterSharedElementTransition(Fragment targetFragment, long transitionDuration) {

        Transition sharedImageEnterTransition = new ImageSizeAndPlaceTransition();
        sharedImageEnterTransition.setDuration(transitionDuration);
        targetFragment.setSharedElementEnterTransition(sharedImageEnterTransition);
        return sharedImageEnterTransition;

    }

    /**
     * Creates and sets a shared element transition from target fragment {@link SwebokKADetailsFragment} BACK TO the source fragment {@link SwebokKAsFragment}.
     * The shared element is the KA decorative image and it will be set on the fragment transaction.
     * This function only creates the Transition animation for the image.
     *
     * @param targetFragment
     * @param transitionDuration
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
            ka = _swebokLoaderService.getKnowledgeArea(knowledgeAreaIdToLoad);
        }
        return ka;
    }

    @Override
    public List<Degree> loadDegrees() {
        List<Degree> degrees = new ArrayList<>();
        if (_isDegreesServiceBound) {
            degrees = _degreesLoaderService.getDegrees();
        }
        return degrees;
    }

    @Override
    public void loadDetailedDegreeFragment(View degreeCard, int degreeId) {

        ImageView image = (ImageView) degreeCard.findViewById(R.id.university_image);
        TextView universityName = (TextView) degreeCard.findViewById(R.id.university_name);
        TextView degreeName = (TextView) degreeCard.findViewById(R.id.degree_name);

        Degree degree = loadDegree(degreeId);


        long transitionDuration = 500;

        Fragment degreeDetailsFragment = DegreeDetailsFragment.newInstance(degreeId);

        Transition imageEnterTransition = createImageEnterSharedElementTransition(degreeDetailsFragment, transitionDuration);
        Transition sharedImageExitTransition = createImageExitSharedElementTransition(degreeDetailsFragment, transitionDuration);


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
    public Degree loadDegree(int degreeId) {

        Degree degree = new Degree();
        if (_isDegreesServiceBound) {
            degree = _degreesLoaderService.getDegree(degreeId);
        }
        return degree;
    }


    @Override
    public DegreeClass loadDegreeClass(int degreeId, String degreeClassId) {

        DegreeClass degreeClass = new DegreeClass();
        if (_isDegreesServiceBound) {
            degreeClass = _degreesLoaderService.getDegreeClass(degreeId, degreeClassId);
        }
        return degreeClass;
    }

    @Override
    public DegreeClass loadDegreeClass(String degreeClassId) {

        DegreeClass degreeClass = new DegreeClass();
        if (_isDegreesServiceBound) {
            degreeClass = _degreesLoaderService.getDegreeClass(degreeClassId);
        }
        return degreeClass;
    }


    @Override
    public void loadDegreeClassFragment(int degreeId, String degreeClassId) {

        Fragment degreeClassFragment = DegreeClassFragment.newInstance(degreeId, degreeClassId);

        DegreeClass degreeClass = loadDegreeClass(degreeId, degreeClassId);
        Degree degree = loadDegree(degreeId);

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

        DegreeClass degreeClass = loadDegreeClass(degreeClassId);

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
        if (_matcherService.saveMatch(newlySubmittedMatch)) {
            getFragmentManager().popBackStackImmediate();
        }
    }

    @Override
    public boolean hasMatch(String degreeClassId) {
        return _matcherService.hasMatches(degreeClassId);
    }

    @Override
    public DegreeClassMatch getDegreeClassMatches(String degreeClassId) {
        return _matcherService.getDegreeClassMatches(degreeClassId);
    }
}
