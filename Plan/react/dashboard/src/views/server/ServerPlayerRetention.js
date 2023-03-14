import React from 'react';
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {Col} from "react-bootstrap";
import LoadIn from "../../components/animation/LoadIn";
import PlayerRetentionGraphCard from "../../components/cards/common/PlayerRetentionGraphCard";
import {useParams} from "react-router-dom";

const ServerPlayerRetention = () => {
    const {identifier} = useParams();
    return (
        <LoadIn>
            <section className="server-retention">
                <ExtendableRow id={'row-server-retention-0'}>
                    <Col lg={12}>
                        <PlayerRetentionGraphCard identifier={identifier}/>
                    </Col>
                </ExtendableRow>
            </section>
        </LoadIn>
    )
};

export default ServerPlayerRetention