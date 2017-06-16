package pt.cmg.sweranker.dependencies;


import dagger.Component;
import pt.cmg.sweranker.MainActivity;

@Component(modules = MainActivityModule.class, dependencies = ApplicationComponent.class)
@MainActivityScope
public interface MainActivityComponent {

    void inject(MainActivity activity);

}
