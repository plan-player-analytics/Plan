import {Card} from "react-bootstrap";
import Datapoint from "../../Datapoint";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faLongArrowAltRight} from "@fortawesome/free-solid-svg-icons";
import BigTrend from "../../trend/BigTrend";
import React from "react";

const TrendCard = ({icon, color, name, previous, next, trend}) => {
    return <Card>
        <Card.Body>
            <Datapoint icon={icon} name={name} color={color}
                       value={
                           <>
                               {previous} <Fa icon={faLongArrowAltRight}/> {next} <BigTrend trend={trend}/>
                           </>
                       }/>
        </Card.Body>
    </Card>
}

export default TrendCard;