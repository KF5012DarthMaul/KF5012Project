package kf5012darthmaulapplication;

import org.bouncycastle.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import org.bouncycastle.*;
import org.bouncycastle.jce.provider.*;

public class SecurityManager {
	private static final int ITERATION_COUNT = 1000;
	
	public SecurityManager(){
	}
	public void GUISecurityErrorDialog(String message) {
		 new ErrorDialog("An Error Occured", new Error("Security Failure: " + message));
	}
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
	private static byte[] getSalt() throws Exception{
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt;
	}
	private static String toHex(byte[] bytes){
		StringBuilder sb = new StringBuilder (bytes.length * 2);
		for(byte b : bytes) {
			sb.append(String.format("%02x",b));
		}
		return sb.toString();
	}
	private static byte[] fromHex(String hex){
		byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++) {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
	}
	public static String generatePassword(String password) throws Exception {
		int iterations = ITERATION_COUNT;
		char[] charArr = password.toCharArray();
		byte[] salt = getSalt();
		
		PBEKeySpec spec = new PBEKeySpec(charArr, salt, iterations, 256);
		SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		byte[] hash = keyFac.generateSecret(spec).getEncoded();
		
		return iterations + ":" + toHex(salt) + ":" + toHex(hash);
	}
	public static boolean validatePassword(String password, String storedPassword) throws Exception {
		String[] passwordSegments = storedPassword.split(":");
		int iterations = Integer.parseInt(passwordSegments[0]);
		byte[] salt = fromHex(passwordSegments[1]);
		byte[] hash = fromHex(passwordSegments[2]);
		
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, hash.length * 8);
		SecretKeyFactory keyFac =  SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		
		byte[] testHash = keyFac.generateSecret(spec).getEncoded();
		int diff = hash.length ^ testHash.length;
		for(int i = 0; i < hash.length && i < testHash.length; i++) {
			diff |= hash[i] ^ testHash[i];
		}
		return diff == 0;
	}
}
