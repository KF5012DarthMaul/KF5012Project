package kf5012darthmaulapplication;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.swing.JOptionPane;

import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class SecurityManager {
	private static final int ITERATION_COUNT = 10000;
	/*
	 * 	Makes sure that the Security of the application is set correctly
	 */
	public void testSecurity() {
		int maxKeySize;
		try {
			maxKeySize = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
			if(maxKeySize <= 128) {
				new ErrorDialog("An Error Occured", new Error("Security Failure: MaxKeySize not set correctly"));
				System.exit(0);
			}
			System.out.println(maxKeySize);
		} catch (NoSuchAlgorithmException e) {
			new ErrorDialog("An Error Occured", new Error("Security Failure: " + e));
			System.exit(0);
		}
	}
	/**
	 * Generates a random 16 byte salt for use in password generation
	 * @return byte[] salt
	 * @throws Exception
	 */
	private static byte[] getSalt() throws Exception{
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt;
	}
	/**
	 * Helper function for converting bytes into a hex string
	 * @param bytes
	 * @return string
	 */
	private static String toHex(byte[] bytes){
		StringBuilder sb = new StringBuilder (bytes.length * 2);
		for(byte b : bytes) {
			sb.append(String.format("%02x",b));
		}
		return sb.toString();
	}
	/**
	 * 
	 * @param string hex
	 * @return byte[] bytes
	 */
	private static byte[] fromHex(String hex){
		byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++) {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
	}
	/**
	 * 
	 * @param password
	 * @return String
	 * @throws Exception
	 */
	public static String generatePassword(String password) throws Exception {
		int iterations = ITERATION_COUNT;
		char[] charArr = password.toCharArray();
		byte[] salt = getSalt();
		
		PBEKeySpec spec = new PBEKeySpec(charArr, salt, iterations, 256);
		SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		byte[] hash = keyFac.generateSecret(spec).getEncoded();
		
		return iterations + ":" + toHex(salt) + ":" + toHex(hash);
	}
	/**
	 * Generates a random secure string for password usage, meeting the minimum requirements of the system
	 * @return String
	 */
	public static String generateRandomPasswordString() {
		SecureRandom sr = new SecureRandom();
		int passLength = 16;		

		int leftLimit = 33;
		int rightLimit = 122;
		
		StringBuilder symbols = new StringBuilder(rightLimit - leftLimit);
		for(int i = leftLimit; i <= rightLimit; i++) {
			symbols.append((char)i);
		}
	    while(true) {
	        char[] generatedString = new char[passLength];
	        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
	        for(int i=0; i<generatedString.length; i++) {
	            char character = symbols.toString().charAt(sr.nextInt(symbols.length()));
	            if(Character.isUpperCase(character)) hasUpper = true;
	            else if(Character.isLowerCase(character)) hasLower = true;
	            else if(Character.isDigit(character)) hasDigit = true;
	            else hasSpecial = true;
	            generatedString[i] = character;
	        }
	        if(hasUpper && hasLower && hasDigit && hasSpecial) {
	            return new String(generatedString);
	        }
	    }
	}
	/**
	 * validates a password based off its hashed counterpart, returns if they match
	 * @param password
	 * @param storedPassword
	 * @return boolean
	 */
	public static boolean validatePassword(String password, String storedPassword) {
		String[] passwordSegments = storedPassword.split(":");
		int iterations = Integer.parseInt(passwordSegments[0]);
		byte[] salt = fromHex(passwordSegments[1]);
		byte[] hash = fromHex(passwordSegments[2]);
		
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, hash.length * 8);
		SecretKeyFactory keyFac = null;
		try {
			keyFac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		
		byte[] testHash = null;
		try {
			testHash = keyFac.generateSecret(spec).getEncoded();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		int diff = hash.length ^ testHash.length;
		for(int i = 0; i < hash.length && i < testHash.length; i++) {
			diff |= hash[i] ^ testHash[i];
		}
		return diff == 0;
	}
	/**
	 * Validate password strength before sending to database and updating password
	 * @param password
	 * @return
	 */
	public static boolean passwordStrengthValidator(char[] password) {
		if(password == null) return false;
		boolean hasUpper = false;
		boolean hasLower = false;
		boolean hasDigit = false;
		boolean hasSpecial = false;
		boolean minimumLength = (password.length >= 8)? true : false;
		for(char c : password) {
			if(Character.isUpperCase(c)) hasUpper = true;
			else if(Character.isLowerCase(c)) hasLower = true;
			else if(Character.isDigit(c)) hasDigit = true;
			else hasSpecial = true;
		}
		if (hasUpper && hasLower && hasDigit && hasSpecial && minimumLength) return true;
		else {
			String upperMissing = "\n> Missing an Uppercase character";
			String lowerMissing = "\n> Missing a Lowercase character";
			String digitMissing = "\n> Missing a number";
			String specialMissing = "\n> Missing a special character";
			String tooShortPass = "\n> Password too short, must be at least 8 characters";
			
			StringBuilder bobTheBuilder = new StringBuilder();
			
			if(!hasUpper) bobTheBuilder.append(upperMissing);
			if(!hasLower) bobTheBuilder.append(lowerMissing);
			if(!hasDigit) bobTheBuilder.append(digitMissing);
			if(!hasSpecial) bobTheBuilder.append(specialMissing);
			if(!minimumLength) bobTheBuilder.append(tooShortPass);
			JOptionPane.showConfirmDialog(null, bobTheBuilder.toString(), "Password does not meet the minimum requirements", JOptionPane.DEFAULT_OPTION);
			return false;
		}
	}
	/**
	 * Validate password strength before sending to database and updating password but with no dialog box popup if it fails
	 * @param password
	 * @return
	 */
	public static boolean passwordStrengthValidatorNoOutput(char[] password) {
		if(password == null) return false;
		boolean hasUpper = false;
		boolean hasLower = false;
		boolean hasDigit = false;
		boolean hasSpecial = false;
		boolean minimumLength = (password.length >= 8)? true : false;
		for(char c : password) {
			if(Character.isUpperCase(c)) hasUpper = true;
			else if(Character.isLowerCase(c)) hasLower = true;
			else if(Character.isDigit(c)) hasDigit = true;
			else hasSpecial = true;
		}
		if (hasUpper && hasLower && hasDigit && hasSpecial && minimumLength) return true;
		else {
			return false;
		}
	}
}
