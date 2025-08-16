import React from 'react';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import NicknamesCard from "../../cards/player/NicknamesCard.jsx";
import PluginHistoryCard from "../../cards/common/PluginHistoryCard.jsx";

const BasicTableUseCase = () => {
    const nicknames = [];
    for (let i = 0; i < 10; i++) {
        const multiplier = i * 7 * 86400000;
        nicknames.push({nickname: 'Player', server: 'Server 1', date: Date.now() - multiplier});
    }
    return (
        <NicknamesCard nicknames={nicknames}/>
    )
};

const DataTableUseCase = () => {
    const history = [];
    for (let i = 0; i < 10; i++) {
        const multiplier = i * 7 * 86400000;
        history.push({name: 'Plugin', version: '1.2.3', modified: Date.now() - multiplier});
    }
    return (
        <PluginHistoryCard data={{history}}/>
    )
}

export const TableUseCase = () => {
    return (
        <Row className="justify-content-center">
            <Col md={6}>
                <BasicTableUseCase/>
            </Col>
            <Col md={6}>
                <DataTableUseCase/>
            </Col>
        </Row>
    );
}