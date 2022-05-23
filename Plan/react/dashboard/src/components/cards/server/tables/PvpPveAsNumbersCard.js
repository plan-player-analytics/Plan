import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCampground} from "@fortawesome/free-solid-svg-icons";
import React from "react";
import ServerPvpPveAsNumbersTable from "../../../table/ServerPvpPveAsNumbersTable";

const PvpPveAsNumbersCard = ({kill_data}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faCampground} className="col-red"/> {t('html.label.pvpPveAsNumbers')}
                </h6>
            </Card.Header>
            <ServerPvpPveAsNumbersTable killData={kill_data}/>
        </Card>
    )
}

export default PvpPveAsNumbersCard;