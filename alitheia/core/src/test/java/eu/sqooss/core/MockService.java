package eu.sqooss.core;

import org.osgi.framework.BundleContext;

import eu.sqooss.service.logging.Logger;

public class MockService implements AlitheiaCoreService {

	@Override
	public boolean startUp() {
		return true;
	}

	@Override
	public void shutDown() {

	}

	@Override
	public void setInitParams(BundleContext bc, Logger l) {

	}

}