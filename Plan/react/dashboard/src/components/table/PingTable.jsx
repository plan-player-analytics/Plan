import React from "react";
import {useTranslation} from "react-i18next";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faGlobe, faSignal} from "@fortawesome/free-solid-svg-icons";
import {formatDecimals} from "../../util/formatters.js";
import {usePreferences} from "../../hooks/preferencesHook.jsx";
import DataTablesTable from "./DataTablesTable.jsx";

const PingTable = ({countries}) => {
    const {t} = useTranslation();
    const {preferencesLoaded, decimalFormat} = usePreferences();

    const columns = [{
        title: <><Fa icon={faGlobe}/> {t('html.label.country')}</>,
        data: "country"
    }, {
        title: <><Fa icon={faSignal}/> {t('html.label.averagePing')}</>,
        data: {_: "pingAverage", display: "pingAverageFormatted"}
    }, {
        title: <><Fa icon={faSignal}/> {t('html.label.bestPing')}</>,
        data: {_: "pingMin", display: "pingMinFormatted"}
    }, {
        title: <><Fa icon={faSignal}/> {t('html.label.worstPing')}</>,
        data: {_: "pingMax", display: "pingMaxFormatted"}
    }];

    const rows = countries.map(country => {
        return {
            country: country.country,
            pingAverage: country.avg_ping,
            pingAverageFormatted: formatDecimals(country.avg_ping, decimalFormat) + " ms",
            pingMax: country.max_ping,
            pingMaxFormatted: country.max_ping + " ms",
            pingMin: country.min_ping,
            pingMinFormatted: country.min_ping + " ms"
        };
    });
    const options = {
        responsive: true,
        deferRender: true,
        columns: columns,
        data: rows,
        paginationCount: 2,
        order: [[0, "desc"]]
    }

    if (!preferencesLoaded) return <></>;

    return (
        <DataTablesTable id={"ping-table"} options={options} colorClass={"bg-ping"}/>
    )
};

export default PingTable;