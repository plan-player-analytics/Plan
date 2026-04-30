import React from 'react';
import CardHeader from "../CardHeader.tsx";
import {Card} from "react-bootstrap";
import PlayersOnlineGraph from "../../graphs/PlayersOnlineGraph";
import {faChartArea} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import {useDataRequest} from "../../../hooks/dataFetchHook.js";
import {fetchPlayersOnlineGraph} from "../../../service/serverService.js";
import {ErrorViewCard} from "../../../views/ErrorView.tsx";
import {CardLoader} from "../../navigation/Loader.tsx";
import {useAuth} from "../../../hooks/authenticationHook.tsx";

const QuickViewGraphCard = ({server}) => {
    const {t} = useTranslation();
    const {hasPermission} = useAuth();
    const {data, loadingError} = useDataRequest(
        fetchPlayersOnlineGraph,
        [server.serverUUID],
        hasPermission('page.server.overview.players.online.graph'))

    if (loadingError) return <ErrorViewCard error={loadingError}/>
    if (!data) return <CardLoader/>;

    return (
        <Card>
            <CardHeader icon={faChartArea} color={'players-online'}
                        label={server.serverName + ' ' + t('html.label.onlineActivity')}/>
            <PlayersOnlineGraph data={data} identifier={server.serverUUID} showPlayersOnline/>
        </Card>
    )
};

export default QuickViewGraphCard