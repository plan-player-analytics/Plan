import React from "react";
import PvpPveAsNumbersCard from "../../components/cards/server/tables/PvpPveAsNumbersCard";
import {Col, Row} from "react-bootstrap-v5";
import PvpKillsTableCard from "../../components/cards/common/PvpKillsTableCard";
import PvpPveInsightsCard from "../../components/cards/server/insights/PvpPveInsightsCard";

const ServerPvpPve = () => {
    return (
        <section className="server_pvp_pve">
            <Row>
                <Col lg={8}>
                    <PvpPveAsNumbersCard kill_data={{}}/>
                    <PvpKillsTableCard player_kills={[]}/>
                </Col>
                <Col lg={4}>
                    <PvpPveInsightsCard/>
                </Col>
            </Row>
        </section>

    )
}

export default ServerPvpPve;