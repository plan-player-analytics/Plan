import React from 'react';
import {useTranslation} from "react-i18next";
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchJoinAddressByDay} from "../../../../service/serverService";
import {ErrorViewCard} from "../../../../views/ErrorView";
import {CardLoader} from "../../../navigation/Loader";
import {Card} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faChartColumn} from "@fortawesome/free-solid-svg-icons";
import JoinAddressGraph from "../../../graphs/JoinAddressGraph";

const JoinAddressGraphCard = () => {
    const {t} = useTranslation();
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchJoinAddressByDay, [identifier]);

    if (loadingError) return <ErrorViewCard error={loadingError}/>
    if (!data) return <CardLoader/>;

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black" style={{width: '100%'}}>
                    <Fa icon={faChartColumn} className="col-amber"/> {t('html.label.joinAddresses')}
                </h6>
            </Card.Header>
            <JoinAddressGraph id={'join-address-graph'} data={data?.join_addresses_by_date} colors={data?.colors}/>
        </Card>
    )
};

export default JoinAddressGraphCard