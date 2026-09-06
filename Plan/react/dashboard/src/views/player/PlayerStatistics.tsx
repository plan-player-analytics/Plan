import React from "react";
import {Col} from "react-bootstrap";
import {usePlayer} from "../layout/PlayerPage";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";
import {Player} from "../../dataHooks/model/Player";
import {StatisticsTable} from "../../components/table/StatisticsTable";

const PlayerStatistics = () => {
    const {hasPermission} = useAuth();
    const {player} = usePlayer() as { player: Player };

    return (
        <LoadIn>
            {hasPermission('page.player.statistics') && <section className="player-statistics" id={"player-statistics"}>
                <ExtendableRow id={'row-player-statistics-0'}>
                    <Col lg={12}>
                        <StatisticsTable statistics={player.statistics} showAllByDefault/>
                    </Col>
                </ExtendableRow>
            </section>}
        </LoadIn>
    )
}

export default PlayerStatistics;