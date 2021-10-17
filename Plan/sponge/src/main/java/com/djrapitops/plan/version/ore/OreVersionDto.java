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
package com.djrapitops.plan.version.ore;

import java.util.List;

public class OreVersionDto {
  private final String name;
  private final List<OreTagDto> tags;

  public OreVersionDto(String name, List<OreTagDto> tags) {
    this.name = name;
    this.tags = tags;
  }

  public String getName() {
    return name;
  }

  public List<OreTagDto> getTags() {
    return tags;
  }

  @Override
  public String toString() {
    return "OreVersionDto{" +
           "name='" + name + '\'' +
           ", tags=" + tags +
           '}';
  }
}
