package utils.module;

import com.google.inject.AbstractModule;

import utils.ApplicationStart;

/**
 * This class is responsible for creating instance of 
 * ApplicationStart at server startup time.
 * @author manzarul
 *
 */
public class StartModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(SignalHandler.class).asEagerSingleton();
		bind(ApplicationStart.class).asEagerSingleton();

	}

}
