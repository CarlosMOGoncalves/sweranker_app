package pt.cmg.sweranker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.degrees.DegreesLoaderService;

public class DegreeClassActivity extends AppCompatActivity {


    public static final String DEGREE_CLASS_ID_EXTRA = "DEGREE_CLASS_ID";
    public static final String DEGREE_ID = "DEGREE_ID";

    // Service related BEGIN
    private DegreesLoaderService _degreesLoaderService;
    boolean _isDegreesServiceBound = false;

    private ServiceConnection _degreesServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DegreesLoaderService.DegreesLoaderBinder binder = (DegreesLoaderService.DegreesLoaderBinder) service;
            _degreesLoaderService = binder.getService();
            _isDegreesServiceBound = true;

            _degreeClass = loadClass();
            TextView className = (TextView) findViewById(R.id.justatext);
            className.setText(getResources().getString(_degreeClass.getNameResource()));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _degreesLoaderService = null;
            _isDegreesServiceBound = false;
        }
    };
    // Service related END


    private Toolbar _toolbar;
    private int _degreeId;
    private String _degreeClassId;
    private DegreeClass _degreeClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_degree_class);

        _toolbar = initialiseActionBar();

        if (!getIntent().hasExtra(DEGREE_CLASS_ID_EXTRA)) {
            throw new RuntimeException("Missing Degree Class Id, contact the developer.");
        }

        if (!getIntent().hasExtra(DEGREE_ID)) {
            throw new RuntimeException("Missing Degree Id, contact the developer.");
        }

        _degreeId = getIntent().getIntExtra(DEGREE_ID, 0);
        _degreeClassId = getIntent().getStringExtra(DEGREE_CLASS_ID_EXTRA);

        Intent bindToDegreesService = new Intent(this, DegreesLoaderService.class);
        bindService(bindToDegreesService, _degreesServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private Toolbar initialiseActionBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(view -> finish());
        toolbar.setTitle("Allahu Akbar");

        return toolbar;
    }

    @Override
    protected void onStart() {
        super.onStart();


    }


    private DegreeClass loadClass() {

        DegreeClass degreeClass = new DegreeClass();
        if (_isDegreesServiceBound) {
            degreeClass = _degreesLoaderService.getDegreeClass(_degreeId, _degreeClassId);
        }

        return degreeClass;
    }

}
