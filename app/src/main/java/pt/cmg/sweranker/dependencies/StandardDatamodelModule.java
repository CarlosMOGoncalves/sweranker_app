package pt.cmg.sweranker.dependencies;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import pt.cmg.sweranker.degrees.DegreesRepository;
import pt.cmg.sweranker.degrees.ResourcesDegreeRepository;
import pt.cmg.sweranker.swebok.ResourcesSwebokRepository;
import pt.cmg.sweranker.swebok.SwebokRepository;

@Module(includes = ContextModule.class)
public class StandardDatamodelModule {

    @Provides
    @SweRankerApplicationScope
    public SwebokRepository swebokRepository(Context context) {
        return new ResourcesSwebokRepository(context);
    }


    @Provides
    @SweRankerApplicationScope
    public DegreesRepository degreesRepository(Context context) {
        return new ResourcesDegreeRepository(context);
    }

}
