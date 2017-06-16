package pt.cmg.sweranker.dependencies;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class ContextModule {

    private Context _context;

    public ContextModule(Context context) {
        _context = context;
    }

    @Provides
    @SweRankerApplicationScope
    public Context context() {
        return _context;
    }
}
