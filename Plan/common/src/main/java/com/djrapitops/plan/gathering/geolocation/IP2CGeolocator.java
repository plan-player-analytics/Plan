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
package com.djrapitops.plan.gathering.geolocation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Fallback {@link Geolocator} implementation using ip2c.
 *
 * @author Rsl1122
 * @see <a href="about.ip2c.org"></a>
 */
@Singleton
public class IP2CGeolocator implements Geolocator {

    @Inject
    public IP2CGeolocator() {
        // Inject constructor required for Dagger
    }

    @Override
    public void prepare() throws IOException {
        // Avoid Socket leak with the parameters in case download url has proxy
        // https://rsl1122.github.io/mishaps/java_socket_leak_incident
        Properties properties = System.getProperties();
        properties.setProperty("sun.net.client.defaultConnectTimeout", Long.toString(TimeUnit.MINUTES.toMillis(1L)));
        properties.setProperty("sun.net.client.defaultReadTimeout", Long.toString(TimeUnit.MINUTES.toMillis(1L)));
        properties.setProperty("sun.net.http.retryPost", Boolean.toString(false));

        // Run a test to see if Internet is available.
        readIPFromURL("0.0.0.0");
    }

    @Override
    public Optional<String> getCountry(InetAddress inetAddress) {
        if (inetAddress instanceof Inet6Address) return Optional.empty();
        String address = inetAddress.getHostAddress();
        return getCountry(address);
    }

    @Override
    public Optional<String> getCountry(String address) {
        try {
            return readIPFromURL(address);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public Optional<String> readIPFromURL(String address) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL("http://ip2c.org/" + address).openConnection();
            connection.setConnectTimeout((int) TimeUnit.MINUTES.toMillis(1L));
            connection.setReadTimeout((int) TimeUnit.MINUTES.toMillis(1L));
            connection.setUseCaches(false);
            connection.connect();
            try (
                    InputStream in = connection.getInputStream();
                    OutputStream out = connection.getOutputStream()
            ) {
                String answer = readAnswer(in);
                out.close();
                return resolveIP(answer);
            }
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public Optional<String> resolveIP(String s) {
        switch (s.charAt(0)) {
            case '1':
                String[] reply = s.split(";");
                return reply.length >= 4 ? Optional.of(reply[3]) : Optional.empty();
            case '0': // No reply
            case '2': // Not in database
            default:  // Not known char
                return Optional.empty();
        }
    }

    public String readAnswer(InputStream is) throws IOException {
        int read;
        StringBuilder answer = new StringBuilder();
        while ((read = is.read()) != -1) answer.append((char) read);
        return answer.toString();
    }
}