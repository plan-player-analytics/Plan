import React from 'react';
import {Card, Col, Row} from "react-bootstrap";
import DataUseCase from "./DataUseCase.jsx";
import {
    faDragon,
    faExclamationCircle,
    faHdd,
    faMap,
    faMicrochip,
    faPowerOff,
    faSignal,
    faStopwatch,
    faTachometerAlt
} from "@fortawesome/free-solid-svg-icons";

const DataPerformanceUseCase = () => {
    return (<Row className={'justify-content-center'}>
            <Col xs={6}>
                <Card>
                    <Card.Body>
                        <DataUseCase label={"uptime"} icon={faPowerOff}/>
                        <DataUseCase label={"downtime"} icon={faPowerOff}/>
                        <DataUseCase label={"tps"} icon={faTachometerAlt}/>
                        <DataUseCase label={"tps-low-spikes"} icon={faExclamationCircle}/>
                        <DataUseCase label={"tps-average"} icon={faTachometerAlt}/>
                        <DataUseCase label={"mspt-average"} icon={faStopwatch}/>
                        <DataUseCase label={"mspt-percentile"} icon={faStopwatch}/>
                    </Card.Body>
                </Card>
            </Col>
            <Col xs={6}>
                <Card>
                    <Card.Body>
                        <DataUseCase label={"cpu"} icon={faTachometerAlt}/>
                        <DataUseCase label={"ram"} icon={faMicrochip}/>
                        <DataUseCase label={"entities"} icon={faDragon}/>
                        <DataUseCase label={"chunks"} icon={faMap}/>
                        <DataUseCase label={"disk"} icon={faHdd}/>
                        <DataUseCase label={"ping"} icon={faSignal}/>
                    </Card.Body>
                </Card>
            </Col>
        </Row>
    )
};

export default DataPerformanceUseCase