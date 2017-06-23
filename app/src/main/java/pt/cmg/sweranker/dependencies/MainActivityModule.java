package pt.cmg.sweranker.dependencies;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import pt.cmg.sweranker.MainActivity;
import pt.cmg.sweranker.MainActivityViewModel;
import pt.cmg.sweranker.MainActivityViewModelFactory;
import pt.cmg.sweranker.degrees.DegreesRepository;
import pt.cmg.sweranker.ranking.MatchesRepository;
import pt.cmg.sweranker.ranking.ScoresRepository;
import pt.cmg.sweranker.swebok.SwebokRepository;

@Module
public class MainActivityModule {

    private final MainActivity activity;

    public MainActivityModule(MainActivity activity) {
        this.activity = activity;
    }

    @Provides
    @MainActivityScope
    Context context() {
        return activity;
    }

    @Provides
    @MainActivityScope
    ViewModelProvider.Factory viewmodelFactory(ViewModel viewModel) {
        return new MainActivityViewModelFactory(viewModel);
    }

    @Provides
    @MainActivityScope
    ViewModel viewModel(SwebokRepository swebokRepository, DegreesRepository degreesRepository, MatchesRepository matchesRepository, ScoresRepository scoresRepository) {
        return new MainActivityViewModel(swebokRepository, degreesRepository, matchesRepository, scoresRepository);
    }


}
