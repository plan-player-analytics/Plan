import React from 'react';
import CardHeader from "../../cards/CardHeader.jsx";
import {faImage, faUser} from "@fortawesome/free-solid-svg-icons";
import {Card} from "react-bootstrap";
import Datapoint from "../../Datapoint.jsx";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faQuestionCircle} from "@fortawesome/free-regular-svg-icons";

const CardUseCase = () => {
    return (
        <Card>
            <CardHeader icon={faImage} label={"Card"}>
                <button className={"float-end"} onClick={() => {
                }}>
                    <Fa className={"col-help-icon"}
                        icon={faQuestionCircle}/>
                </button>
            </CardHeader>
            <Card.Body>
                <Datapoint name={"Example"} value={1234} icon={faUser}/>
                <hr/>
                <Datapoint name={"Example"} value={1234} icon={faUser}/>
            </Card.Body>
        </Card>
    )
};

export default CardUseCase