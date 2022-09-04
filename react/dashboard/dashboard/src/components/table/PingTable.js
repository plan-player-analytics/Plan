import React from "react";
import {useTheme} from "../../hooks/themeHook";
import {useTranslation} from "react-i18next";

const PingRow = ({country}) => {
    return (
        <tr>
            <td>{country.country}</td>
            <td>{country.avg_ping}</td>
            <td>{country.min_ping}</td>
            <td>{country.max_ping}</td>
        </tr>
    );
}

const PingTable = ({countries}) => {
    const {t} = useTranslation();
    const {nightModeEnabled} = useTheme();

    return (
        <table className={"table mb-0" + (nightModeEnabled ? " table-dark" : '')}>
            <thead className="bg-amber">
            <tr>
                <th>{t('html.label.country')}</th>
                <th>{t('html.label.averagePing')}</th>
                <th>{t('html.label.bestPing')}</th>
                <th>{t('html.label.worstPing')}</th>
            </tr>
            </thead>
            <tbody>
            {countries.length ? countries.map((country, i) => <PingRow key={i} country={country}/>) : <tr>
                <td>{t('generic.noData')}</td>
                <td>-</td>
                <td>-</td>
                <td>-</td>
            </tr>}
            </tbody>
        </table>
    )
};

export default PingTable;