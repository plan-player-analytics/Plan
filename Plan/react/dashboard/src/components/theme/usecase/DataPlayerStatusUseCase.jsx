import React from 'react';
import {Card} from "react-bootstrap";
import DataUseCase from "./DataUseCase.jsx";
import {faAddressCard, faCircle, faGavel} from "@fortawesome/free-solid-svg-icons";
import {faSuperpowers} from "@fortawesome/free-brands-svg-icons";

const DataPlayerStatusUseCase = () => {


    return (
        <Card>
            <Card.Body>
                <DataUseCase label={"online"} icon={faCircle} value={''}/>
                <DataUseCase label={"offline"} icon={faCircle} value={''}/>
                <DataUseCase label={"banned"} icon={faGavel} value={''}/>
                <DataUseCase label={"operator"} icon={faSuperpowers} value={''}/>
                <DataUseCase label={"nicknames"} icon={faAddressCard} value={''}/>
                <DataUseCase label={"kicks"} icon={faGavel}/>
            </Card.Body>
        </Card>
    )
};

export default DataPlayerStatusUseCase