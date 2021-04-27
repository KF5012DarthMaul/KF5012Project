package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import kf5012darthmaulapplication.SecurityManager;

class TestSecurity {
	
	@Test
	void testPasswordValidation() {
		String testPassword = "password";				
		try {
			String testEncryptedPassword = SecurityManager.generatePassword(testPassword);
			Boolean result = SecurityManager.validatePassword(testPassword, testEncryptedPassword);
			assertTrue(result);
		} catch (Exception e) {
			fail("TestSecurity Error: Failed to generate password \n" + e);
		}
	}
	
	@Test
	void testPasswordGeneration() {
		String testPassword = "password";
		try {
			String encryptedPassword1 = SecurityManager.generatePassword(testPassword);
			String encryptedPassword2 = SecurityManager.generatePassword(testPassword);
			assertNotEquals(encryptedPassword1,encryptedPassword2);
		} catch (Exception e) {
			fail("TestSecurity Error: Failed to generate password \n" + e);
		}
	}

}
