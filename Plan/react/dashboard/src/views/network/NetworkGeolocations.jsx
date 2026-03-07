import React from 'react';
import {useDataRequest} from "../../hooks/dataFetchHook";
import Geolocations from "../common/Geolocations";
import {fetchNetworkPingTable} from "../../service/networkService";
import {fetchGeolocations} from "../../service/serverService";
import {useAuth} from "../../hooks/authenticationHook.tsx";

const NetworkGeolocations = () => {
    const {hasPermission} = useAuth();

    const seeGeolocations = hasPermission('page.network.geolocations.map');
    const seePing = hasPermission('page.network.geolocations.ping.per.country');
    const {data, loadingError} = useDataRequest(fetchGeolocations, [], seeGeolocations);
    const {data: pingData, loadingError: pingLoadingError} = useDataRequest(fetchNetworkPingTable, [], seePing);

    return (
        <Geolocations className={"network-geolocations"}
                      geolocationData={data} geolocationError={loadingError} seeGeolocations={seeGeolocations}
                      pingData={pingData} pingError={pingLoadingError} seePing={seePing}
        />
    )
};

export default NetworkGeolocations