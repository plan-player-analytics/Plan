import React from 'react';
import {Card, Col, Row} from "react-bootstrap";
import DataUseCase from "./DataUseCase.jsx";
import {faUser, faUsers} from "@fortawesome/free-solid-svg-icons";

const DataPlayersUseCase = () => {
    return (
        <Row className={'justify-content-center'}>
            <Col xs={6}>
                <Card>
                    <Card.Body>
                        <DataUseCase label={"players-count"} icon={faUsers}/>
                        <DataUseCase label={"players-online"} icon={faUsers}/>
                        <DataUseCase label={"players-unique"} icon={faUsers}/>
                        <DataUseCase label={"players-new"} icon={faUsers}/>
                        <hr/>
                        <DataUseCase label={"players-activity-index"} icon={faUser}/>
                        <DataUseCase label={"players-very-active"} icon={faUser} value={''}/>
                        <DataUseCase label={"players-active"} icon={faUser} value={''}/>
                        <DataUseCase label={"players-regular"} icon={faUser} value={''}/>
                        <DataUseCase label={"players-irregular"} icon={faUser} value={''}/>
                        <DataUseCase label={"players-inactive"} icon={faUser} value={''}/>
                    </Card.Body>
                </Card>
            </Col>
        </Row>
    )
};

export default DataPlayersUseCase