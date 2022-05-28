import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React from "react";
import {faLifeRing} from "@fortawesome/free-regular-svg-icons";

const InsightsFor30DaysCard = ({children}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faLifeRing} className="col-red"/> {t('html.label.insights30days')}
                </h6>
            </Card.Header>
            <Card.Body>
                {children}
            </Card.Body>
        </Card>
    )
}

export default InsightsFor30DaysCard;