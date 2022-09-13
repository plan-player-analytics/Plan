import React from 'react';
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {Card} from "react-bootstrap-v5";
import {useTranslation} from "react-i18next";

const CardHeader = ({icon, color, label}) => {
    const {t} = useTranslation();

    return (
        <Card.Header>
            <h6 className="col-black">
                <Fa icon={icon} className={"col-" + color}/> {t(label)}
            </h6>
        </Card.Header>
    )
};

export default CardHeader