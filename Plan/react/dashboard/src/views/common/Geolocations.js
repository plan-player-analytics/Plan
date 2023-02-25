import React from 'react';
import {Col} from "react-bootstrap";
import {ErrorViewCard} from "../ErrorView";
import GeolocationsCard from "../../components/cards/common/GeolocationsCard";
import PingTableCard from "../../components/cards/common/PingTableCard";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";

const Geolocations = ({className, geolocationData, pingData, geolocationError, pingError}) => {
    return (
        <LoadIn>
            <section className={className}>
                <ExtendableRow id={'row-' + className}>
                    <Col md={12}>
                        {geolocationError ? <ErrorViewCard error={geolocationError}/> :
                            <GeolocationsCard data={geolocationData}/>}
                        {pingError ? <ErrorViewCard error={pingError}/> : <PingTableCard data={pingData}/>}
                    </Col>
                </ExtendableRow>
            </section>
        </LoadIn>
    )
};

export default Geolocations