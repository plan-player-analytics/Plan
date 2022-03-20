import React, {useEffect, useState} from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import logo from '../../Flaticon_circle.png';
import {faArrowLeft, faDoorOpen, faDownload, faPalette, faQuestionCircle} from "@fortawesome/free-solid-svg-icons";
import {NavLink} from "react-router-dom";
import {useTheme} from "../../hooks/themeHook";
import PluginInformationModal from "../modal/PluginInformationModal";
import VersionInformationModal from "../modal/VersionInformationModal";
import {fetchPlanVersion} from "../../service/metadataService";
import {useAuth} from "../../hooks/authenticationHook";
import {useNavigation} from "../../hooks/navigationHook";
import {useTranslation} from "react-i18next";
import {Collapse} from "react-bootstrap-v5";

const Logo = () => (
    <a className="sidebar-brand d-flex align-items-center justify-content-center" href="/">
        <img alt="logo" className="w-22" src={logo}/>
    </a>
)

const Divider = () => (
    <hr className="sidebar-divider my-0"/>
)

const InnerItem = ({href, icon, name, nameShort}) => {
    const {setCurrentTab} = useNavigation();

    if (href.startsWith('/')) {
        return (
            <a href={href} className="collapse-item nav-button">
                <Fa icon={icon}/> <span>{nameShort ? nameShort : name}</span>
            </a>
        )
    }

    return <NavLink to={href} className={({isActive}) => {
        if (isActive) setCurrentTab(name);
        return isActive ? "collapse-item nav-button active" : "collapse-item nav-button"
    }}>
        <Fa icon={icon}/> <span>{nameShort ? nameShort : name}</span>
    </NavLink>
}

const Item = ({href, icon, name, nameShort, inner}) => {
    const {setCurrentTab} = useNavigation();

    if (inner) {
        return (<InnerItem href={href} icon={icon} name={name} nameShort={nameShort}/>)
    }

    if (href.startsWith('/')) {
        return (
            <li className={"nav-item nav-button"}>
                <a href={href} className="nav-link">
                    <Fa icon={icon}/> <span>{nameShort ? nameShort : name}</span>
                </a>
            </li>
        )
    }

    return (
        <li className={"nav-item nav-button"}>
            <NavLink to={href} className={({isActive}) => {
                if (isActive) setCurrentTab(nameShort ? nameShort : name);
                return isActive ? "nav-link active" : "nav-link"
            }}>
                <Fa icon={icon}/> <span>{name}</span>
            </NavLink>
        </li>
    );
}

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

const SidebarCollapse = ({item, open, setOpen}) => {
    const toggle = event => {
        event.preventDefault();
        setOpen(!open);
    }

    return (
        <li className="nav-item">
            <a className="nav-link"
               onClick={toggle}
               aria-controls={item.name + "-collapse"}
               aria-expanded={open}
               data-bs-toggle="collapse"
               href="#"
            >
                <Fa icon={item.icon}/> <span>{item.name}</span>
            </a>
            <Collapse in={open}>
                <div id={item.name + "-collapse"}>
                    <div className="bg-white py-2 collapse-inner rounded">
                        {item.contents.map((content, i) =>
                            <Item key={i}
                                  inner
                                  active={false}
                                  href={content.href}
                                  icon={content.icon}
                                  name={content.name}
                                  nameShort={content.nameShort}
                            />)}
                    </div>
                </div>
            </Collapse>
        </li>
    )
}

const renderItem = (item, i, openCollapse, setOpenCollapse) => {
    if (item.contents) {
        return <SidebarCollapse key={i}
                                item={item}
                                open={openCollapse === item.name}
                                setOpen={() => setOpenCollapse(item.name)}/>
    }

    if (item.href) {
        return <Item key={i}
                     active={false}
                     href={item.href}
                     icon={item.icon}
                     name={item.name}
                     nameShort={item.nameShort}
        />
    }

    if (item.name) {
        return <div key={i} className="sidebar-heading">{item.name}</div>
    }

    return <hr key={i} className="sidebar-divider"/>
}

const Sidebar = ({items, showBackButton}) => {
    const {t} = useTranslation();
    const {color} = useTheme();

    const [openCollapse, setOpenCollapse] = useState(undefined);
    const toggleCollapse = collapse => {
        setOpenCollapse(openCollapse === collapse ? undefined : collapse);
    }

    return (
        <ul className={"navbar-nav sidebar sidebar-dark accordion bg-" + color} id="accordionSidebar">
            <Logo/>
            <Divider/>
            {showBackButton ? <>
                <Item active={false} href="/" icon={faArrowLeft} name={t('html.sidebar.toMainPage')}/>
                <Divider/>
            </> : ''}
            {items.map((item, i) => renderItem(item, i, openCollapse, toggleCollapse))}
            <Divider/>
            <FooterButtons/>
        </ul>
    )
}

export default Sidebar