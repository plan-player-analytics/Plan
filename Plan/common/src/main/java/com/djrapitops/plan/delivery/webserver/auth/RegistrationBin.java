package com.djrapitops.plan.delivery.webserver.auth;

import com.djrapitops.plan.delivery.domain.WebUser;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RegistrationBin {

    private static final Map<String, AwaitingForRegistration> REGISTRATION_BIN = new HashMap<>();

    public static String addInfoForRegistration(String username, String password) throws PassEncryptUtil.CannotPerformOperationException {
        String hash = PassEncryptUtil.createHash(password);
        String code = DigestUtils.sha256Hex(username + password + System.currentTimeMillis()).substring(0, 12);
        REGISTRATION_BIN.put(code, new AwaitingForRegistration(username, hash));
        return code;
    }

    public static Optional<WebUser> register(String code, int permissionLevel) {
        AwaitingForRegistration found = REGISTRATION_BIN.get(code);
        if (found == null) return Optional.empty();
        REGISTRATION_BIN.remove(code);
        return Optional.of(found.toWebUser(permissionLevel));
    }

    public static boolean contains(String code) {
        return REGISTRATION_BIN.containsKey(code);
    }

    private static class AwaitingForRegistration {
        private final String username;
        private final String passwordHash;

        public AwaitingForRegistration(String username, String passwordHash) {
            this.username = username;
            this.passwordHash = passwordHash;
        }

        public WebUser toWebUser(int permissionLevel) {
            return new WebUser(username, passwordHash, permissionLevel);
        }
    }
}
