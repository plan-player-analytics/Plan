import React from 'react';
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {Card} from "react-bootstrap";
import {useTranslation} from "react-i18next";

const CardHeader = ({icon, color, label, children}) => {
    const {t} = useTranslation();

    return (
        <Card.Header>
            <h6 className="col-text" style={{width: "100%"}}>
                <Fa icon={icon} className={"col-" + color}/> {label.length ? t(label) : label}
                {children}
            </h6>
        </Card.Header>
    )
};

export default CardHeader