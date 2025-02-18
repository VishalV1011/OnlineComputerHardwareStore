import java.util.regex.Pattern;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    // Password validation regex: at least 1 uppercase, 1 lowercase, 1 digit, 1 special character, minimum 8 characters
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    private static final int SALT_LENGTH = 16; // 16-byte salt for secure hashing

    /**
     * Validates the password against a secure pattern.
     *
     * @param password The password to validate.
     * @return True if the password meets the security requirements, false otherwise.
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return Pattern.matches(PASSWORD_PATTERN, password);
    }

    /**
     * Hashes a password using SHA-256 with a unique salt.
     *
     * @param password The password to hash.
     * @return The salted hash in the format: "salt:hashedPassword".
     * @throws Exception If hashing fails.
     */
    public static String hashPassword(String password) throws Exception {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        byte[] salt = generateSalt();
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hash = hashWithSalt(password, salt);
        return saltBase64 + ":" + hash; // Combine salt and hash for storage
    }

    /**
     * Verifies a password against a stored salted hash.
     *
     * @param password       The plain text password to verify.
     * @param saltedHash The stored salted hash in the format: "salt:hashedPassword".
     * @return True if the password matches the hash, false otherwise.
     * @throws Exception If verification fails.
     */
    public static boolean verifyPassword(String password, String saltedHash) throws Exception {
        if (password == null || saltedHash == null) {
            throw new IllegalArgumentException("Password and saltedHash cannot be null");
        }
        String[] parts = saltedHash.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid salted hash format");
        }
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        String storedHash = parts[1];
        String computedHash = hashWithSalt(password, salt);
        return storedHash.equals(computedHash);
    }

    /**
     * Generates a secure random salt.
     *
     * @return A byte array representing the salt.
     */
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hashes a password with a given salt using SHA-256.
     *
     * @param password The password to hash.
     * @param salt     The salt to use for hashing.
     * @return The hashed password as a hexadecimal string.
     * @throws Exception If hashing fails.
     */
    private static String hashWithSalt(String password, byte[] salt) throws Exception {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt); // Apply the salt
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
