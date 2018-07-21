/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.system.webserver.response.errors.NotFoundResponse;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plugin.utilities.Verify;

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

    public static String format(String fileName) {
        String[] split = fileName.split("/");
        int i;
        for (i = 0; i < split.length; i++) {
            String s = split[i];
            if (Verify.equalsOne(s, "css", "js", "plugins", "scss")) {
                break;
            }
        }
        StringBuilder b = new StringBuilder("web");
        for (int j = i; j < split.length; j++) {
            String s = split[j];
            b.append("/").append(s);
        }
        return b.toString();
    }
}
