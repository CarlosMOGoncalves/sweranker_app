package pt.cmg.sweranker;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * This class is used mainly to initialise and configure he Realm database.
 * Yes, Realm, like the Netherealm from Mortal Kombat.
 * <p>
 * Created by Carlos on 06/04/2017.
 */

public class SweRankerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);

        RealmConfiguration defaultConfiguration = new RealmConfiguration.Builder()
                .name("sweranker.realm")
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(18)
                .build();

        Realm.setDefaultConfiguration(defaultConfiguration);
    }

}
