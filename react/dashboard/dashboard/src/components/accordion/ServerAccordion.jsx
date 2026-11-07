import React from 'react';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import {
    faCrosshairs,
    faGavel,
    faLocationArrow,
    faServer,
    faSkull,
} from '@fortawesome/free-solid-svg-icons';
import {
    faCalendarCheck,
    faCalendarPlus,
    faClock,
} from '@fortawesome/free-regular-svg-icons';
import Datapoint from '../datapoint/Datapoint.tsx';
import { Col, Row } from 'react-bootstrap';
import WorldPie from '../graphs/WorldPie';
import Accordion from './Accordion';
import { faSuperpowers } from '@fortawesome/free-brands-svg-icons';
import { useTranslation } from 'react-i18next';
import FormattedDate from '../text/FormattedDate.tsx';
import FormattedTime from '../text/FormattedTime.tsx';
import ServerPageLinkButton from '../input/button/ServerPageLinkButton.tsx';

const ServerHeader = ({ server }) => {
    const { t } = useTranslation();
    return (
        <>
            <td>
                {t(server.serverName)}
                {server.operator ? (
                    <span title={t('html.label.operator')}>
                        <Fa icon={faSuperpowers} />
                    </span>
                ) : (
                    ''
                )}
                {server.banned ? (
                    <span title={t('html.label.banned')}>
                        <Fa icon={faGavel} />
                    </span>
                ) : (
                    ''
                )}
            </td>
            <td>
                <FormattedTime timeMs={server.playtime} />
            </td>
            <td>
                <FormattedDate date={server.registered} />
            </td>
            <td>
                <FormattedDate date={server.lastSeen} />
            </td>
        </>
    );
};

const ServerBody = ({ i, server }) => {
    const { t } = useTranslation();

    return (
        <Row>
            <Col lg={6}>
                {server.operator ? (
                    <Datapoint
                        icon={faSuperpowers}
                        color='operator'
                        name={t('html.label.operator')}
                    />
                ) : (
                    ''
                )}
                {server.banned ? (
                    <Datapoint
                        icon={faGavel}
                        color='banned'
                        name={t('html.label.banned')}
                    />
                ) : (
                    ''
                )}
                {server.operator || server.banned ? <br /> : ''}
                <Datapoint
                    icon={faCalendarCheck}
                    color={'sessions'}
                    name={t('html.label.sessions')}
                    value={server.sessionCount}
                    bold
                />
                <Datapoint
                    icon={faClock}
                    color={'playtime'}
                    name={t('html.label.playtime')}
                    value={<FormattedTime timeMs={server.playtime} />}
                    bold
                />
                <Datapoint
                    icon={faClock}
                    color={'playtime-afk'}
                    name={t('html.label.afkTime')}
                    value={<FormattedTime timeMs={server.afkTime} />}
                    bold
                />
                <Datapoint
                    icon={faClock}
                    color={'sessions'}
                    name={t('html.label.longestSession')}
                    value={
                        <FormattedTime timeMs={server.longestSessionLength} />
                    }
                    bold
                />
                <Datapoint
                    icon={faClock}
                    color={'sessions'}
                    name={t('html.label.sessionMedian')}
                    value={<FormattedTime timeMs={server.sessionMedian} />}
                    bold
                />
                <br />
                <Datapoint
                    icon={faLocationArrow}
                    color={'join-addresses'}
                    name={t('html.label.joinAddress')}
                    value={server.joinAddress}
                />
                <br />
                <Datapoint
                    icon={faCrosshairs}
                    color='player-kills'
                    name={t('html.label.playerKills')}
                    value={server.playerKills}
                    bold
                />
                <Datapoint
                    icon={faCrosshairs}
                    color='mob-kills'
                    name={t('html.label.mobKills')}
                    value={server.mobKills}
                    bold
                />
                <Datapoint
                    icon={faSkull}
                    color='deaths'
                    name={t('html.label.deaths')}
                    value={server.deaths}
                    bold
                />
                <hr />
            </Col>
            <div className='col-xs-12 col-sm-12 col-md-12 col-lg-6'>
                <WorldPie
                    id={'worldpie_server_' + i}
                    worldSeries={server.worldPieSeries}
                    gmSeries={server.gmSeries}
                />

                <ServerPageLinkButton
                    uuid={server.serverUUID}
                    className={'float-end'}
                />
            </div>
        </Row>
    );
};

const ServerAccordion = ({ servers }) => {
    const { t } = useTranslation();
    return (
        <Accordion
            headers={[
                <>
                    <Fa icon={faServer} /> {t('html.label.server')}
                </>,
                <>
                    <Fa icon={faClock} /> {t('html.label.playtime')}
                </>,
                <>
                    <Fa icon={faCalendarPlus} /> {t('html.label.registered')}
                </>,
                <>
                    <Fa icon={faCalendarCheck} /> {t('html.label.lastSeen')}
                </>,
            ]}
            slices={servers.map((server) => {
                return {
                    body: <ServerBody server={server} />,
                    header: <ServerHeader server={server} />,
                    color: 'servers',
                    outline: true,
                };
            })}
        />
    );
};

export default ServerAccordion;
