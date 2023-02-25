import {useTranslation} from "react-i18next";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchPlayerbaseDevelopmentGraph} from "../../../../service/serverService";
import {ErrorViewCard} from "../../../../views/ErrorView";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faChartLine} from "@fortawesome/free-solid-svg-icons";
import React from "react";
import PlayerbaseGraph from "../../../graphs/PlayerbaseGraph";
import {CardLoader} from "../../../navigation/Loader";

export const PlayerbaseDevelopmentCardWithData = ({data, title}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa className="col-amber"
                        icon={faChartLine}/> {t(title ? title : 'html.label.playerbaseDevelopment')}
                </h6>
            </Card.Header>
            <PlayerbaseGraph data={data}/>
        </Card>
    )
}

const PlayerbaseDevelopmentCard = ({identifier}) => {
    const {data, loadingError} = useDataRequest(
        fetchPlayerbaseDevelopmentGraph,
        [identifier])

    if (loadingError) return <ErrorViewCard error={loadingError}/>
    if (!data) return <CardLoader/>;

    return (
        <PlayerbaseDevelopmentCardWithData data={data}/>
    )
}

export default PlayerbaseDevelopmentCard;