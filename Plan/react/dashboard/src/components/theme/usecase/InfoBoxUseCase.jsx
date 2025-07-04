import React from 'react';
import {Alert} from "react-bootstrap";

const InfoBoxUseCase = () => {
    return (
        <>
            <Alert variant="success">Info</Alert>
            <Alert variant="warning">Notice</Alert>
            <Alert variant="danger">Error</Alert>
        </>
    )
};

export default InfoBoxUseCase