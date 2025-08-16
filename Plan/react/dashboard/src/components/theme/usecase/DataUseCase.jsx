import React from 'react';
import Datapoint from "../../Datapoint.jsx";
import {Card, Col, Row} from "react-bootstrap";

const DataUseCase = ({icon, label, card, value}) => {
    if (card) {
        return (
            <Row className={'justify-content-center'}>
                <Col xs={6}>
                    <Card>
                        <Card.Body>
                            <Datapoint icon={icon} value={value !== undefined ? value : 123}
                                       name={label.replaceAll('-', ' ')}
                                       color={label}/>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        )
    } else {
        return (
            <Datapoint icon={icon} value={value !== undefined ? value : 123} name={label.replaceAll('-', ' ')}
                       color={label}/>
        )
    }
};

export default DataUseCase