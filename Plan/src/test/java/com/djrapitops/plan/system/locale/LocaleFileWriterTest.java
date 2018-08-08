package com.djrapitops.plan.system.locale;

import com.djrapitops.plan.utilities.file.FileUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link LocaleFileWriter}.
 *
 * @author Rsl1122
 */
public class LocaleFileWriterTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void writesAllIdentifiers() throws IOException {
        File file = temporaryFolder.newFile();
        new LocaleFileWriter(new Locale()).writeToFile(file);

        long expected = LocaleSystem.getIdentifiers().size();
        int result = FileUtil.lines(file, Charset.forName("UTF-8")).size();
        assertEquals(expected, result);
    }

}