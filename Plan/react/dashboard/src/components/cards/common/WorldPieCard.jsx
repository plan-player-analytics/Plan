import {Card} from "react-bootstrap";
import {faClock} from "@fortawesome/free-regular-svg-icons";
import WorldPie from "../../graphs/WorldPie";
import React from "react";
import {CardLoader} from "../../navigation/Loader.tsx";
import CardHeader from "../CardHeader.tsx";

const WorldPieCard = ({worldSeries, gmSeries, title}) => {
    if (!worldSeries || !gmSeries) return <CardLoader/>;
    return (
        <Card>
            <CardHeader icon={faClock} color={"sessions"} label={title}/>
            <WorldPie
                id="world-pie"
                worldSeries={worldSeries}
                gmSeries={gmSeries}
            />
        </Card>
    )
}

export default WorldPieCard;