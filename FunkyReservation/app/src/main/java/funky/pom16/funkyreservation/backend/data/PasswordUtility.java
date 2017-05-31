package funky.pom16.funkyreservation.backend.data;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Utility class for hashing passwords handed over. Uses SHA-1 for hashing
 *
 * @author Sebastian Reichl
 * @since 07-06-2016
 * @version 0.3
 */
public class PasswordUtility {

    /**
     * Hashes a password using SHA-1.
     *
     * @param password The password in plain text
     * @return The SHA-1 hashed password
     */
    public static String hashPassword(String password) {
        String sha1 = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(password.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        }
        catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return sha1;
    }

    /**
     * Converts a byte array into a hexString
     * @param hash The byte array containing a hash
     * @return A hexString representation
     */
    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
