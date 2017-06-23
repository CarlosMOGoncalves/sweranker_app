package pt.cmg.sweranker.dependencies;

import dagger.Component;
import pt.cmg.sweranker.SweRankerApplication;
import pt.cmg.sweranker.degrees.DegreesRepository;
import pt.cmg.sweranker.ranking.MatchesRepository;
import pt.cmg.sweranker.ranking.ScoresRepository;
import pt.cmg.sweranker.swebok.SwebokRepository;

/**
 * This is the main component of the application. This is basically used at this moment
 * to wire the correct Data Repositories to the whole app, despite the fact that they
 * are used in the Activity level.
 */
@Component(modules = StandardDatamodelModule.class)
@SweRankerApplicationScope
public interface ApplicationComponent {

    void inject(SweRankerApplication application);

    SwebokRepository swebokRepository();

    DegreesRepository degreesRepository();

    MatchesRepository matchesRepository();

    ScoresRepository scoresRepository();

}
