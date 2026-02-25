import {useTranslation} from "react-i18next";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchPlayerbaseDevelopmentGraph} from "../../../../service/serverService";
import {ErrorViewCard} from "../../../../views/ErrorView.tsx";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faChartLine} from "@fortawesome/free-solid-svg-icons";
import React, {useCallback} from "react";
import PlayerbaseGraph from "../../../graphs/PlayerbaseGraph";
import {CardLoader} from "../../../navigation/Loader";
import {faQuestionCircle} from "@fortawesome/free-regular-svg-icons";
import {useNavigation} from "../../../../hooks/navigationHook";

export const PlayerbaseDevelopmentCardWithData = ({data, title}) => {
    const {t} = useTranslation();
    const {setHelpModalTopic} = useNavigation();

    const openHelp = useCallback(() => setHelpModalTopic('activity-index'), [setHelpModalTopic]);
    return (
        <Card>
            <Card.Header>
                <h6 className="col-text" style={{width: "100%"}}>
                    <Fa className="col-players-activity-index"
                        icon={faChartLine}/> {t(title ? title : 'html.label.playerbaseDevelopment')}
                    <button className={"float-end"} onClick={openHelp}>
                        <Fa className={"col-help-icon"}
                            icon={faQuestionCircle}/>
                    </button>
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