package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import kf5012darthmaulapplication.*;
import kf5012darthmaulapplication.User.permissionsEnum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
class TestUser {

	@Test
	void testUsernameMatches() {
		String username = "username";
		try {
			User user = new User(username,1);
			assertEquals(user.getUsername(),username);
		}catch(Exception ex) {
			fail("User Generation failed: " + ex.getMessage());
		}
	}
	
	@Test
	void testUserHasAllPermission() {		
		try {
			int allPermissionsValue = (int) ((Math.pow(2, permissionsEnum.values().length)) - 1);
			User user1 = new User("test", allPermissionsValue);
			ArrayList<User.permissionsEnum> ActualUserPermissions = user1.getPermissions();
			List<permissionsEnum> ExpectedUserPermissions = new ArrayList<>(Arrays.asList(User.permissionsEnum.values()));
			
			assertEquals(ExpectedUserPermissions, ActualUserPermissions);
			
		}catch(Exception ex) {
			fail("User Generation failed" + ex.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	@Test
	void testUserPermissionNumberTooBig() {
		int biggestValue = (int) ((Math.pow(2, permissionsEnum.values().length)));
		Exception exception = assertThrows(Exception.class, () -> {
			User user1 = new User("test", biggestValue);
		});
		String expectedMessage = "Permission Value too big, got: " + biggestValue;
		String actualMessage = exception.getMessage();
		
		assertEquals(actualMessage,expectedMessage);
	}
}
