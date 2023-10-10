import React from 'react';
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {Col} from "react-bootstrap";
import LoadIn from "../../components/animation/LoadIn";
import PlayerRetentionGraphCard from "../../components/cards/common/PlayerRetentionGraphCard";
import {useParams} from "react-router-dom";
import {useAuth} from "../../hooks/authenticationHook";
import FirstMomentsCard from "../../components/cards/common/FirstMomentsCard";

const ServerPlayerRetention = () => {
    const {hasPermission} = useAuth();
    const {identifier} = useParams();

    const seeRetention = hasPermission('page.server.retention');
    return (
        <LoadIn>
            <section className="server-retention">
                {seeRetention && <ExtendableRow id={'row-server-retention-0'}>
                    <Col lg={12}>
                        <PlayerRetentionGraphCard identifier={identifier}/>
                    </Col>
                </ExtendableRow>}
                <ExtendableRow id={'row-server-retention-1'}>
                    <Col lg={12}>
                        <FirstMomentsCard identifier={identifier}/>
                    </Col>
                </ExtendableRow>
            </section>
        </LoadIn>
    )
};

export default ServerPlayerRetention