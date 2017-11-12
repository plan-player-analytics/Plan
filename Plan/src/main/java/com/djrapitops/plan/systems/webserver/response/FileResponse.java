/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.response;

import main.java.com.djrapitops.plan.utilities.file.FileUtil;

import java.io.IOException;

/**
 * Response class for returning file contents.
 * <p>
 * Created to remove copy-paste.
 *
 * @author Rsl1122
 * @since 4.0.0
 */
public class FileResponse extends Response {

    public FileResponse(String fileName) {
        super.setHeader("HTTP/1.1 200 OK");
        try {
            super.setContent(FileUtil.getStringFromResource(fileName));
        } catch (IOException e) {
            super.setContent(new NotFoundResponse(fileName + " was not found inside the .jar or /plugins/Plan/ folder").getContent());
        }
    }
}