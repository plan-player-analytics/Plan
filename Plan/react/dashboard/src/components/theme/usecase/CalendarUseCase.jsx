import React from 'react';
import PlayerSessionCalendar from "../../calendar/PlayerSessionCalendar.jsx";
import {calculateCssHexColor} from "../../../util/colors.js";

const CalendarUseCase = () => {

    const date = Date.now();
    const day = 24 * 60 * 60 * 1000;
    const series = [{
        title: "html.label.playtime",
        start: date - (date % day),
        end: undefined,
        color: calculateCssHexColor("var(--color-data-play-playtime)"),
        value: 12345678
    }]
    for (let i = 0; i < 10; i++) {
        const playtime = 12345678 / 10;
        const offset = ((date % day) / 10) * i;
        series.push({
            title: "html.label.session",
            start: date - offset,
            end: (date - offset) + playtime,
            color: calculateCssHexColor("var(--color-data-play-sessions)"),
            value: playtime
        })
    }
    return (
        <div>
            <div>
                <PlayerSessionCalendar series={series} firstDay={1}/>
            </div>
        </div>
    )
};

export default CalendarUseCase