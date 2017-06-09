package pt.cmg.sweranker;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Carlos on 09/06/2017.
 */
@Module
public class StandardDatamodelModule {

    private Context _context;

    public StandardDatamodelModule(Context context) {
        _context = context;
    }

    @Provides
    Context provideContext() {
        return _context;
    }

    @Provides
    SwebokRepository provideSwebokRepository() {
        return new ResourcesSwebokRepository(_context);
    }

}
