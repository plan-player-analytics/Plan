import React from 'react';
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchGeolocations} from "../../service/serverService";
import {Col, Row} from "react-bootstrap-v5";
import ErrorView from "../ErrorView";
import GeolocationsCard from "../../components/cards/common/GeolocationsCard";
import LoadIn from "../../components/animation/LoadIn";

const ServerGeolocations = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchGeolocations, [identifier]);

    if (loadingError) return <ErrorView error={loadingError}/>

    return (
        <LoadIn>
            <section className="server_geolocations">
                <Row>
                    <Col md={12}>
                        <GeolocationsCard data={data}/>
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
};

export default ServerGeolocations