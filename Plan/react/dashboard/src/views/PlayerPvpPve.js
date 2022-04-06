import React from "react";
import {Card, Col, Row} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faLifeRing} from "@fortawesome/free-regular-svg-icons";
import {faCampground, faCrosshairs, faKhanda, faSkull} from "@fortawesome/free-solid-svg-icons";
import AsNumbersTable, {TableRow} from "../components/table/AsNumbersTable";
import Datapoint from "../components/Datapoint";
import KillsTable from "../components/table/KillsTable";
import {usePlayer} from "./PlayerPage";
import {useTranslation} from "react-i18next";


const PvpPveAsNumbersTable = ({player}) => {
    const {t} = useTranslation();
    return (
        <AsNumbersTable
            headers={[t('html.label.allTime'), t('html.label.last30days'), t('html.label.last7days')]}
        >
            <TableRow icon={faCrosshairs} color="red" text={t('html.label.kdr')} bold
                      values={[player.kill_data.player_kdr_total,
                          player.kill_data.player_kdr_30d,
                          player.kill_data.player_kdr_7d]}/>
            <TableRow icon={faCrosshairs} color="red" text={t('html.label.playerKills')}
                      values={[player.kill_data.player_kills_total,
                          player.kill_data.player_kills_30d,
                          player.kill_data.player_kills_7d]}/>
            <TableRow icon={faSkull} color="red" text={t('html.label.playerDeaths')}
                      values={[player.kill_data.player_deaths_total,
                          player.kill_data.player_deaths_30d,
                          player.kill_data.player_deaths_7d]}/>
            <TableRow icon={faCrosshairs} color="green" text={t('html.label.mobKdr')} bold
                      values={[player.kill_data.mob_kdr_total,
                          player.kill_data.mob_kdr_30d,
                          player.kill_data.mob_kdr_7d]}/>
            <TableRow icon={faCrosshairs} color="green" text={t('html.label.mobKills')}
                      values={[player.kill_data.mob_kills_total,
                          player.kill_data.mob_kills_30d,
                          player.kill_data.mob_kills_7d]}/>
            <TableRow icon={faSkull} color="green" text={t('html.label.mobDeaths')}
                      values={[player.kill_data.mob_deaths_total,
                          player.kill_data.mob_deaths_30d,
                          player.kill_data.mob_deaths_7d]}/>
            <TableRow icon={faSkull} color="black" text={t('html.label.deaths')}
                      values={[player.kill_data.deaths_total,
                          player.kill_data.deaths_30d,
                          player.kill_data.deaths_7d]}/>
        </AsNumbersTable>
    )
}

const PvpPveNumbersCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faCampground} className="col-red"/> {t('html.label.pvpPveAsNumbers')}
                </h6>
            </Card.Header>
            <PvpPveAsNumbersTable player={player}/>
        </Card>
    )
}

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
                    <PvpPveNumbersCard player={player}/>
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