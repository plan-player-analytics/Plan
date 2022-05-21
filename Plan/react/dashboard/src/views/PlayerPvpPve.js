import React from "react";
import {Card, Col, Row} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faLifeRing} from "@fortawesome/free-regular-svg-icons";
import {faCrosshairs, faKhanda, faSkull} from "@fortawesome/free-solid-svg-icons";
import Datapoint from "../components/Datapoint";
import KillsTable from "../components/table/KillsTable";
import {usePlayer} from "./PlayerPage";
import {useTranslation} from "react-i18next";
import PvpPveAsNumbersCard from "../components/cards/player/PvpPveAsNumbersCard";

const InsightsCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faLifeRing} className="col-red"/> {t('html.label.insights')}
                </h6>
            </Card.Header>
            <Card.Body>
                <Datapoint icon={faKhanda} color="amber" name={t('html.label.deadliestWeapon')}
                           value={player.kill_data.weapon_1st}/>
                <Datapoint icon={faKhanda} color="grey" name={t('html.label.secondDeadliestWeapon')}
                           value={player.kill_data.weapon_2nd}/>
                <Datapoint icon={faKhanda} color="brown" name={t('html.label.thirdDeadliestWeapon')}
                           value={player.kill_data.weapon_3rd}/>
            </Card.Body>
        </Card>
    )
}

const PvpKillsTableCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faCrosshairs} className="col-red"/> {t('html.label.recentPvpKills')}
                </h6>
            </Card.Header>
            <KillsTable kills={player.player_kills}/>
        </Card>
    )
}

const PvpDeathsTableCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faSkull} className="col-red"/> {t('html.label.recentPvpDeaths')}
                </h6>
            </Card.Header>
            <KillsTable kills={player.player_deaths}/>
        </Card>
    )
}

const PlayerPvpPve = () => {
    const {player} = usePlayer();
    return (
        <section className="player_pvp_pve">
            <Row>
                <Col lg={8}>
                    <PvpPveAsNumbersCard player={player}/>
                </Col>
                <Col lg={4}>
                    <InsightsCard player={player}/>
                </Col>
            </Row>
            <Row>
                <Col lg={6}>
                    <PvpKillsTableCard player={player}/>
                </Col>
                <Col lg={6}>
                    <PvpDeathsTableCard player={player}/>
                </Col>
            </Row>
        </section>
    )
}

export default PlayerPvpPve