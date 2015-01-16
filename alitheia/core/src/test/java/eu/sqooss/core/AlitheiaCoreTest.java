package eu.sqooss.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.sqooss.impl.service.admin.AdminServiceImpl;
import eu.sqooss.service.admin.AdminService;

public class AlitheiaCoreTest {
	
	/**
	 * Test the registration of a standard service
	 */
	@Test
	public void testGetBaseService() {
		AlitheiaCore instance = AlitheiaCore.testInstance();
		
		assertNull(instance.getAdminService());
		
		// Register a standard service
		instance.registerService(AdminService.class, AdminServiceImpl.class);
		
		// Should be not null now
		assertNotNull(instance.getAdminService());
		assertNotNull(instance.getService(AdminService.class));
		
		// Should be the same
		assertEquals(instance.getAdminService(), instance.getService(AdminService.class));
	}

	/**
	 * Test the registration of a custom service
	 */
	@Test
	public void testGetCustomService() {
		AlitheiaCore instance = AlitheiaCore.testInstance();
		
		// Register a custom service
		instance.registerService(MockService.class, MockService.class);
		
		MockService service = instance.getService(MockService.class);
		
		// Should not be null
		assertNotNull(service);
		
		// Register a second implementation of the same service
		instance.registerService(MockService.class, MockServiceExtended.class);
		
		MockService service2 = instance.getService(MockService.class);
		
		// Should not be null
		assertNotNull(service);
				
		// Should not be the same
		assertNotSame(service, service2);
		
		// Should be of the subtype
		assertTrue(service2 instanceof MockServiceExtended);
	}
}
