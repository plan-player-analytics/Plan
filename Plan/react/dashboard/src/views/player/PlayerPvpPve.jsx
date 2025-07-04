import React from "react";
import {Card, Col} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faLifeRing} from "@fortawesome/free-regular-svg-icons";
import {faKhanda, faSkull} from "@fortawesome/free-solid-svg-icons";
import Datapoint from "../../components/Datapoint";
import KillsTable from "../../components/table/KillsTable";
import {usePlayer} from "../layout/PlayerPage";
import {useTranslation} from "react-i18next";
import PvpPveAsNumbersCard from "../../components/cards/player/PvpPveAsNumbersCard";
import PvpKillsTableCard from "../../components/cards/common/PvpKillsTableCard";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";

const InsightsCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-text">
                    <Fa icon={faLifeRing} className="col-insights"/> {t('html.label.insights')}
                </h6>
            </Card.Header>
            <Card.Body>
                <Datapoint icon={faKhanda} color="top-3-first" name={t('html.label.deadliestWeapon')}
                           value={player.kill_data.weapon_1st}/>
                <Datapoint icon={faKhanda} color="top-3-second" name={t('html.label.secondDeadliestWeapon')}
                           value={player.kill_data.weapon_2nd}/>
                <Datapoint icon={faKhanda} color="top-3-third" name={t('html.label.thirdDeadliestWeapon')}
                           value={player.kill_data.weapon_3rd}/>
            </Card.Body>
        </Card>
    )
}

const PvpDeathsTableCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-text">
                    <Fa icon={faSkull} className="col-player-kills"/> {t('html.label.recentPvpDeaths')}
                </h6>
            </Card.Header>
            <KillsTable kills={player.player_deaths}/>
        </Card>
    )
}

const PlayerPvpPve = () => {
    const {hasPermission} = useAuth();
    const {player} = usePlayer();
    return (
        <LoadIn>
            {hasPermission('page.player.versus') && <section className="player-pvp-pve" id={"player-pvp-pve"}>
                <ExtendableRow id={'row-player-pvp-pve-0'}>
                    <Col lg={8}>
                        <PvpPveAsNumbersCard player={player}/>
                    </Col>
                    <Col lg={4}>
                        <InsightsCard player={player}/>
                    </Col>
                </ExtendableRow>
                <ExtendableRow id={'row-player-pvp-pve-1'}>
                    <Col lg={6}>
                        <PvpKillsTableCard player_kills={player.player_kills}/>
                    </Col>
                    <Col lg={6}>
                        <PvpDeathsTableCard player={player}/>
                    </Col>
                </ExtendableRow>
            </section>}
        </LoadIn>
    )
}

export default PlayerPvpPve