import React, {ReactNode} from 'react';
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {Card} from "react-bootstrap";
import {useTranslation} from "react-i18next";
import {IconProp} from "@fortawesome/fontawesome-svg-core";

type Props = {
    icon: IconProp;
    color: string;
    label: string | any;
    children?: ReactNode;
}

const CardHeader = ({icon, color, label, children}: Props) => {
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