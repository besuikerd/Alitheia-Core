package eu.sqooss.core;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.sqooss.impl.service.admin.AdminServiceImpl;
import eu.sqooss.service.admin.AdminService;

public class AlitheiaCoreTest {

	/**
	 * 
	 */
	@Test
	public void testGetService() {
		AlitheiaCore instance = AlitheiaCore.testInstance();
		
		assertNull(instance.getAdminService());
		
		// Register a standard service
		instance.registerService(AdminService.class, AdminServiceImpl.class);
		
		// Should be not null now
		assertNotNull(instance.getAdminService());
		assertNotNull(instance.getService(AdminService.class));
		
		// Should be the same
		assertEquals(instance.getAdminService(), instance.getService(AdminService.class));
		
		// Register a custom service
		instance.registerService(MockService.class, MockService.class);
		
		// Should not be null
		assertNotNull(instance.getService(MockService.class));
		
	}

}
