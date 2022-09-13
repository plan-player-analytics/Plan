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
package net.playeranalytics.plan;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.commands.PlanCommand;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author AuroraLS3
 */
public class ScannerPrompter implements SubSystem {

    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final AtomicReference<String> scannedLine = new AtomicReference<>();
    private Scanner scanner;

    @Override
    public void enable() {
        scanner = new Scanner(System.in);
        while (!shutdown.get() && scanner.hasNext()) {
            synchronized (scannedLine) {
                String scanned = scanner.nextLine();

                if (StringUtils.equalsAny(scanned, "stop", "end", "quit", "exit")) {
                    PlanStandalone.shutdown(0);
                    return; // Ends the loop
                }

                scannedLine.set(scanned);
                scannedLine.notify();
            }
        }
    }

    @Override
    public void disable() {
        scanner.close();
        shutdown.set(true);
    }

    public Optional<String> waitAndGetInput() {
        try {
            String scanned = "";
            while (scanned.trim().isEmpty()) {
                synchronized (scannedLine) {
                    scannedLine.wait();
                    scanned = scannedLine.get();
                }
            }
            return Optional.of(scanned);
        } catch (InterruptedException e) {
            PlanStandalone.shutdown(132);
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    public void insertCommands(PlanCommand planCommand) {
        // TODO enable commands
    }
}
