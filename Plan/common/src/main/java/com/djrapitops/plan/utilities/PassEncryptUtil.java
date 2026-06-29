/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.utilities;

import com.djrapitops.plan.exceptions.PassEncryptException;
import com.djrapitops.plan.utilities.dev.Untrusted;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Password Encryption utility.
 * <p>
 * <a href="https://github.com/defuse/password-hashing/blob/master/PasswordStorage.java">Based on this code</a>
 *
 * @author Defuse
 */
public class PassEncryptUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    // These constants may be changed without breaking existing hashes.
    private static final int SALT_BYTE_SIZE = 24;
    private static final int HASH_BYTE_SIZE = 18;
    private static final int PBKDF2_ITERATIONS = 64000;
    // These constants define the encoding and may not be changed.
    private static final int HASH_SECTIONS = 5;
    private static final int HASH_ALGORITHM_INDEX = 0;
    private static final int ITERATION_INDEX = 1;
    private static final int HASH_SIZE_INDEX = 2;
    private static final int SALT_INDEX = 3;
    private static final int PBKDF2_INDEX = 4;

    /**
     * Constructor used to hide the public constructor
     */
    private PassEncryptUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Create a hash of password + salt.
     *
     * @param password Password
     * @return Hash + salt
     * @throws CannotPerformOperationException If the hash creation fails
     */
    public static String createHash(@Untrusted String password) {
        return createHash(password.toCharArray());
    }

    private static String createHash(char[] password) {
        // Generate a random salt
        byte[] salt = new byte[SALT_BYTE_SIZE];
        RANDOM.nextBytes(salt);

        // Hash the password
        byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE);
        int hashSize = hash.length;

        // format: algorithm:iterations:hashSize:salt:hash
        return "sha1:"
                + PBKDF2_ITERATIONS
                + ":" + hashSize
                + ":" + toBase64(salt)
                + ":" + toBase64(hash);
    }

    /**
     * Verify that a password matches a hash.
     *
     * @param password    Password
     * @param correctHash hash created with {@link PassEncryptUtil#createHash(String)}
     * @return true if match
     * @throws CannotPerformOperationException If hashing fails
     * @throws InvalidHashException            If the hash is missing details.
     */
    public static boolean verifyPassword(@Untrusted String password, String correctHash) {
        return verifyPassword(password.toCharArray(), correctHash);
    }

    private static boolean verifyPassword(char[] password, String correctHash) {
        // Decode the hash into its parameters
        String[] params = StringUtils.split(correctHash, ':');
        if (params.length != HASH_SECTIONS) {
            throw new InvalidHashException(
                    "Fields are missing from the password hash."
            );
        }

        // Currently, Java only supports SHA1.
        if (!params[HASH_ALGORITHM_INDEX].equals("sha1")) {
            throw new CannotPerformOperationException(
            );
        }

        int iterations;
        try {
            iterations = Integer.parseInt(params[ITERATION_INDEX]);
        } catch (NumberFormatException ex) {
            throw new InvalidHashException(
                    "Could not parse the iteration count as an integer.", ex
            );
        }

        if (iterations < 1) {
            throw new InvalidHashException(
                    "Invalid number of iterations. Must be >= 1."
            );
        }

        byte[] salt;
        try {
            salt = fromBase64(params[SALT_INDEX]);
        } catch (IllegalArgumentException ex) {
            throw new InvalidHashException(
                    "Base64 decoding of salt failed.", ex
            );
        }

        byte[] hash;
        try {
            hash = fromBase64(params[PBKDF2_INDEX]);
        } catch (IllegalArgumentException ex) {
            throw new InvalidHashException(
                    "Base64 decoding of pbkdf2 output failed.", ex
            );
        }

        int storedHashSize;
        try {
            storedHashSize = Integer.parseInt(params[HASH_SIZE_INDEX]);
        } catch (NumberFormatException ex) {
            throw new InvalidHashException(
                    "Could not parse the hash size as an integer.", ex
            );
        }

        if (storedHashSize != hash.length) {
            throw new InvalidHashException(
                    "Hash length doesn't match stored hash length."
            );
        }

        // Compute the hash of the provided password, using the same salt,
        // iteration count, and hash length
        byte[] testHash = pbkdf2(password, salt, iterations, hash.length);
        // Compare the hashes in constant time. The password is correct if
        // both hashes match.
        return slowEquals(hash, testHash);
    }

    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException ex) {
            throw new CannotPerformOperationException(
                    "Hash algorithm not supported: " + PBKDF2_ALGORITHM, ex
            );
        } catch (InvalidKeySpecException ex) {
            throw new CannotPerformOperationException(
                    "Invalid key spec.", ex
            );
        }
    }

    private static byte[] fromBase64(String hex) {
        return Base64.getDecoder().decode(hex);
    }

    private static String toBase64(byte[] array) {
        return Base64.getEncoder().encodeToString(array);
    }

    @SuppressWarnings("serial")
    public static class InvalidHashException extends PassEncryptException {

        InvalidHashException(String message) {
            super(message);
        }

        InvalidHashException(String message, Throwable source) {
            super(message, source);
        }
    }

    @SuppressWarnings("serial")
    public static class CannotPerformOperationException extends PassEncryptException {

        CannotPerformOperationException() {
            super("Unsupported hash type.");
        }

        CannotPerformOperationException(String message, Throwable source) {
            super(message, source);
        }
    }

}
