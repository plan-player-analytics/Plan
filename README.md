# ![Player Analytics](http://puu.sh/AXSg7/5f2f78c06c.jpg)

[![Build Status](http://plan.djrapitops.com/buildStatus/icon?job=Player+Analytics%2Fmaster)](https://plan.djrapitops.com/blue/organizations/jenkins/Player%20Analytics/activity)
[![Discord](https://img.shields.io/discord/364107873267089409.svg?logo=discord)](https://discord.gg/yXKmjzT)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.djrapitops%3APlan&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.djrapitops%3APlan)  
[![Maintainability](https://sonarcloud.io/api/project_badges/measure?project=com.djrapitops%3APlan&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=com.djrapitops%3APlan)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=com.djrapitops%3APlan&metric=ncloc)](https://sonarcloud.io/dashboard?id=com.djrapitops%3APlan)
[![Code coverage](https://sonarcloud.io/api/project_badges/measure?project=com.djrapitops%3APlan&metric=coverage)](https://sonarcloud.io/dashboard?id=com.djrapitops%3APlan)
[![Duplicate lines](https://sonarcloud.io/api/project_badges/measure?project=com.djrapitops%3APlan&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=com.djrapitops%3APlan)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=com.djrapitops%3APlan&metric=code_smells)](https://sonarcloud.io/dashboard?id=com.djrapitops%3APlan)

Player Analytics is a fully fledged solution for the analytics needs of your server. A built in webserver displays insights into different aspects on the server such as Online activity, Playerbase and how these change over time.

Plan supports multiple different platforms and versions;
Spigot, Paper, Sponge, SpongeForge, Bungeecord, Velocity, Cauldron & all derivatives; from 1.7 to 1.14 - One jar for all platforms.

[API, Documentation & Tutorials](https://github.com/Rsl1122/Plan-PlayerAnalytics/wiki) | [Releases](https://github.com/plan-player-analytics/Plan/releases) | [Issues & Suggestions](https://github.com/Rsl1122/Plan-PlayerAnalytics/issues) | [Jenkins (Development builds)](https://plan.djrapitops.com/job/Player%20Analytics/job/master/)

![Image](https://user-images.githubusercontent.com/24460436/66252533-96d89400-e765-11e9-978f-bc4bd040ab97.jpg)

## Building

You can build the project by running the following in the repository root
```
cd Plan
./gradlew shadowJar
```

- [More information about setting up the project](https://github.com/plan-player-analytics/Plan/wiki/Project-Setup)

## Used Libraries

- **[HighCharts](https://www.highcharts.com/)** | [Free for non-commercial](https://www.highcharts.com/products/highcharts/#non-commercial)
- **[AdminBSB Bootstrap template](https://gurayyarar.github.io/AdminBSBMaterialDesign/index.html)** | [MIT License](https://opensource.org/licenses/MIT)
  - **[Bootstrap](https://v4-alpha.getbootstrap.com/)** | [MIT License](https://v4-alpha.getbootstrap.com/about/license/)
  - **[Materialize](http://materializecss.com/about.html)** | [MIT License](https://github.com/Dogfalo/materialize/blob/master/LICENSE)
  - **[jQuery Datatables](https://datatables.net/)** | [MIT License](https://datatables.net/license/mit)
- **[Font Awesome Icons](http://fontawesome.io/icons/)** | [SIL Open Font License](http://scripts.sil.org/cms/scripts/page.php?site_id=nrsi&id=OFL)
- **[MaxMind GeoIP2](https://www.maxmind.com/en/geoip-demo)** | [Creative Commons Attribution-ShareAlike 4.0 International License](https://creativecommons.org/licenses/by-sa/4.0/)
- **[H2 Database](http://www.h2database.com)** | [MPL 2.0](http://www.h2database.com/html/license.html#mpl2) or [EPL 1.0](http://www.h2database.com/html/license.html#eclipse_license)

## License

Player Analytics is licensed under LGPL-3 (GNU Lesser General Public License)

- [License](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/LICENSE)
