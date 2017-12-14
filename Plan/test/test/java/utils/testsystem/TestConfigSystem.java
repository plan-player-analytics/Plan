/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package test.java.utils.testsystem;

import main.java.com.djrapitops.plan.systems.file.config.ConfigSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class TestConfigSystem extends ConfigSystem {

    @Override
    protected void copyDefaults() throws IOException {
        File file = new File(getClass().getResource("/config.yml").getPath());
        Scanner scanner = new Scanner(new FileInputStream(file));

        List<String> lines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        config.copyDefaults(lines);
    }
}