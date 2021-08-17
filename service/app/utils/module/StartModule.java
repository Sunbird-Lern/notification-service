package utils.module;

import com.google.inject.AbstractModule;

/**
 * This class is responsible for creating instance of
 * ApplicationStart at server startup time.
 * @author manzarul
 *
 */
public class StartModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(utils.module.SignalHandler.class).asEagerSingleton();
            bind(utils.ApplicationStart.class).asEagerSingleton();

        }

}
