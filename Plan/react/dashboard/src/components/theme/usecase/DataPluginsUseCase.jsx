import React from 'react';
import {Card, Col, Row} from "react-bootstrap";
import DataUseCase from "./DataUseCase.jsx";
import {faCircle} from "@fortawesome/free-solid-svg-icons";

const DataPluginsUseCase = () => {
    return (
        <Row className={'justify-content-center'}>
            <Col xs={6}>
                <Card>
                    <Card.Body>
                        <DataUseCase label={"plugin-red"} icon={faCircle}/>
                        <DataUseCase label={"plugin-pink"} icon={faCircle}/>
                        <DataUseCase label={"plugin-purple"} icon={faCircle}/>
                        <DataUseCase label={"plugin-deep-purple"} icon={faCircle}/>
                        <DataUseCase label={"plugin-indigo"} icon={faCircle}/>
                        <DataUseCase label={"plugin-blue"} icon={faCircle}/>
                        <DataUseCase label={"plugin-light-blue"} icon={faCircle}/>
                        <DataUseCase label={"plugin-cyan"} icon={faCircle}/>
                        <DataUseCase label={"plugin-teal"} icon={faCircle}/>
                        <DataUseCase label={"plugin-green"} icon={faCircle}/>
                    </Card.Body>
                </Card>
            </Col>
            <Col xs={6}>
                <Card>
                    <Card.Body>
                        <DataUseCase label={"plugin-light-green"} icon={faCircle}/>
                        <DataUseCase label={"plugin-lime"} icon={faCircle}/>
                        <DataUseCase label={"plugin-yellow"} icon={faCircle}/>
                        <DataUseCase label={"plugin-amber"} icon={faCircle}/>
                        <DataUseCase label={"plugin-orange"} icon={faCircle}/>
                        <DataUseCase label={"plugin-deep-orange"} icon={faCircle}/>
                        <DataUseCase label={"plugin-brown"} icon={faCircle}/>
                        <DataUseCase label={"plugin-grey"} icon={faCircle}/>
                        <DataUseCase label={"plugin-blue-grey"} icon={faCircle}/>
                        <DataUseCase label={"plugin-black"} icon={faCircle}/>
                    </Card.Body>
                </Card>
            </Col>
        </Row>
    )
};

export default DataPluginsUseCase