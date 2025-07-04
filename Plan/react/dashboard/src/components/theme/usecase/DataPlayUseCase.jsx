import React from 'react';
import {Card} from "react-bootstrap";
import DataUseCase from "./DataUseCase.jsx";
import {faCalendar, faCalendarPlus, faClock} from "@fortawesome/free-regular-svg-icons";
import {faGamepad} from "@fortawesome/free-solid-svg-icons";

const DataPlayUseCase = () => {

    return (
        <Card>
            <Card.Body>
                <DataUseCase label={"playtime"} icon={faClock}/>
                <DataUseCase label={"playtime-active"} icon={faClock}/>
                <DataUseCase label={"playtime-afk"} icon={faClock}/>
                <hr/>
                <DataUseCase label={"sessions"} icon={faCalendar}/>
                <DataUseCase label={"session-length"} icon={faClock}/>
                <DataUseCase label={"gamemode"} icon={faGamepad}/>
                <hr/>
                <DataUseCase label={"first-seen"} icon={faCalendarPlus}/>
                <DataUseCase label={"last-seen"} icon={faCalendar}/>
            </Card.Body>
        </Card>
    )
};

export default DataPlayUseCase