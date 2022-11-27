import React, {useCallback, useEffect, useState} from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import logo from '../../Flaticon_circle.png';
import {faArrowLeft, faDoorOpen, faDownload, faPalette, faQuestionCircle} from "@fortawesome/free-solid-svg-icons";
import {NavLink, useLocation} from "react-router-dom";
import {useTheme} from "../../hooks/themeHook";
import PluginInformationModal from "../modal/PluginInformationModal";
import VersionInformationModal from "../modal/VersionInformationModal";
import {fetchPlanVersion} from "../../service/metadataService";
import {useAuth} from "../../hooks/authenticationHook";
import {useNavigation} from "../../hooks/navigationHook";
import {useTranslation} from "react-i18next";
import {Collapse} from "react-bootstrap-v5";
import {baseAddress} from "../../service/backendConfiguration";

const Logo = () => (
    <a className="sidebar-brand d-flex align-items-center justify-content-center" href="/">
        <img alt="logo" className="w-22" src={logo}/>
    </a>
)

const Divider = ({showMargin}) => (
    <hr className={"sidebar-divider" + (showMargin ? '' : " my-0")}/>
)

const InnerItem = ({href, icon, name, nameShort, color}) => {
    if (!href) {
        return (<hr className={"nav-servers dropdown-divider mx-3 my-2"}/>)
    }

    if (href.startsWith('/')) {
        return (
            <a href={href} className="collapse-item nav-button">
                <Fa icon={icon} className={color ? "col-" + color : undefined}/>
                <span>{nameShort ? nameShort : name}</span>
            </a>
        )
    }

    return <NavLink to={href} className={({isActive}) => {
        return isActive ? "collapse-item nav-button active" : "collapse-item nav-button"
    }}>
        <Fa icon={icon} className={color ? "col-" + color : undefined}/> <span>{nameShort ? nameShort : name}</span>
    </NavLink>
}

const Item = ({item, inner}) => {
    const {setCurrentTab} = useNavigation();
    const {pathname} = useLocation();
    const {t} = useTranslation();

    const {href, name, nameShort, color, icon, external} = item;

    useEffect(() => {
        if (!external && '/' !== href && pathname.includes(href)) setCurrentTab(name);
    }, [pathname, href, setCurrentTab, name, external])

    if (inner) {
        return (<InnerItem href={href} icon={icon} name={t(name)} nameShort={t(nameShort)} color={color}/>)
    }

    if (href.startsWith('/')) {
        return (
            <li className={"nav-item nav-button"}>
                <a href={baseAddress + href} className="nav-link">
                    <Fa icon={icon} className={color ? "col-" + color : undefined}/>
                    <span>{t(nameShort ? nameShort : name)}</span>
                </a>
            </li>
        )
    }

    return (
        <li className={"nav-item nav-button"}>
            <NavLink to={href} className={({isActive}) => {
                return isActive ? "nav-link active" : "nav-link"
            }}>
                <Fa icon={icon} className={color ? "col-" + color : undefined}/> <span>{t(name)}</span>
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
    const {t} = useTranslation();
    const {toggleColorChooser} = useTheme();
    const {authRequired} = useAuth();

    const [infoModalOpen, setInfoModalOpen] = useState(false);
    const toggleInfoModal = () => setInfoModalOpen(!infoModalOpen);

    const [versionModalOpen, setVersionModalOpen] = useState(false);
    const toggleVersionModal = () => setVersionModalOpen(!versionModalOpen);

    const [versionInfo, setVersionInfo] = useState({currentVersion: 'Loading..', updateAvailable: false});

    const loadVersion = async () => {
        const {data, error} = await fetchPlanVersion();
        if (data) {
            setVersionInfo(data);
        } else if (error) {
            setVersionInfo({currentVersion: "Error getting version", updateAvailable: false})
        }
    }

    useEffect(() => {
        loadVersion();
    }, [])

    return (
        <>
            <div className="mt-2 ms-md-3 text-center text-md-start">
                <button className="btn bg-transparent-light" onClick={toggleColorChooser}
                        title={t('html.label.themeSelect')}>
                    <Fa icon={faPalette}/>
                </button>
                <button className="btn bg-transparent-light" onClick={toggleInfoModal}
                        title={t('html.modal.info.text')}>
                    <Fa icon={faQuestionCircle}/>
                </button>
                {authRequired ?
                    <a className="btn bg-transparent-light" href={baseAddress + "/auth/logout"} id="logout-button">
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
    const {t} = useTranslation();
    const toggle = event => {
        event.preventDefault();
        setOpen(!open);
    }

    return (
        <li className="nav-item">
            <button className="nav-link"
                    onClick={toggle}
                    aria-controls={item.name + "-collapse"}
                    aria-expanded={open}
                    data-bs-toggle="collapse"
            >
                <Fa icon={item.icon} className={item?.color ? "col-" + item?.color : undefined}/>
                <span>{t(item.name)}</span>
            </button>
            <Collapse in={open}>
                <div id={item.name + "-collapse"}>
                    <div className="bg-white py-2 collapse-inner rounded">
                        {item.contents.map((content, i) =>
                            <Item key={i}
                                  inner
                                  active={false}
                                  item={content}
                            />)}
                    </div>
                </div>
            </Collapse>
        </li>
    )
}

const renderItem = (item, i, openCollapse, setOpenCollapse, t) => {
    if (item.contents) {
        return <SidebarCollapse key={i}
                                item={item}
                                open={openCollapse && openCollapse === i}
                                setOpen={() => setOpenCollapse(i)}/>
    }

    if (item.href !== undefined) {
        return <Item key={i}
                     active={false}
                     item={item}
        />
    }

    if (item.name) {
        return <div key={i} className="sidebar-heading">{t(item.name)}</div>
    }

    return <hr key={i} className="sidebar-divider"/>
}

const Sidebar = ({items, showBackButton}) => {
    const {t} = useTranslation();
    const {color} = useTheme();
    const {currentTab, sidebarExpanded, setSidebarExpanded} = useNavigation();

    const [openCollapse, setOpenCollapse] = useState(undefined);
    const toggleCollapse = collapse => {
        setOpenCollapse(openCollapse === collapse ? undefined : collapse);
    }

    const [windowWidth, setWindowWidth] = useState(window.innerWidth);
    const updateWidth = useCallback(() => setWindowWidth(window.innerWidth), []);
    useEffect(() => {
        window.addEventListener('resize', updateWidth);
        return () => window.removeEventListener('resize', updateWidth);
    }, [updateWidth]);

    const collapseSidebar = () => setSidebarExpanded(windowWidth > 1350);
    useEffect(collapseSidebar, [windowWidth, currentTab, setSidebarExpanded]);

    return (
        <>
            {sidebarExpanded &&
                <ul className={"navbar-nav sidebar sidebar-dark accordion bg-" + color} id="accordionSidebar">
                    <Logo/>
                    <Divider/>
                    {showBackButton && <>
                        <Item active={false} item={{href: "/", icon: faArrowLeft, name: t('html.label.toMainPage')}}/>
                        <Divider showMargin={items.length && !items[0].contents && items[0].href === undefined}/>
                    </>}
                    {items.length ? items.filter(item => item !== undefined).map((item, i) => renderItem(item, i, openCollapse, toggleCollapse, t)) : ''}
                    <Divider/>
                    <FooterButtons/>
                </ul>}
        </>
    )
}

export default Sidebar