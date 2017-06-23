package pt.cmg.sweranker.dependencies;

import dagger.Component;
import pt.cmg.sweranker.SweRankerApplication;

/**
 * This is the main component of the application. This is basically used at this moment
 * to wire the correct Data Repositories to the whole app, despite the fact that they
 * are used in the Activity level.
 */
@Component
@SweRankerApplicationScope
public interface ApplicationComponent {

    void inject(SweRankerApplication application);

}
