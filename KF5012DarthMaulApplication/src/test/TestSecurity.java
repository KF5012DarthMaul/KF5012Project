package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;
import kf5012darthmaulapplication.SecurityManager;

public class TestSecurity {
	
	@Test
	public void testPasswordValidation() {
		String testPassword = "password";		
		try {
			String testEncryptedPassword = SecurityManager.generatePassword(testPassword);
			System.out.println(testEncryptedPassword);
			Boolean result = SecurityManager.validatePassword(testPassword, testEncryptedPassword);
			assertTrue(result);
		} catch (Exception e) {
			fail("TestSecurity Error: Failed to generate password \n" + e);
		}
	}
	
	@Test
	public void testPasswordGeneration() {
		String testPassword = "password";
		try {
			String encryptedPassword1 = SecurityManager.generatePassword(testPassword);
			String encryptedPassword2 = SecurityManager.generatePassword(testPassword);
			assertNotEquals(encryptedPassword1,encryptedPassword2);
		} catch (Exception e) {
			fail("TestSecurity Error: Failed to generate password \n" + e);
		}
	}
	
	@Test
	public void testNewPasswordStringGeneration() {
		String passString1 = SecurityManager.generateRandomPasswordString();
		String passString2 = SecurityManager.generateRandomPasswordString();
		assertNotEquals(passString1, passString2);
	}
	
	@Test
	public void testPasswordStrengthValidator() {
		assertTrue(SecurityManager.passwordStrengthValidatorNoOutput("abcABC123!@#".toCharArray()));
		assertFalse(SecurityManager.passwordStrengthValidatorNoOutput(null));
		assertFalse(SecurityManager.passwordStrengthValidatorNoOutput("AAAaa#1".toCharArray())); //Length
		assertFalse(SecurityManager.passwordStrengthValidatorNoOutput("aaaaaaa#1".toCharArray())); //Upper
		assertFalse(SecurityManager.passwordStrengthValidatorNoOutput("AAAAAAA#1".toCharArray())); //Lower
		assertFalse(SecurityManager.passwordStrengthValidatorNoOutput("AAAAaaaa1".toCharArray())); //Special
	}
}