import React from 'react';
import {useTranslation} from "react-i18next";

const PlayerRetentionGraphHelp = () => {
    const {t} = useTranslation();

    return (
        <>
            <h3>{t('html.label.retention.timeStep')}</h3>
            <p>This option sets how many points the graph has. Example: 'Days' adds points 24h apart.</p>
            <hr/>
            <h3>{t('html.label.retention.playersRegisteredInTime')}</h3>
            <p>This option filters the data you have. Example: 'in the last 7 days' displays data of players who
                registered in the last 7 days.</p>
            <hr/>
            <h3>{t('html.label.retention.groupByTime')}</h3>
            <p>After filtering, the data is grouped into series so that different months or weeks can be compared.
                Example: 'Month' groups the players who registered in April 2023 to a line 2023-04.</p>
            <hr/>
            <h3>{t('html.label.xAxis')}</h3>
            <ul>
                <li><b>{t('html.label.retention.timeSinceRegistered')}</b> is the most intuitive option. It shows how
                    many players were still playing at the specific point. Example: 5 Days 24.53% - About
                    1/4th of players are still joining the server 5 days after they registered.
                </li>
                <li><b>{t('html.label.playtime')}</b> Playtime as X axis shows how long players play before they quit
                    joining the server. Example: 2 Hours: 2023-04 - 5%, 5% of players who registered in April 2023 have
                    played for more than 2 hours on the server.
                </li>
                <li><b>{t('html.label.time.date')}</b> Date as X axis shows how long players have kept playing on a
                    timeline. This can be used to
                    contextualize the information from {t('html.label.retention.timeSinceRegistered')} view. Example:
                    2023-05-13: 2023-04 - 5%, 5% of players who registered in April 2023 were still playing on 13th of
                    May 2023
                </li>
            </ul>
            <p></p>
            <hr/>
            <h3>{t('html.label.yAxis')}</h3>
            <ul>
                <li><b>{t('html.label.unit.percentage')}</b> % of players in the group who had not stopped playing.</li>
                <li><b>{t('html.label.unit.playerCount')}</b> Number of players in the group who had not stopped
                    playing. This can be used as context for the
                    percentage, since there may be more new players in different months.
                </li>
                <li><b>{t('html.label.unit.playerCount') + ' + ' + t('html.label.registered')}</b> Number of players in
                    the group who had registered, and had not stopped playing. This can be used to
                    see how many new players were gained each month (the peak). This option only works with 'Date' X
                    axis
                </li>
            </ul>
            <hr/>
            <h3>Graph options</h3>
            <ul>
                <li>You can Zoom in by click + dragging on the graph.</li>
                <li>You can hide/show a series by clicking on the label at the bottom.</li>
            </ul>
        </>
    )
};

export default PlayerRetentionGraphHelp