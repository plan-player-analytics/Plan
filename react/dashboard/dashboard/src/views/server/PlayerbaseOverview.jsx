import { Col } from 'react-bootstrap';
import React from 'react';
import PlayerbaseDevelopmentCard from '../../components/cards/server/graphs/PlayerbaseDevelopmentCard';
import CurrentPlayerbaseCard from '../../components/cards/server/graphs/CurrentPlayerbaseCard';
import { useParams } from 'react-router';
import PlayerbaseTrendsCard from '../../components/cards/server/tables/PlayerbaseTrendsCard';
import LoadIn from '../../components/animation/LoadIn.tsx';
import ExtendableRow from '../../components/layout/extension/ExtendableRow.tsx';
import { useAuth } from '../../hooks/authenticationHook.tsx';

const PlayerbaseOverview = () => {
    const { hasPermission } = useAuth();
    const { identifier } = useParams();

    const seeOverview = hasPermission('page.server.playerbase.overview');
    const seeGraphs = hasPermission('page.server.playerbase.graphs');

    return (
        <LoadIn>
            <section className='server-playerbase'>
                {seeGraphs && (
                    <ExtendableRow id={'row-server-playerbase-0'}>
                        <Col lg={8}>
                            <PlayerbaseDevelopmentCard
                                identifier={identifier}
                            />
                        </Col>
                        <Col lg={4}>
                            <CurrentPlayerbaseCard identifier={identifier} />
                        </Col>
                    </ExtendableRow>
                )}
                {seeOverview && (
                    <ExtendableRow id={'row-server-playerbase-1'}>
                        <Col lg={12}>
                            <PlayerbaseTrendsCard identifier={identifier} />
                        </Col>
                    </ExtendableRow>
                )}
            </section>
        </LoadIn>
    );
};

export default PlayerbaseOverview;
