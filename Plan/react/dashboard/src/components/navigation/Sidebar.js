import React, {useEffect, useState} from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import logo from '../../Flaticon_circle.png';
import {
    faCalendar,
    faCampground,
    faCubes,
    faDoorOpen,
    faDownload,
    faInfoCircle,
    faNetworkWired,
    faPalette,
    faQuestionCircle
} from "@fortawesome/free-solid-svg-icons";
import {NavLink} from "react-router-dom";
import {useTheme} from "../../hooks/themeHook";
import PluginInformationModal from "../modal/PluginInformationModal";
import VersionInformationModal from "../modal/VersionInformationModal";

const Logo = () => (
    <a className="sidebar-brand d-flex align-items-center justify-content-center" href="/">
        <img alt="logo" className="w-22" src={logo}/>
    </a>
)

const Divider = () => (
    <hr className="sidebar-divider my-0"/>
)

const Item = ({href, icon, name}) => (
    <li className={"nav-item nav-button"}>
        <NavLink to={href} className={({isActive}) => isActive ? "nav-link active" : "nav-link"}>
            <Fa icon={icon}/> <span>{name}</span>
        </NavLink>
    </li>
)

const VersionButton = ({toggleVersionModal, versionInfo}) => {
    if (versionInfo.updateAvailable) {
        return <button className="btn bg-white col-plan" onClick={toggleVersionModal}>
            <Fa icon={faDownload}/> Update Available!
        </button>;
    }

    return <button className="btn bg-transparent-light" onClick={toggleVersionModal}>
        {versionInfo.currentVersion}
    </button>;
}

const FooterButtons = () => {
    const {toggleColorChooser} = useTheme();

    const [infoModalOpen, setInfoModalOpen] = useState(false);
    const toggleInfoModal = () => setInfoModalOpen(!infoModalOpen);

    const [versionModalOpen, setVersionModalOpen] = useState(false);
    const toggleVersionModal = () => setVersionModalOpen(!versionModalOpen);

    const [versionInfo, setVersionInfo] = useState({currentVersion: 'Loading..', updateAvailable: false});
    // TODO Load version info from backend
    useEffect(() => {
        setVersionInfo({
            currentVersion: '6.0 build 1672',
            updateAvailable: Math.random() > 0.5,
            newVersion: '6.0 build 1673',
            downloadUrl: '',
            changelogUrl: '',
            isRelease: false
        })
    }, [])

    return (
        <>
            <div className="mt-2 ms-md-3 text-center text-md-start">
                <button className="btn bg-transparent-light" onClick={toggleColorChooser}>
                    <Fa icon={faPalette}/>
                </button>
                <button className="btn bg-transparent-light" onClick={toggleInfoModal}>
                    <Fa icon={faQuestionCircle}/>
                </button>
                <a className="btn bg-transparent-light" href="../auth/logout" id="logout-button">
                    <Fa icon={faDoorOpen}/> Logout
                </a>
            </div>
            <div className="ms-md-3 text-center text-md-start">
                <VersionButton toggleVersionModal={toggleVersionModal} versionInfo={versionInfo}/>
            </div>
            <PluginInformationModal open={infoModalOpen} toggle={toggleInfoModal}/>
            <VersionInformationModal open={versionModalOpen} toggle={toggleVersionModal} versionInfo={versionInfo}/>
        </>
    )
}

const Sidebar = () => {
    const {color} = useTheme();

    return (
        <ul className={"navbar-nav sidebar sidebar-dark accordion bg-" + color} id="accordionSidebar">
            <Logo/>
            <Divider/>
            <Item active={true} href={"overview"} icon={faInfoCircle} name="Player Overview"/>
            <Item active={false} href={"sessions"} icon={faCalendar} name="Sessions"/>
            <Item active={false} href={"pvppve"} icon={faCampground} name="PvP & PvE"/>
            <Item active={false} href={"servers"} icon={faNetworkWired} name="Servers Overview"/>
            <Item active={false} href={"plugins/Server 1"} icon={faCubes} name="Plugins (Server 1)"/>
            <Divider/>
            <FooterButtons/>
        </ul>
    )
}

export default Sidebar