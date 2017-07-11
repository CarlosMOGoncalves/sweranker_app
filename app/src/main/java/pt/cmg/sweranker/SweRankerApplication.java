package pt.cmg.sweranker;

import android.app.Activity;
import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import pt.cmg.sweranker.dependencies.ApplicationComponent;
import pt.cmg.sweranker.dependencies.DaggerApplicationComponent;

/**
 * This class is used mainly to initialise and configure he Realm database.
 * Yes, Realm, like the Netherealm from Mortal Kombat.
 */
public class SweRankerApplication extends Application {

    private ApplicationComponent _component;


    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);

        RealmConfiguration defaultConfiguration = new RealmConfiguration.Builder()
                .name("sweranker.realm")
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(2)
                .build();

        Realm.setDefaultConfiguration(defaultConfiguration);

        _component = DaggerApplicationComponent.builder().build();
    }


    public static SweRankerApplication get(Activity activity) {
        return (SweRankerApplication) activity.getApplication();
    }

    public ApplicationComponent getComponent() {
        return _component;
    }

}
