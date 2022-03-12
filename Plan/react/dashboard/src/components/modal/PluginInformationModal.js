import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {Modal} from "react-bootstrap-v5";
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

const LicenseSection = () => {
    return (
        <p>Player Analytics is developed and licensed under{' '}
            <a href="https://opensource.org/licenses/LGPL-3.0"
               rel="noopener noreferrer"
               target="_blank">
                Lesser General Public License v3.0
            </a>
        </p>
    )
}

const Links = () => {
    return (<>
            <a className="btn col-plan" href="https://github.com/plan-player-analytics/Plan/wiki"
               rel="noopener noreferrer" target="_blank">
                <Fa icon={faGraduationCap}/> Plan Wiki, Tutorials & Documentation
            </a>
            <a className="btn col-plan" href="https://github.com/plan-player-analytics/Plan/issues"
               rel="noopener noreferrer" target="_blank">
                <Fa icon={faBug}/> Report Issues</a>
            <a className="btn col-plan" href="https://discord.gg/yXKmjzT" rel="noopener noreferrer"
               target="_blank">
                <Fa icon={faDiscord}/> General Support on Discord
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
        (type, i) => <Fa key={i} icon={["fa", getContributionIcon(type)]}/>);
    return (
        <li className="col-4">{contributor.name} {icons} </li>
    )
}

const Contributions = () => {
    const metadata = useMetadata();
    const contributors = metadata.contributors ? metadata.contributors : [{
        name: '(Error getting contributors)',
        contributed: ['exclamation-triangle']
    }];

    return (<>
        <p>Player Analytics is developed by AuroraLS3.</p>
        <p>In addition following <span className="col-plan">awesome people</span> have
            contributed:</p>
        <ul className="row contributors">
            {contributors.map((contributor, i) => <Contributor key={i} contributor={contributor}/>)}
            <li>& Bug reporters!</li>
        </ul>
        <small>
            <Fa icon={faCode}/> code contributor <Fa icon={faLanguage}/> translator
        </small>
        <hr/>
        <p className="col-plan">
            Extra special thanks to those who have monetarily supported the development.
            <Fa icon={faStar} className={"col-amber"}/>
        </p>
    </>)
}

const MetricsLinks = () => {
    return (
        <>
            <h6>bStats Metrics</h6>
            <a className="btn col-plan" href="https://bstats.org/plugin/bukkit/Plan"
               rel="noopener noreferrer" target="_blank">
                <Fa icon={faChartArea}/> Bukkit
            </a>
            <a className="btn col-plan" href="https://bstats.org/plugin/bungeecord/Plan"
               rel="noopener noreferrer" target="_blank">
                <Fa icon={faChartArea}/> BungeeCord
            </a>
            <a className="btn col-plan" href="https://bstats.org/plugin/sponge/plan"
               rel="noopener noreferrer" target="_blank">
                <Fa icon={faChartArea}/> Sponge
            </a>
            <a className="btn col-plan" href="https://bstats.org/plugin/velocity/Plan/10326"
               rel="noopener noreferrer" target="_blank">
                <Fa icon={faChartArea}/> Velocity
            </a>
        </>
    );
}

const PluginInformationModal = ({open, toggle}) => {
    return (
        <Modal id="informationModal" aria-labelledby="informationModalLabel" show={open} onHide={toggle} size="lg">
            <Modal.Header>
                <Modal.Title id="informationModalLabel">
                    <Fa icon={faQuestionCircle}/> Information about the plugin
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
                <button className="btn bg-plan" onClick={toggle}>OK</button>
            </Modal.Footer>
        </Modal>
    )
}

export default PluginInformationModal;