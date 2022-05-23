import React from "react";
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchPlayerbaseDevelopmentGraph} from "../../../../service/serverService";
import {ErrorViewBody} from "../../../../views/ErrorView";
import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faUsers} from "@fortawesome/free-solid-svg-icons";

const CurrentPlayerbaseCard = () => {
    const {t} = useTranslation();
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchPlayerbaseDevelopmentGraph, [identifier]);

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <></>;

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black" style={{width: '100%'}}>
                    <Fa icon={faUsers} className="col-amber"/> {t('html.label.currentPlayerbase')}
                </h6>
            </Card.Header>

        </Card>
    )
}

export default CurrentPlayerbaseCard;