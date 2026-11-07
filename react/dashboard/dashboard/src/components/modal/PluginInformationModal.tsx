import React from 'react';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import { Modal } from 'react-bootstrap';
import {
    faBug,
    faChartArea,
    faCode,
    faExclamationTriangle,
    faGraduationCap,
    faLanguage,
    faQuestionCircle,
    faStar,
} from '@fortawesome/free-solid-svg-icons';
import { faDiscord } from '@fortawesome/free-brands-svg-icons';
import { useMetadata } from '../../hooks/metadataHook';
import { Trans, useTranslation } from 'react-i18next';
import ActionButton from '../input/button/ActionButton';
import ExternalLink from '../input/button/ExternalLink';
import ModalCloseButton from '../input/button/ModalCloseButton';
import {
    ContributedFor,
    Contributor as ContributorType,
} from '../../hooks/model/Metadata';

const LicenseSection = () => {
    const { t } = useTranslation();
    return (
        <p>
            {t('html.modal.info.license')}{' '}
            <a
                href='https://opensource.org/licenses/LGPL-3.0'
                rel='noopener noreferrer'
                target='_blank'>
                Lesser General Public License v3.0
            </a>
        </p>
    );
};

const Links = () => {
    const { t } = useTranslation();
    return (
        <>
            <ExternalLink href='https://github.com/plan-player-analytics/Plan/wiki'>
                <Fa icon={faGraduationCap} /> {t('html.modal.info.wiki')}
            </ExternalLink>
            <ExternalLink href='https://github.com/plan-player-analytics/Plan/issues'>
                <Fa icon={faBug} /> {t('html.modal.info.bugs')}
            </ExternalLink>
            <ExternalLink href='https://discord.gg/yXKmjzT'>
                <Fa icon={faDiscord} /> {t('html.modal.info.discord')}
            </ExternalLink>
        </>
    );
};

const getContributionIcon = (type: ContributedFor) => {
    switch (type) {
        case 'LANG':
            return faLanguage;
        case 'CODE':
            return faCode;
        default:
            return faExclamationTriangle;
    }
};

const Contributor = ({ contributor }: { contributor: ContributorType }) => {
    const icons = contributor.contributed.map((type) => (
        <Fa key={'' + type} icon={getContributionIcon(type)} />
    ));
    return (
        <li className='contributor'>
            {contributor.name} {icons}{' '}
        </li>
    );
};

const Contributions = () => {
    const { t } = useTranslation();
    const metadata = useMetadata();
    if (!metadata.loaded) return null;
    const { contributors } = metadata;

    return (
        <>
            <p>Player Analytics {t('html.modal.info.developer')} AuroraLS3.</p>
            <p>
                <Trans
                    i18nKey='html.modal.info.contributors.text'
                    shouldUnescape={true}
                    components={{ 1: <span className='col-theme' /> }}
                />
            </p>
            <ul className='row contributors'>
                {contributors.map((contributor) => (
                    <Contributor
                        key={contributor.name}
                        contributor={contributor}
                    />
                ))}
                <li>{t('html.modal.info.contributors.bugreporters')}</li>
            </ul>
            <small>
                <Fa icon={faCode} /> {t('html.modal.info.contributors.code')}{' '}
                <Fa icon={faLanguage} />{' '}
                {t('html.modal.info.contributors.translator')}
            </small>
            <hr />
            <p className='col-theme'>
                {t('html.modal.info.contributors.donate')}
                <Fa icon={faStar} className={'col-amber'} />
            </p>
        </>
    );
};

const MetricsLinks = () => {
    const { t } = useTranslation();
    return (
        <>
            <h6>{t('html.modal.info.metrics')}</h6>
            <ExternalLink href='https://bstats.org/plugin/bukkit/Plan'>
                <Fa icon={faChartArea} /> Bukkit
            </ExternalLink>
            <ExternalLink href='https://bstats.org/plugin/bungeecord/Plan'>
                <Fa icon={faChartArea} /> BungeeCord
            </ExternalLink>
            <ExternalLink href='https://bstats.org/plugin/sponge/plan'>
                <Fa icon={faChartArea} /> Sponge
            </ExternalLink>
            <ExternalLink href='https://bstats.org/plugin/velocity/Plan/10326'>
                <Fa icon={faChartArea} /> Velocity
            </ExternalLink>
        </>
    );
};

type Props = {
    open: boolean;
    toggle: () => void;
};

const PluginInformationModal = ({ open, toggle }: Props) => {
    const { t } = useTranslation();
    return (
        <Modal
            id='informationModal'
            aria-labelledby='informationModalLabel'
            show={open}
            onHide={toggle}
            size='lg'>
            <Modal.Header>
                <Modal.Title id='informationModalLabel'>
                    <Fa icon={faQuestionCircle} /> {t('html.modal.info.text')}
                </Modal.Title>
                <ModalCloseButton onClick={toggle} />
            </Modal.Header>
            <Modal.Body>
                <LicenseSection />
                <hr />
                <Links />
                <hr />
                <Contributions />
                <hr />
                <MetricsLinks />
            </Modal.Body>
            <Modal.Footer>
                <ActionButton onClick={toggle}>OK</ActionButton>
            </Modal.Footer>
        </Modal>
    );
};

export default PluginInformationModal;
