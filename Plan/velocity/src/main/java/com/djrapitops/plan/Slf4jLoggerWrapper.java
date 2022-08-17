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
package com.djrapitops.plan;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

/**
 * Delegates org.slf4j.Logger (unrelocated) to plan.org.slf4j.Logger (relocated).
 *
 * @author AuroraLS3
 */
public class Slf4jLoggerWrapper implements Logger {

    private final com.djrapitops.plan.unrelocate.org.slf4j.Logger logger;

    public Slf4jLoggerWrapper(com.djrapitops.plan.unrelocate.org.slf4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    public String getName() {return logger.getName();}

    @Override
    public LoggingEventBuilder makeLoggingEventBuilder(Level level) {return logger.makeLoggingEventBuilder(level);}

    @Override
    public LoggingEventBuilder atLevel(Level level) {return logger.atLevel(level);}

    @Override
    public boolean isEnabledForLevel(Level level) {return logger.isEnabledForLevel(level);}

    @Override
    public boolean isTraceEnabled() {return logger.isTraceEnabled();}

    @Override
    public void trace(String msg) {logger.trace(msg);}

    @Override
    public void trace(String format, Object arg) {logger.trace(format, arg);}

    @Override
    public void trace(String format, Object arg1, Object arg2) {logger.trace(format, arg1, arg2);}

    @Override
    public void trace(String format, Object... arguments) {logger.trace(format, arguments);}

    @Override
    public void trace(String msg, Throwable t) {logger.trace(msg, t);}

    @Override
    public boolean isTraceEnabled(Marker marker) {return logger.isTraceEnabled(marker);}

    @Override
    public LoggingEventBuilder atTrace() {return logger.atTrace();}

    @Override
    public void trace(Marker marker, String msg) {logger.trace(marker, msg);}

    @Override
    public void trace(Marker marker, String format, Object arg) {logger.trace(marker, format, arg);}

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {logger.trace(marker, format, arg1, arg2);}

    @Override
    public void trace(Marker marker, String format, Object... argArray) {logger.trace(marker, format, argArray);}

    @Override
    public void trace(Marker marker, String msg, Throwable t) {logger.trace(marker, msg, t);}

    @Override
    public boolean isDebugEnabled() {return logger.isDebugEnabled();}

    @Override
    public void debug(String msg) {logger.debug(msg);}

    @Override
    public void debug(String format, Object arg) {logger.debug(format, arg);}

    @Override
    public void debug(String format, Object arg1, Object arg2) {logger.debug(format, arg1, arg2);}

    @Override
    public void debug(String format, Object... arguments) {logger.debug(format, arguments);}

    @Override
    public void debug(String msg, Throwable t) {logger.debug(msg, t);}

    @Override
    public boolean isDebugEnabled(Marker marker) {return logger.isDebugEnabled(marker);}

    @Override
    public void debug(Marker marker, String msg) {logger.debug(marker, msg);}

    @Override
    public void debug(Marker marker, String format, Object arg) {logger.debug(marker, format, arg);}

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {logger.debug(marker, format, arg1, arg2);}

    @Override
    public void debug(Marker marker, String format, Object... arguments) {logger.debug(marker, format, arguments);}

    @Override
    public void debug(Marker marker, String msg, Throwable t) {logger.debug(marker, msg, t);}

    @Override
    public LoggingEventBuilder atDebug() {return logger.atDebug();}

    @Override
    public boolean isInfoEnabled() {return logger.isInfoEnabled();}

    @Override
    public void info(String msg) {logger.info(msg);}

    @Override
    public void info(String format, Object arg) {logger.info(format, arg);}

    @Override
    public void info(String format, Object arg1, Object arg2) {logger.info(format, arg1, arg2);}

    @Override
    public void info(String format, Object... arguments) {logger.info(format, arguments);}

    @Override
    public void info(String msg, Throwable t) {logger.info(msg, t);}

    @Override
    public boolean isInfoEnabled(Marker marker) {return logger.isInfoEnabled(marker);}

    @Override
    public void info(Marker marker, String msg) {logger.info(marker, msg);}

    @Override
    public void info(Marker marker, String format, Object arg) {logger.info(marker, format, arg);}

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {logger.info(marker, format, arg1, arg2);}

    @Override
    public void info(Marker marker, String format, Object... arguments) {logger.info(marker, format, arguments);}

    @Override
    public void info(Marker marker, String msg, Throwable t) {logger.info(marker, msg, t);}

    @Override
    public LoggingEventBuilder atInfo() {return logger.atInfo();}

    @Override
    public boolean isWarnEnabled() {return logger.isWarnEnabled();}

    @Override
    public void warn(String msg) {logger.warn(msg);}

    @Override
    public void warn(String format, Object arg) {logger.warn(format, arg);}

    @Override
    public void warn(String format, Object... arguments) {logger.warn(format, arguments);}

    @Override
    public void warn(String format, Object arg1, Object arg2) {logger.warn(format, arg1, arg2);}

    @Override
    public void warn(String msg, Throwable t) {logger.warn(msg, t);}

    @Override
    public boolean isWarnEnabled(Marker marker) {return logger.isWarnEnabled(marker);}

    @Override
    public void warn(Marker marker, String msg) {logger.warn(marker, msg);}

    @Override
    public void warn(Marker marker, String format, Object arg) {logger.warn(marker, format, arg);}

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {logger.warn(marker, format, arg1, arg2);}

    @Override
    public void warn(Marker marker, String format, Object... arguments) {logger.warn(marker, format, arguments);}

    @Override
    public void warn(Marker marker, String msg, Throwable t) {logger.warn(marker, msg, t);}

    @Override
    public LoggingEventBuilder atWarn() {return logger.atWarn();}

    @Override
    public boolean isErrorEnabled() {return logger.isErrorEnabled();}

    @Override
    public void error(String msg) {logger.error(msg);}

    @Override
    public void error(String format, Object arg) {logger.error(format, arg);}

    @Override
    public void error(String format, Object arg1, Object arg2) {logger.error(format, arg1, arg2);}

    @Override
    public void error(String format, Object... arguments) {logger.error(format, arguments);}

    @Override
    public void error(String msg, Throwable t) {logger.error(msg, t);}

    @Override
    public boolean isErrorEnabled(Marker marker) {return logger.isErrorEnabled(marker);}

    @Override
    public void error(Marker marker, String msg) {logger.error(marker, msg);}

    @Override
    public void error(Marker marker, String format, Object arg) {logger.error(marker, format, arg);}

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {logger.error(marker, format, arg1, arg2);}

    @Override
    public void error(Marker marker, String format, Object... arguments) {logger.error(marker, format, arguments);}

    @Override
    public void error(Marker marker, String msg, Throwable t) {logger.error(marker, msg, t);}

    @Override
    public LoggingEventBuilder atError() {return logger.atError();}
}
