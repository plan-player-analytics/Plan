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
package com.djrapitops.plan.delivery.domain.datatransfer;

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.utilities.dev.Untrusted;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Represents query page view that the user wants to see data for.
 */
@Untrusted
public class ViewDto {
    private static final String DATE_PATTERN = "dd/MM/yyyy kk:mm";

    private final String afterDate;
    private final String afterTime;
    private final String beforeDate;
    private final String beforeTime;
    private final List<ServerDto> servers;

    public ViewDto(Formatters formatters, List<ServerDto> servers) {
        this.servers = servers;
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30);

        Formatter<Long> formatter = formatters.javascriptDateFormatterLong();
        String[] after = StringUtils.split(formatter.apply(monthAgo), " ");
        String[] before = StringUtils.split(formatter.apply(now), " ");

        this.afterDate = after[0];
        this.afterTime = after[1];
        this.beforeDate = before[0];
        this.beforeTime = before[1];
    }

    public long getAfterEpochMs() throws ParseException {
        return new SimpleDateFormat(DATE_PATTERN).parse(afterDate + " " + afterTime).getTime();
    }

    public long getBeforeEpochMs() throws ParseException {
        return new SimpleDateFormat(DATE_PATTERN).parse(beforeDate + " " + beforeTime).getTime();
    }

    public List<ServerUUID> getServerUUIDs() {
        return servers.stream()
                .map(ServerDto::getServerUUID)
                .map(ServerUUID::fromString)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ViewDto viewDto = (ViewDto) o;
        return Objects.equals(afterDate, viewDto.afterDate) && Objects.equals(afterTime, viewDto.afterTime) && Objects.equals(beforeDate, viewDto.beforeDate) && Objects.equals(beforeTime, viewDto.beforeTime) && Objects.equals(servers, viewDto.servers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(afterDate, afterTime, beforeDate, beforeTime, servers);
    }
}
