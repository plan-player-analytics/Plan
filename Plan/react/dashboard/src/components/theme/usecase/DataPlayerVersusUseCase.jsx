import React from 'react';
import {Card} from "react-bootstrap";
import DataUseCase from "./DataUseCase.jsx";
import {faCrosshairs, faKhanda, faSkull} from "@fortawesome/free-solid-svg-icons";

const DataPlayerVersusUseCase = () => {


    return (
        <Card>
            <Card.Body>
                <DataUseCase label={"player-kills"} icon={faCrosshairs}/>
                <DataUseCase label={"mob-kills"} icon={faCrosshairs}/>
                <DataUseCase label={"deaths"} icon={faSkull}/>
                <hr/>
                <DataUseCase label={"top-3-first"} icon={faKhanda}/>
                <DataUseCase label={"top-3-second"} icon={faKhanda}/>
                <DataUseCase label={"top-3-third"} icon={faKhanda}/>
            </Card.Body>
        </Card>
    )
};

export default DataPlayerVersusUseCase