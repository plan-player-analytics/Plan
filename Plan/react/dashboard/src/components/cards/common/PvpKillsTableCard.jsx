import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCrosshairs} from "@fortawesome/free-solid-svg-icons";
import KillsTable from "../../table/KillsTable";
import React from "react";
import {CardLoader} from "../../navigation/Loader";

const PvpKillsTableCard = ({player_kills}) => {
    const {t} = useTranslation();

    if (!player_kills) return <CardLoader/>;

    return (
        <Card id={'pvp-kills-table'}>
            <Card.Header>
                <h6 className="col-text">
                    <Fa icon={faCrosshairs} className="col-player-kills"/> {t('html.label.recentPvpKills')}
                </h6>
            </Card.Header>
            <KillsTable kills={player_kills}/>
        </Card>
    )
}

export default PvpKillsTableCard;