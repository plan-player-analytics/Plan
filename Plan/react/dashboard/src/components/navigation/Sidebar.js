import React, {useEffect, useState} from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import logo from '../../Flaticon_circle.png';
import {faDoorOpen, faDownload, faPalette, faQuestionCircle} from "@fortawesome/free-solid-svg-icons";
import {NavLink} from "react-router-dom";
import {useTheme} from "../../hooks/themeHook";
import PluginInformationModal from "../modal/PluginInformationModal";
import VersionInformationModal from "../modal/VersionInformationModal";
import {fetchPlanVersion} from "../../service/metadataService";
import {useAuth} from "../../hooks/authenticationHook";

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
    const {authRequired} = useAuth();

    const [infoModalOpen, setInfoModalOpen] = useState(false);
    const toggleInfoModal = () => setInfoModalOpen(!infoModalOpen);

    const [versionModalOpen, setVersionModalOpen] = useState(false);
    const toggleVersionModal = () => setVersionModalOpen(!versionModalOpen);

    const [versionInfo, setVersionInfo] = useState({currentVersion: 'Loading..', updateAvailable: false});

    const loadVersion = async () => {
        setVersionInfo(await fetchPlanVersion())
    }

    useEffect(() => {
        loadVersion();
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
                {authRequired ? <a className="btn bg-transparent-light" href="/auth/logout" id="logout-button">
                    <Fa icon={faDoorOpen}/> Logout
                </a> : ''}
            </div>
            <div className="ms-md-3 text-center text-md-start">
                <VersionButton toggleVersionModal={toggleVersionModal} versionInfo={versionInfo}/>
            </div>
            <PluginInformationModal open={infoModalOpen} toggle={toggleInfoModal}/>
            <VersionInformationModal open={versionModalOpen} toggle={toggleVersionModal} versionInfo={versionInfo}/>
        </>
    )
}

const Sidebar = ({items}) => {
    const {color} = useTheme();

    return (
        <ul className={"navbar-nav sidebar sidebar-dark accordion bg-" + color} id="accordionSidebar">
            <Logo/>
            <Divider/>
            {items.map((item, i) =>
                <Item key={i}
                      active={false}
                      href={item.href}
                      icon={item.icon}
                      name={item.name}
                />)}
            <Divider/>
            <FooterButtons/>
        </ul>
    )
}

export default Sidebar