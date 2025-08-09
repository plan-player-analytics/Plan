import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {Modal} from "react-bootstrap";
import {
    faBug,
    faChartArea,
    faCode,
    faGraduationCap,
    faLanguage,
    faQuestionCircle,
    faStar
} from "@fortawesome/free-solid-svg-icons";
import {faDiscord} from "@fortawesome/free-brands-svg-icons";
import {useMetadata} from "../../hooks/metadataHook";
import {Trans, useTranslation} from "react-i18next";
import ActionButton from "../input/ActionButton.jsx";

const LicenseSection = () => {
    const {t} = useTranslation();
    return (
        <p>{t('html.modal.info.license')}{' '}
            <a href="https://opensource.org/licenses/LGPL-3.0"
               rel="noopener noreferrer"
               target="_blank">
                Lesser General Public License v3.0
            </a>
        </p>
    )
}

const Links = () => {
    const {t} = useTranslation();
    return (<>
            <a className="btn col-theme" href="https://github.com/plan-player-analytics/Plan/wiki"
               rel="noopener noreferrer" target="_blank">
                <Fa icon={faGraduationCap}/> {t('html.modal.info.wiki')}
            </a>
            <a className="btn col-theme" href="https://github.com/plan-player-analytics/Plan/issues"
               rel="noopener noreferrer" target="_blank">
                <Fa icon={faBug}/> {t('html.modal.info.bugs')}</a>
            <a className="btn col-theme" href="https://discord.gg/yXKmjzT" rel="noopener noreferrer"
               target="_blank">
                <Fa icon={faDiscord}/> {t('html.modal.info.discord')}
            </a>
        </>
    )
}

const getContributionIcon = (type) => {
    switch (type) {
        case "LANG":
            return 'language';
        case "CODE":
            return 'code';
        default:
            return "exclamation-triangle";
    }
}

const Contributor = ({contributor}) => {
    const icons = contributor.contributed.map(
        type => <Fa key={"" + type} icon={["fa", getContributionIcon(type)]}/>);
    return (
        <li className="contributor">{contributor.name} {icons} </li>
    )
}

const Contributions = () => {
    const {t} = useTranslation();
    const metadata = useMetadata();
    const contributors = metadata.contributors ? metadata.contributors : [{
        name: '(Error getting contributors)',
        contributed: ['exclamation-triangle']
    }];

    return (<>
        <p>Player Analytics {t('html.modal.info.developer')} AuroraLS3.</p>
        <p><Trans i18nKey="html.modal.info.contributors.text"
                  shouldUnescape={true}
                  components={{1: <span className="col-theme"/>}}
        /></p>
        <ul className="row contributors">
            {contributors.map((contributor, i) => <Contributor key={contributor.name} contributor={contributor}/>)}
            <li>{t('html.modal.info.contributors.bugreporters')}</li>
        </ul>
        <small>
            <Fa icon={faCode}/> {t('html.modal.info.contributors.code')} <Fa
            icon={faLanguage}/> {t('html.modal.info.contributors.translator')}
        </small>
        <hr/>
        <p className="col-theme">
            {t('html.modal.info.contributors.donate')}
            <Fa icon={faStar} className={"col-amber"}/>
        </p>
    </>)
}

const MetricsLinks = () => {
    const {t} = useTranslation();
    return (
        <>
            <h6>{t('html.modal.info.metrics')}</h6>
            <a className="btn col-theme" href="https://bstats.org/plugin/bukkit/Plan"
               rel="noopener noreferrer" target="_blank">
                <Fa icon={faChartArea}/> Bukkit
            </a>
            <a className="btn col-theme" href="https://bstats.org/plugin/bungeecord/Plan"
               rel="noopener noreferrer" target="_blank">
                <Fa icon={faChartArea}/> BungeeCord
            </a>
            <a className="btn col-theme" href="https://bstats.org/plugin/sponge/plan"
               rel="noopener noreferrer" target="_blank">
                <Fa icon={faChartArea}/> Sponge
            </a>
            <a className="btn col-theme" href="https://bstats.org/plugin/velocity/Plan/10326"
               rel="noopener noreferrer" target="_blank">
                <Fa icon={faChartArea}/> Velocity
            </a>
        </>
    );
}

const PluginInformationModal = ({open, toggle}) => {
    const {t} = useTranslation();
    return (
        <Modal id="informationModal" aria-labelledby="informationModalLabel" show={open} onHide={toggle} size="lg">
            <Modal.Header>
                <Modal.Title id="informationModalLabel">
                    <Fa icon={faQuestionCircle}/> {t('html.modal.info.text')}
                </Modal.Title>
                <button aria-label="Close" className="btn-close" type="button" onClick={toggle}/>
            </Modal.Header>
            <Modal.Body>
                <LicenseSection/>
                <hr/>
                <Links/>
                <hr/>
                <Contributions/>
                <hr/>
                <MetricsLinks/>
            </Modal.Body>
            <Modal.Footer>
                <ActionButton onClick={toggle}>OK</ActionButton>
            </Modal.Footer>
        </Modal>
    )
}

export default PluginInformationModal;