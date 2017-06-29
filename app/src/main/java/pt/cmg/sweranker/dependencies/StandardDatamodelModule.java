package pt.cmg.sweranker.dependencies;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import pt.cmg.sweranker.degrees.DegreeMatchesRepository;
import pt.cmg.sweranker.degrees.DegreesRepository;
import pt.cmg.sweranker.degrees.ResourcesDegreeMatchesRepository;
import pt.cmg.sweranker.degrees.ResourcesDegreesRepository;
import pt.cmg.sweranker.ranking.RealmScoresRepository;
import pt.cmg.sweranker.ranking.ScoresRepository;
import pt.cmg.sweranker.swebok.ResourcesSwebokRepository;
import pt.cmg.sweranker.swebok.SwebokRepository;

@Module
public class StandardDatamodelModule {

    @Provides
    @MainActivityScope
    public SwebokRepository swebokRepository(Context context) {
        return new ResourcesSwebokRepository(context);
    }


    @Provides
    @MainActivityScope
    public DegreesRepository degreesRepository(Context context) {
        return new ResourcesDegreesRepository(context);
    }

    @Provides
    @MainActivityScope
    public DegreeMatchesRepository matchesRepository(Context context) {
        return new ResourcesDegreeMatchesRepository(context);
    }

    @Provides
    @MainActivityScope
    public ScoresRepository scoresRepository() {
        return new RealmScoresRepository();
    }


}
