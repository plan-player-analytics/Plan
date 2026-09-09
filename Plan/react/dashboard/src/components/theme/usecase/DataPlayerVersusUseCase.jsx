import React from 'react';
import {Card, Col, Row} from "react-bootstrap";
import DataUseCase from "./DataUseCase.jsx";
import {faCrosshairs, faHandSparkles, faSkull} from "@fortawesome/free-solid-svg-icons";

const DataPlayerVersusUseCase = () => {
    return (
        <Row className={'justify-content-center'}>
            <Col xs={6}>
                <Card>
                    <Card.Body>
                        <DataUseCase label={"player-kills"} icon={faCrosshairs}/>
                        <DataUseCase label={"mob-kills"} icon={faCrosshairs}/>
                        <DataUseCase label={"deaths"} icon={faSkull}/>
                        <hr/>
                        <DataUseCase label={"top-3-first"} icon={faHandSparkles}/>
                        <DataUseCase label={"top-3-second"} icon={faHandSparkles}/>
                        <DataUseCase label={"top-3-third"} icon={faHandSparkles}/>
                    </Card.Body>
                </Card>
            </Col>
        </Row>
    )
};

export default DataPlayerVersusUseCase