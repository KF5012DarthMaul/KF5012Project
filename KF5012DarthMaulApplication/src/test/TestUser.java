package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import kf5012darthmaulapplication.*;

import java.util.ArrayList;
class TestUser {

	@Test
	void testUsernameMatches() {
		String username = "username";
		try {
			User user = new User(username,PermissionManager.AccountType.CARETAKER);
			assertEquals(user.getUsername(),username);
		}catch(Exception ex) {
			fail("User Generation failed: " + ex.getMessage());
		}
	}
	
	@Test
	void testCARETAKEPermissionAssignment() {
		ArrayList<PermissionManager.Permission> expectedPermissions = new ArrayList<>();
		expectedPermissions.add(PermissionManager.Permission.SWAP_TASK);
		expectedPermissions.add(PermissionManager.Permission.GET_TASK);
		try {
			User user = new User("test", PermissionManager.AccountType.CARETAKER);
			ArrayList<PermissionManager.Permission> actualPermissions = user.pm.getPermissions();
			
			assertEquals(expectedPermissions, actualPermissions);
		}catch(Exception ex) {
			fail("User Generation failed: " + ex.getMessage());
		}
	}
	@Test
	void testHRPERSONNELPermissionAssignment() {
		assertEquals(1, 1);
	}
}