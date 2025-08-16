import React from 'react';
import {useTranslation} from "react-i18next";
import {useTheme} from "../../../hooks/themeHook";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faGlobe, faWifi} from "@fortawesome/free-solid-svg-icons";
import Scrollable from "../../Scrollable";
import {faClock} from "@fortawesome/free-regular-svg-icons";
import FormattedDate from "../../text/FormattedDate.jsx";
import {localeService, reverseRegionLookupMap} from "../../../service/localeService.js";

const ConnectionsCard = ({connections}) => {
    const {t} = useTranslation();
    const {nightModeEnabled} = useTheme();

    const regions = new Intl.DisplayNames([localeService.getIntlFriendlyLocale()], {type: 'region'});

    return (
        <Card>
            <Card.Header>
                <h6 className="col-text">
                    <Fa icon={faWifi}/> {t('html.label.connectionInfo')}
                </h6>
            </Card.Header>
            <Scrollable>
                <table className={"table table-striped mb-0" + (nightModeEnabled ? " table-dark" : '')}>
                    <thead className="bg-geolocation">
                    <tr>
                        <th><Fa icon={faGlobe}/> {t('html.label.country')}</th>
                        <th><Fa icon={faClock}/> {t('html.label.lastConnected')}</th>
                    </tr>
                    </thead>
                    {Boolean(connections?.length) && <tbody>
                    {connections.map(connection => {
                        const code = reverseRegionLookupMap[connection.geolocation];
                        const location = code ? regions.of(code) : connection.geolocation.replace('Local Machine', t('html.value.localMachine'));

                        return (<tr key={JSON.stringify(connection)}>
                            <td>{location}</td>
                            <td><FormattedDate date={connection.date}/></td>
                        </tr>)
                    })}
                    </tbody>}
                    {!connections?.length && <tbody>
                    <tr>
                        <td colSpan={2}>{t('generic.noData')}</td>
                    </tr>
                    </tbody>}
                </table>
            </Scrollable>
        </Card>
    )
}

export default ConnectionsCard