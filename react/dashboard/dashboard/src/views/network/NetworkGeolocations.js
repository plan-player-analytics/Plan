import React from 'react';
import {useDataRequest} from "../../hooks/dataFetchHook";
import Geolocations from "../common/Geolocations";
import {fetchNetworkPingTable} from "../../service/networkService";
import {fetchGeolocations} from "../../service/serverService";

const NetworkGeolocations = () => {
    const {data, loadingError} = useDataRequest(fetchGeolocations, []);
    const {data: pingData, loadingError: pingLoadingError} = useDataRequest(fetchNetworkPingTable, []);

    return (
        <Geolocations className={"network_geolocations"}
                      geolocationData={data} geolocationError={loadingError}
                      pingData={pingData} pingError={pingLoadingError}
        />
    )
};

export default NetworkGeolocations