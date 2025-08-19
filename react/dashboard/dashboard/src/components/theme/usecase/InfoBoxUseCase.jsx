import React from 'react';
import {Alert, Col, Row} from "react-bootstrap";

const InfoBoxUseCase = () => {
    return (
        <Row className={'justify-content-center'}>
            <Col xs={6}>
                <Alert variant="success">Info</Alert>
                <Alert variant="warning">Notice</Alert>
                <Alert variant="danger">Error</Alert>
            </Col>
        </Row>
    )
};

export default InfoBoxUseCase