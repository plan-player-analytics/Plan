/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 AuroraLS3
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.djrapitops.plan.settings.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Configuration utility for storing settings in a .yml file.
 *
 * @author AuroraLS3
 */
public class Config extends ConfigNode {

    private final Path configFilePath;

    public Config(File configFile) {
        super("", null, null);
        this.configFilePath = configFile.toPath();
        Path dir = configFilePath.getParent();

        try {
            if (!Files.isSymbolicLink(dir)) Files.createDirectories(dir);
            if (!Files.exists(configFilePath)) Files.createFile(configFilePath);
            read();
            save();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public Config(File configFile, ConfigNode defaults) {
        this(configFile);
        copyMissing(defaults);
        try {
            save();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    Config() {
        super("", null, null);
        configFilePath = null;
    }

    public boolean fileExists() {
        return Files.exists(configFilePath);
    }

    public void read() throws IOException {
        try (ConfigReader reader = new ConfigReader(Files.newInputStream(configFilePath))) {
            copyAll(reader.read());
        }
    }

    @Override
    public void save() throws IOException {
        new ConfigWriter(configFilePath).write(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), configFilePath);
    }

    public Path getConfigFilePath() {
        return configFilePath;
    }
}
