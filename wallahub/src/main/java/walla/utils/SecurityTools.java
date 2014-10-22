package walla.utils;

import java.security.SecureRandom;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;

public class SecurityTools
{
	public static String GetRandomKey(int length)
	{
		SecureRandom random = new SecureRandom();
	    byte bytes[] = new byte[length];
	    random.nextBytes(bytes);
	    
	    return bytes.toString();
	}
	
    public static String GenerateSalt() throws NoSuchAlgorithmException {
	   SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
	   
	   // Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
	   byte[] salt = new byte[32];
	   random.nextBytes(salt);

	   return Base64.encodeBase64String(salt);
    }
	
    public static String GetHashedPassword(String password, String salt, int keyLength, int iterations)
    	      throws NoSuchAlgorithmException, InvalidKeySpecException 
    {
	     // PBKDF2 with SHA-1 as the hashing algorithm. Note that the NIST
	     // specifically names SHA-1 as an acceptable hashing algorithm for PBKDF2
	     final String algorithm = "PBKDF2WithHmacSHA1";
	
	     // SHA-1 generates 160 bit hashes, so that's what makes sense here
		 //int derivedKeyLength = 160;
		 //int iterations = 10000;

		 KeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.decodeBase64(salt), iterations, keyLength);
		
		 SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);
		 return Base64.encodeBase64String(f.generateSecret(spec).getEncoded());
    }
    
    public static boolean SlowEquals(byte[] a, byte[] b)
    {
        int diff = a.length ^ b.length;
        for(int i = 0; i < a.length && i < b.length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }
}