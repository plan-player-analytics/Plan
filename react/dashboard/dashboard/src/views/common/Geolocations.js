import React from 'react';
import {Col, Row} from "react-bootstrap-v5";
import {ErrorViewCard} from "../ErrorView";
import GeolocationsCard from "../../components/cards/common/GeolocationsCard";
import PingTableCard from "../../components/cards/common/PingTableCard";
import LoadIn from "../../components/animation/LoadIn";

const Geolocations = ({className, geolocationData, pingData, geolocationError, pingError}) => {
    return (
        <LoadIn>
            <section className={className}>
                <Row>
                    <Col md={12}>
                        {geolocationError ? <ErrorViewCard error={geolocationError}/> :
                            <GeolocationsCard data={geolocationData}/>}
                        {pingError ? <ErrorViewCard error={pingError}/> : <PingTableCard data={pingData}/>}
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
};

export default Geolocations