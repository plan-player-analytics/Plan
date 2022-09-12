import React from 'react';
import {useTranslation} from "react-i18next";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchJoinAddressPie} from "../../../../service/serverService";
import {ErrorViewCard} from "../../../../views/ErrorView";
import {CardLoader} from "../../../navigation/Loader";
import {Card} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faLocationArrow} from "@fortawesome/free-solid-svg-icons";
import GroupVisualizer from "../../../graphs/GroupVisualizer";

const JoinAddressGroupCard = ({identifier}) => {
    const {t} = useTranslation();

    const {data, loadingError} = useDataRequest(fetchJoinAddressPie, [identifier]);

    if (loadingError) return <ErrorViewCard error={loadingError}/>
    if (!data) return <CardLoader/>;

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black" style={{width: '100%'}}>
                    <Fa icon={faLocationArrow} className="col-amber"/> {t('html.label.latestJoinAddresses')}
                </h6>
            </Card.Header>
            <GroupVisualizer groups={data.slices} colors={data.colors}/>
        </Card>
    )
};

export default JoinAddressGroupCard