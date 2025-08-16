import React from 'react';
import {Card, Col, Row} from "react-bootstrap";
import DataUseCase from "./DataUseCase.jsx";
import {faCrosshairs, faKhanda, faSkull} from "@fortawesome/free-solid-svg-icons";

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
                        <DataUseCase label={"top-3-first"} icon={faKhanda}/>
                        <DataUseCase label={"top-3-second"} icon={faKhanda}/>
                        <DataUseCase label={"top-3-third"} icon={faKhanda}/>
                    </Card.Body>
                </Card>
            </Col>
        </Row>
    )
};

export default DataPlayerVersusUseCase