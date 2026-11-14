import { Col } from 'react-bootstrap';
import React from 'react';
import PlayerbaseDevelopmentCard from '../../components/cards/server/graphs/PlayerbaseDevelopmentCard';
import CurrentPlayerbaseCard from '../../components/cards/server/graphs/CurrentPlayerbaseCard';
import PlayerbaseTrendsCard from '../../components/cards/server/tables/PlayerbaseTrendsCard';
import LoadIn from '../../components/animation/LoadIn.tsx';
import ExtendableRow from '../../components/layout/extension/ExtendableRow.tsx';
import { useAuth } from '../../hooks/authenticationHook.tsx';

const NetworkPlayerbaseOverview = () => {
    const { hasPermission } = useAuth();

    const seeOverview = hasPermission('page.network.playerbase.overview');
    const seeGraphs = hasPermission('page.network.playerbase.graphs');

    return (
        <LoadIn>
            <section className='network-playerbase'>
                {seeGraphs && (
                    <ExtendableRow id={'row-network-playerbase-0'}>
                        <Col lg={8}>
                            <PlayerbaseDevelopmentCard identifier={undefined} />
                        </Col>
                        <Col lg={4}>
                            <CurrentPlayerbaseCard identifier={undefined} />
                        </Col>
                    </ExtendableRow>
                )}
                {seeOverview && (
                    <ExtendableRow id={'row-network-playerbase-1'}>
                        <Col lg={12}>
                            <PlayerbaseTrendsCard />
                        </Col>
                    </ExtendableRow>
                )}
            </section>
        </LoadIn>
    );
};

export default NetworkPlayerbaseOverview;
