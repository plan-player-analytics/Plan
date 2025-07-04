import React from "react";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchPlayerbaseDevelopmentGraph} from "../../../../service/serverService";
import {ErrorViewCard} from "../../../../views/ErrorView";
import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faUsers} from "@fortawesome/free-solid-svg-icons";
import {CardLoader} from "../../../navigation/Loader";
import GroupVisualizer from "../../../graphs/GroupVisualizer";

export const CurrentPlayerbaseCardWithData = ({data, title}) => {
    const {t} = useTranslation();
    return (
        <Card id={"playerbase-current"}>
            <Card.Header>
                <h6 className="col-text" style={{width: '100%'}}>
                    <Fa icon={faUsers}
                        className="col-players-activity-index"/> {t(title ? title : 'html.label.currentPlayerbase')}
                </h6>
            </Card.Header>
            <GroupVisualizer groups={data.activity_pie_series} name={t('html.label.players')}/>
        </Card>
    )
}

const CurrentPlayerbaseCard = ({identifier}) => {
    const {data, loadingError} = useDataRequest(fetchPlayerbaseDevelopmentGraph, [identifier]);

    if (loadingError) return <ErrorViewCard error={loadingError}/>
    if (!data) return <CardLoader/>;

    return (
        <CurrentPlayerbaseCardWithData data={data}/>
    )
}

export default CurrentPlayerbaseCard;