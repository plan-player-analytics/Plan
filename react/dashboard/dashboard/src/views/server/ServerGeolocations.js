import React from 'react';
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchGeolocations, fetchPingTable} from "../../service/serverService";
import Geolocations from "../common/Geolocations";

const ServerGeolocations = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchGeolocations, [identifier]);
    const {pingData, pingLoadingError} = useDataRequest(fetchPingTable, [identifier]);

    return (
        <Geolocations className={"server_geolocations"}
                      geolocationData={data} geolocationError={loadingError}
                      pingData={pingData} pingError={pingLoadingError}
        />
    )
};

export default ServerGeolocations