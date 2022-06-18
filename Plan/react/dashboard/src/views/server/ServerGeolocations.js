import React from 'react';
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchGeolocations} from "../../service/serverService";
import {Col, Row} from "react-bootstrap-v5";
import ErrorView from "../ErrorView";
import GeolocationsCard from "../../components/cards/common/GeolocationsCard";

const ServerGeolocations = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchGeolocations, [identifier]);

    if (!data) return <></>;
    if (loadingError) return <ErrorView error={loadingError}/>

    return (
        <Row>
            <Col md={12}>
                <GeolocationsCard data={data}/>
            </Col>
        </Row>
    )
};

export default ServerGeolocations