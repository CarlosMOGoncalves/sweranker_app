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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pt.cmg.sweranker.fragments.KADetailsFragment;
import pt.cmg.sweranker.fragments.SwebokFragment;
import pt.cmg.sweranker.ui.ImageSizeAndPlaceTransition;
import pt.cmg.sweranker.ui.OnEndTransitionListener;
import pt.cmg.sweranker.ui.UXUtils;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwebokFragment.OnSwebokFragmentInteractionListener, KADetailsFragment.OnKaDetailsFragmentInteractionListener {


    private SwebokLoaderService _swebokLoaderService;
    boolean _isBound = false;
    private Toolbar _toolbar;


    private ServiceConnection _serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SwebokLoaderService.SwebokLoaderBinder binder = (SwebokLoaderService.SwebokLoaderBinder) service;
            _swebokLoaderService = binder.getService();
            _isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _swebokLoaderService = null;
            _isBound = false;
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
        Intent intent = new Intent(this, SwebokLoaderService.class);
        startService(intent);
        bindService(intent, _serviceConnection, Context.BIND_AUTO_CREATE);
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
            getFragmentManager().beginTransaction().add(R.id.content_area, SwebokFragment.newInstance(), "Swebok").commit();
        } else if (id == R.id.curricula_nav) {
            Toast.makeText(getApplicationContext(), "Curriculos", Toast.LENGTH_LONG).show();
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
        if (_isBound) {
            kas = _swebokLoaderService.getKnowledgeAreas();
        }
        return kas;
    }

    @Override
    public void loadDetailedKnowledgeAreaFragment(View v, int knowledgeAreaId) {
        ImageView image = (ImageView) v.findViewById(R.id.ka_image);
        int imageBackgroundColour = ((ColorDrawable) image.getBackground()).getColor();

        int actionBarOriginalColour = _toolbar.getSolidColor();
        int statusBarOriginalColour = this.getWindow().getStatusBarColor();

        long transitionDuration = 500;

        Fragment kaDetailsFragment = KADetailsFragment.newInstance(knowledgeAreaId);
        Activity myActivity = this;

        Transition sharedImageEnterTransition = new ImageSizeAndPlaceTransition();
        sharedImageEnterTransition.setDuration(transitionDuration);
        kaDetailsFragment.setSharedElementEnterTransition(sharedImageEnterTransition);
        sharedImageEnterTransition.addListener(new OnEndTransitionListener() {
            @Override
            public void onEndTransition(Transition transition) {
                UXUtils.animateActionBarColourChange(_toolbar, imageBackgroundColour, 500, 0);
                UXUtils.animateStatusBarColourChange(myActivity, imageBackgroundColour);
            }
        });


        Transition sharedImageExitTransition = new ImageSizeAndPlaceTransition();
        sharedImageExitTransition.setDuration(transitionDuration);
        kaDetailsFragment.setSharedElementReturnTransition(sharedImageExitTransition);
        sharedImageExitTransition.addListener(new OnEndTransitionListener() {
            @Override
            public void onEndTransition(Transition transition) {
                UXUtils.animateStatusBarColourChange(myActivity, statusBarOriginalColour);
                UXUtils.animateActionBarColourChange(_toolbar, actionBarOriginalColour, 500, 0);

            }
        });


        Transition enterContentTransition = new Slide();
        enterContentTransition.setDuration(transitionDuration);
        kaDetailsFragment.setEnterTransition(enterContentTransition);
        kaDetailsFragment.setExitTransition(enterContentTransition);


        getFragmentManager().beginTransaction().addSharedElement(image, "ka_image").replace(R.id.content_area, kaDetailsFragment, "KADetail").addToBackStack(null).commit();


    }


    @Override
    public KnowledgeArea getKnowledgeArea(int knowledgeAreaIdToLoad) {
        KnowledgeArea ka = new KnowledgeArea();
        if (_isBound) {
            ka = _swebokLoaderService.getKnowledgeArea(knowledgeAreaIdToLoad);
        }
        return ka;
    }
}
