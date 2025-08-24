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

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

/**
 * Utility for encrypting strings with jasypt
 *
 * @author AuroraLS3
 */
public class JasyptEncryptUtility {

    public static void main(String[] args) {
        String input = "";
        String password = "";

        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(password);
        String output = encryptor.encrypt(input);
        System.out.println("Input:");
        System.out.println(input);
        System.out.println("Output:");
        System.out.println(output);
    }

}
