import React from 'react';
import Datapoint from "../../Datapoint.jsx";
import {Card} from "react-bootstrap";

const DataUseCase = ({icon, label, card, value}) => {
    if (card) {
        return (
            <Card>
                <Card.Body>
                    <Datapoint icon={icon} value={value !== undefined ? value : 123} name={label.replaceAll('-', ' ')}
                               color={label}/>
                </Card.Body>
            </Card>
        )
    } else {
        return (
            <Datapoint icon={icon} value={value !== undefined ? value : 123} name={label.replaceAll('-', ' ')}
                       color={label}/>
        )
    }
};

export default DataUseCase