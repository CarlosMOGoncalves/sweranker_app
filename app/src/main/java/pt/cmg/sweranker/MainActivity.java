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
import pt.cmg.sweranker.degrees.DegreeDetailsFragment;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SwebokKAsFragment.OnSwebokFragmentInteractionListener,
        SwebokKADetailsFragment.OnKaDetailsFragmentInteractionListener,
        DegreesFragment.DegreesFragmentInteractionListener,
        DegreeDetailsFragment.DegreeDetailsFragmentInteractionListener,
        DegreeClassFragment.DegreeClassFragmentInteractionListener {


    private SwebokLoaderService _swebokLoaderService;
    private DegreesLoaderService _degreesLoaderService;
    boolean _isSwebokServiceBound = false;
    boolean _isDegreesServiceBound = false;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(_toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.nav_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, _toolbar, R.string.openDrawer, R.string.closeDrawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

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
    public List<KnowledgeArea> loadKnowledgeAreasForSwebokFragment() {
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

        // Very important! Adding a listener in order to change the Action Bar colour and the Status Bar colour when this transition finishes.
        imageEnterTransition.addListener(new OnEndTransitionListener() {
            @Override
            public void onEndTransition(Transition transition) {
                UXUtils.animateActionBarColourChange(_toolbar, imageBackgroundColour, 0, 0);
                UXUtils.animateStatusBarColourChange(myActivity, imageBackgroundColour, 0, 0);
            }
        });


        Transition sharedImageExitTransition = createImageExitSharedElementTransition(kaDetailsFragment, transitionDuration);
        // Very important! Adding a listener in order to change the Action Bar colour and the Status Bar colour when this transition starts.
        sharedImageExitTransition.addListener(new OnStartTransitionListener() {
            @Override
            public void onStartTransition(Transition transition) {
                UXUtils.animateStatusBarColourChange(myActivity, statusBarOriginalColour, 0, 0);
                UXUtils.animateActionBarColourChange(_toolbar, actionBarOriginalColour, 0, 0);
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
    public List<Degree> loadDegreesForFragment() {
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


        long transitionDuration = 500;

        Fragment degreeDetailsFragment = DegreeDetailsFragment.newInstance(degreeId);

        Activity myActivity = this;

        Transition imageEnterTransition = createImageEnterSharedElementTransition(degreeDetailsFragment, transitionDuration);
        Transition sharedImageExitTransition = createImageExitSharedElementTransition(degreeDetailsFragment, transitionDuration);

        // This just creates a transition for the rest of the content, i.e. not shared.
        Transition contentTransition = new Slide(Gravity.RIGHT);
        contentTransition.setDuration(transitionDuration);
        degreeDetailsFragment.setEnterTransition(contentTransition);
        degreeDetailsFragment.setExitTransition(contentTransition);

        getFragmentManager()
                .beginTransaction()
                .addSharedElement(image, "degree_image")
                .addSharedElement(universityName, "university_name")
                .addSharedElement(degreeName, "degree_name")
                .replace(R.id.content_area, degreeDetailsFragment, "DegreeDetail")
                .addToBackStack(null)
                .commit();
    }

    /**
     * Loads a Degree passing its id.
     *
     * @param degreeId
     * @return
     */
    @Override
    public Degree getDegree(int degreeId) {

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
}
