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
package utilities;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class HTTPConnector {

    private static final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }
            }
    };

    private SSLSocketFactory getRelaxedSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        return sc.getSocketFactory();
    }

    public HttpURLConnection getConnection(String method, String address) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        URL url = URI.create(address).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (address.startsWith("https")) {
            HttpsURLConnection httpsConn = (HttpsURLConnection) connection;

            // Disables unsigned certificate & hostname check, because we're trusting the user given certificate.
            // This allows https connections internally to local ports.
            httpsConn.setHostnameVerifier((hostname, session) -> true);
            httpsConn.setSSLSocketFactory(getRelaxedSocketFactory());
        }
        connection.setConnectTimeout(10000);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod(method);
        connection.setUseCaches(false);

        return connection;
    }

}
