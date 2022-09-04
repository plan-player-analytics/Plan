import {useTranslation} from "react-i18next";
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchPlayerbaseDevelopmentGraph} from "../../../../service/serverService";
import {ErrorViewCard} from "../../../../views/ErrorView";
import {Card} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faChartLine} from "@fortawesome/free-solid-svg-icons";
import React from "react";
import PlayerbaseGraph from "../../../graphs/PlayerbaseGraph";
import {CardLoader} from "../../../navigation/Loader";

const PlayerbaseDevelopmentCard = () => {
    const {t} = useTranslation();
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(
        fetchPlayerbaseDevelopmentGraph,
        [identifier])

    if (loadingError) return <ErrorViewCard error={loadingError}/>
    if (!data) return <CardLoader/>;

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa className="col-amber" icon={faChartLine}/> {t('html.label.playerbaseDevelopment')}
                </h6>
            </Card.Header>
            <PlayerbaseGraph data={data}/>
        </Card>
    )
}

export default PlayerbaseDevelopmentCard;