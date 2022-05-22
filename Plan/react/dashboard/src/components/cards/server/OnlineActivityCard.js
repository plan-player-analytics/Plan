import {useTranslation} from "react-i18next";
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../../hooks/dataFetchHook";
import {fetchPlayersOnlineGraph} from "../../../service/serverService";
import {ErrorViewCard} from "../../../views/ErrorView";
import {Card} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faChartArea} from "@fortawesome/free-solid-svg-icons";
import PlayersOnlineGraph from "../../graphs/PlayersOnlineGraph";
import React from "react";

const OnlineActivityCard = () => {
    const {t} = useTranslation();
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(
        fetchPlayersOnlineGraph,
        [identifier])

    if (!data) return <></>;
    if (loadingError) return <ErrorViewCard error={loadingError}/>

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa className="col-blue" icon={faChartArea}/> {t('html.label.onlineActivity')}
                </h6>
            </Card.Header>
            <PlayersOnlineGraph data={data}/>
        </Card>
    )
}

export default OnlineActivityCard;