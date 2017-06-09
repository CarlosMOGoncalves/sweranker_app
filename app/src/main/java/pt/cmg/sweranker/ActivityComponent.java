package pt.cmg.sweranker;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Carlos on 09/06/2017.
 */
@Singleton
@Component(modules = StandardDatamodelModule.class)
public interface ActivityComponent {

    void inject(MainActivity activity);

    Context getContext();

    SwebokRepository getSwebokRepository();

}
