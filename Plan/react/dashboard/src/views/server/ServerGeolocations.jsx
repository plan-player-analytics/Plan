import React from 'react';
import {useParams} from "react-router";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchGeolocations, fetchPingTable} from "../../service/serverService";
import Geolocations from "../common/Geolocations";
import {useAuth} from "../../hooks/authenticationHook";

const ServerGeolocations = () => {
    const {hasPermission} = useAuth();
    const {identifier} = useParams();

    const seeGeolocations = hasPermission('page.server.geolocations.map');
    const seePing = hasPermission('page.server.geolocations.ping.per.country');
    const {data, loadingError} = useDataRequest(fetchGeolocations, [identifier], seeGeolocations);
    const {data: pingData, loadingError: pingLoadingError} = useDataRequest(fetchPingTable, [identifier], seePing);

    return (
        <Geolocations className={"server-geolocations"}
                      identifier={identifier}
                      geolocationData={data} geolocationError={loadingError} seeGeolocations={seeGeolocations}
                      pingData={pingData} pingError={pingLoadingError} seePing={seePing}
        />
    )
};

export default ServerGeolocations