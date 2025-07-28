import React from 'react';
import {Card, Col, Row} from "react-bootstrap";
import DataUseCase from "./DataUseCase.jsx";
import {
    faChartColumn,
    faCodeCompare,
    faFilterCircleXmark,
    faGlobe,
    faUserCircle,
    faUsersViewfinder
} from "@fortawesome/free-solid-svg-icons";
import {faLifeRing} from "@fortawesome/free-regular-svg-icons";

const DataCalculatedUseCase = () => {
    return (
        <Row className={'justify-content-center'}>
            <Col xs={6}>
                <Card>
                    <Card.Body>
                        <DataUseCase label={"insights"} icon={faLifeRing} value={''}/>
                        <DataUseCase label={"join-addresses"} icon={faChartColumn} value={''}/>
                        <DataUseCase label={"retention"} icon={faUsersViewfinder} value={''}/>
                        <DataUseCase label={"retention-new-players"} icon={faUserCircle} value={''}/>
                        <DataUseCase label={"geolocation"} icon={faGlobe} value={''}/>
                        <DataUseCase label={"allow-list"} icon={faFilterCircleXmark} value={''}/>
                        <DataUseCase label={"plugin-versions"} icon={faCodeCompare} value={''}/>
                    </Card.Body>
                </Card>
            </Col>
        </Row>
    )
};

export default DataCalculatedUseCase