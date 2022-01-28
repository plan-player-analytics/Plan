import React from "react";
import {Card, Col, Row} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faLifeRing} from "@fortawesome/free-regular-svg-icons";
import {faCampground, faCrosshairs, faKhanda, faSkull} from "@fortawesome/free-solid-svg-icons";
import AsNumbersTable, {TableRow} from "../components/table/AsNumbersTable";
import Datapoint from "../components/Datapoint";
import KillsTable from "../components/table/KillsTable";


const Header = ({player}) => (
    <div className="d-sm-flex align-items-center justify-content-between mb-4">
        <h1 className="h3 mb-0 text-gray-800">
            {player.info.name} &middot; PvP & PvE
        </h1>
    </div>
)

const PvpPveAsNumbersTable = ({player}) => (
    <AsNumbersTable
        headers={['All Time', 'Last 30 days', 'Last 7 days']}
    >
        <TableRow icon={faCrosshairs} color="red" text="KDR" bold
                  values={[player.kill_data.player_kdr_total,
                      player.kill_data.player_kdr_30d,
                      player.kill_data.player_kdr_7d]}/>
        <TableRow icon={faCrosshairs} color="red" text="Player Kills"
                  values={[player.kill_data.player_kills_total,
                      player.kill_data.player_kills_30d,
                      player.kill_data.player_kills_7d]}/>
        <TableRow icon={faSkull} color="red" text="Player Caused Deaths"
                  values={[player.kill_data.player_deaths_total,
                      player.kill_data.player_deaths_30d,
                      player.kill_data.player_deaths_7d]}/>
        <TableRow icon={faCrosshairs} color="green" text="Mob KDR" bold
                  values={[player.kill_data.mob_kdr_total,
                      player.kill_data.mob_kdr_30d,
                      player.kill_data.mob_kdr_7d]}/>
        <TableRow icon={faCrosshairs} color="green" text="Mob Kills"
                  values={[player.kill_data.mob_kills_total,
                      player.kill_data.mob_kills_30d,
                      player.kill_data.mob_kills_7d]}/>
        <TableRow icon={faSkull} color="green" text="Mob Caused Deaths"
                  values={[player.kill_data.mob_deaths_total,
                      player.kill_data.mob_deaths_30d,
                      player.kill_data.mob_deaths_7d]}/>
        <TableRow icon={faSkull} color="black" text="Deaths"
                  values={[player.kill_data.deaths_total,
                      player.kill_data.deaths_30d,
                      player.kill_data.deaths_7d]}/>
    </AsNumbersTable>
)

const PvpPveNumbersCard = ({player}) => (
    <Card>
        <Card.Header>
            <h6 className="col-black">
                <Fa icon={faCampground} className="col-red"/> Pvp & PvE as Numbers
            </h6>
        </Card.Header>
        <PvpPveAsNumbersTable player={player}/>
    </Card>
)

const InsightsCard = ({player}) => (
    <Card>
        <Card.Header>
            <h6 className="col-black">
                <Fa icon={faLifeRing} className="col-red"/> Insights
            </h6>
        </Card.Header>
        <Card.Body>
            <Datapoint icon={faKhanda} color="amber" name="Deadliest PVP Weapon" value={player.kill_data.weapon_1st}/>
            <Datapoint icon={faKhanda} color="grey" name="2nd PVP Weapon" value={player.kill_data.weapon_2nd}/>
            <Datapoint icon={faKhanda} color="brown" name="3rd PVP Weapon" value={player.kill_data.weapon_3rd}/>
        </Card.Body>
    </Card>
)

const PvpKillsTableCard = ({player}) => (
    <Card>
        <Card.Header>
            <h6 className="col-black">
                <Fa icon={faCrosshairs} className="col-red"/> Recent PvP Kills
            </h6>
        </Card.Header>
        <KillsTable kills={player.player_kills}/>
    </Card>
)

const PvpDeathsTableCard = ({player}) => (
    <Card>
        <Card.Header>
            <h6 className="col-black">
                <Fa icon={faSkull} className="col-red"/> Recent PvP Deaths
            </h6>
        </Card.Header>
        <KillsTable kills={player.player_deaths}/>
    </Card>
)

const PlayerPvpPve = ({player}) => {
    return (
        <section className="player_pvp_pve">
            <Header player={player}/>
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