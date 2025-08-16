import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCampground} from "@fortawesome/free-solid-svg-icons";
import React from "react";
import PlayerPvpPveAsNumbersTable from "../../table/PlayerPvpPveAsNumbersTable";

const PvpPveAsNumbersCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-text">
                    <Fa icon={faCampground} className="col-player-kills"/> {t('html.label.pvpPveAsNumbers')}
                </h6>
            </Card.Header>
            <PlayerPvpPveAsNumbersTable killData={player.kill_data}/>
        </Card>
    )
}

export default PvpPveAsNumbersCard;