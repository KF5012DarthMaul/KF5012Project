package kf5012darthmaulapplication;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import org.bouncycastle.*;
import org.bouncycastle.jce.provider.*;

public class SecurityManager {
	
	public SecurityManager(){
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
	

}
