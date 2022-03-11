import React, {useEffect, useState} from "react";
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

const Contributor = ({contributor}) => {
    const icons = contributor.contributions.map(
        (icon, i) => <Fa key={i} icon={["fa", icon]}/>);
    return (
        <li className="col-4">{contributor.name} {icons} </li>
    )
}

function getContributorsTEMP() {
    const CODE = "code";
    const LANG = "language";

    return [{name: "aidn5", contributions: [CODE]},
        {name: "Antonok", contributions: [CODE]},
        {name: "Argetan", contributions: [CODE]},
        {name: "Aurelien", contributions: [CODE, LANG]},
        {name: "BrainStone", contributions: [CODE]},
        {name: "Catalina", contributions: [LANG]},
        {name: "Elguerrero", contributions: [LANG]},
        {name: "Combustible", contributions: [CODE]},
        {name: "Creeperface01", contributions: [CODE]},
        {name: "CyanTech", contributions: [LANG]},
        {name: "DarkPyves", contributions: [CODE]},
        {name: "DaveDevil", contributions: [LANG]},
        {name: "developStorm", contributions: [CODE]},
        {name: "enterih", contributions: [LANG]},
        {name: "Eyremba", contributions: [LANG]},
        {name: "f0rb1d (\u4f5b\u58c1\u706f)", contributions: [LANG]},
        {name: "Fur_xia", contributions: [LANG]},
        {name: "fuzzlemann", contributions: [CODE, LANG]},
        {name: "Guinness_Akihiko", contributions: [LANG]},
        {name: "hallo1142", contributions: [LANG]},
        {name: "itaquito", contributions: [LANG]},
        {name: "jyhsu2000", contributions: [CODE]},
        {name: "jvmuller", contributions: [LANG]},
        {name: "Malachiel", contributions: [LANG]},
        {name: "Miclebrick", contributions: [CODE]},
        {name: "Morsmorse", contributions: [LANG]},
        {name: "MAXOUXAX", contributions: [CODE]},
        {name: "Nogapra", contributions: [LANG]},
        {name: "Sander0542", contributions: [LANG]},
        {name: "Saph1s", contributions: [LANG]},
        {name: "Shadowhackercz", contributions: [LANG]},
        {name: "shaokeyibb", contributions: [LANG]},
        {name: "skmedix", contributions: [CODE]},
        {name: "TDJisvan", contributions: [LANG]},
        {name: "Vankka", contributions: [CODE]},
        {name: "yukieji", contributions: [LANG]},
        {name: "qsefthuopq", contributions: [LANG]},
        {name: "Karlatemp", contributions: [CODE, LANG]},
        {name: "Mastory_Md5", contributions: [LANG]},
        {name: "FluxCapacitor2", contributions: [CODE]},
        {name: "galexrt", contributions: [LANG]},
        {name: "QuakyCZ", contributions: [LANG]},
        {name: "MrFriggo", contributions: [LANG]},
        {name: "vacoup", contributions: [CODE]},
        {name: "Kopo942", contributions: [CODE]},
        {name: "WolverStones", contributions: [LANG]},
        {name: "BruilsiozPro", contributions: [LANG]},
        {name: "AppleMacOS", contributions: [CODE]},
        {name: "10935336", contributions: [LANG]},
        {name: "EyuphanMandiraci", contributions: [LANG]},
        {name: "4drian3d", contributions: [LANG]},
        {name: "\u6d1b\u4f0a", contributions: [LANG]},
        {name: "portlek", contributions: [CODE]},
        {name: "mbax", contributions: [CODE]},
        {name: "KairuByte", contributions: [CODE]},
        {name: "rymiel", contributions: [CODE]},
        {name: "Perchun_Pak", contributions: [LANG]},
        {name: "HexedHero", contributions: [CODE]},
        {name: "DrexHD", contributions: [CODE]},
        {name: "zisunny104", contributions: [LANG]},
        {name: "SkipM4", contributions: [LANG]},
        {name: "ahdg6", contributions: [CODE]},
        {name: "BratishkaErik", contributions: [LANG]}];
}

const Contributions = () => {
    const [contributors, setContributors] = useState(getContributorsTEMP());

    useEffect(() => {
        // TODO Load contributors from backend.
        setContributors(getContributorsTEMP());
    }, []);

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