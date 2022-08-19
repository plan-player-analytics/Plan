import React from 'react';
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchGeolocations, fetchPingTable} from "../../service/serverService";
import {Col, Row} from "react-bootstrap-v5";
import {ErrorViewCard} from "../ErrorView";
import GeolocationsCard from "../../components/cards/common/GeolocationsCard";
import LoadIn from "../../components/animation/LoadIn";
import PingTableCard from "../../components/cards/common/PingTableCard";

const ServerGeolocations = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchGeolocations, [identifier]);
    const {pingData, pingLoadingError} = useDataRequest(fetchPingTable, [identifier]);

    return (
        <LoadIn>
            <section className="server_geolocations">
                <Row>
                    <Col md={12}>
                        {loadingError ? <ErrorViewCard error={loadingError}/> : <GeolocationsCard data={data}/>}
                        {pingLoadingError ? <ErrorViewCard error={pingLoadingError}/> :
                            <PingTableCard data={pingData}/>}
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
};

export default ServerGeolocations