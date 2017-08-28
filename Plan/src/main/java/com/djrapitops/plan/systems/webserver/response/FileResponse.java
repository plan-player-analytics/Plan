/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.response;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;

import java.io.FileNotFoundException;

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
        } catch (FileNotFoundException e) {
            Log.toLog(this.getClass().getName(), e);
            super.setContent("");
        }
    }
}