import React, {useCallback, useEffect, useState} from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import logo from '../../Flaticon_circle.png';
import {faDoorOpen, faDownload, faPalette, faQuestionCircle} from "@fortawesome/free-solid-svg-icons";
import {NavLink, useLocation} from "react-router-dom";
import {useTheme} from "../../hooks/themeHook";
import PluginInformationModal from "../modal/PluginInformationModal";
import VersionInformationModal from "../modal/VersionInformationModal";
import {fetchPlanVersion} from "../../service/metadataService";
import {useAuth} from "../../hooks/authenticationHook";
import {useNavigation} from "../../hooks/navigationHook";
import {useTranslation} from "react-i18next";
import {Collapse} from "react-bootstrap";
import {baseAddress} from "../../service/backendConfiguration";
import PageNavigationItem from "./PageNavigationItem";
import {useWindowWidth} from "../../hooks/interaction/windowWidthHook.jsx";

const Logo = () => (
    <a className="sidebar-brand d-flex align-items-center justify-content-center" href="/">
        <img alt="logo" className="w-22" src={logo}/>
    </a>
)

const Divider = ({showMargin}) => (
    <hr className={"sidebar-divider" + (showMargin ? '' : " my-0")}/>
)

const InnerItem = ({href, icon, name, nameShort, color, external, collapseSidebar}) => {
    if (!href) {
        return (<hr className={"nav-servers dropdown-divider mx-3 my-2"}/>)
    }

    if (external) {
        return (
            <a href={href} className="collapse-item nav-button">
                <Fa icon={icon} className={color ? "col-" + color : ""}/>
                <span>{nameShort ? nameShort : name}</span>
            </a>
        )
    }

    return <NavLink to={href} onClick={collapseSidebar} className={({isActive}) => {
        return isActive ? "collapse-item nav-button active" : "collapse-item nav-button"
    }}>
        <Fa icon={icon} className={color ? "col-" + color : ""}/> <span>{nameShort ? nameShort : name}</span>
    </NavLink>
}

export const Item = ({item, inner, collapseSidebar}) => {
    const {setCurrentTab} = useNavigation();
    const {pathname} = useLocation();
    const {t} = useTranslation();

    const {href, name, nameShort, color, icon, external} = item;

    useEffect(() => {
        if (!external && '/' !== href && pathname.includes(href)) setCurrentTab(name);
    }, [pathname, href, setCurrentTab, name, external])

    if (inner) {
        return (<InnerItem href={href} icon={icon} name={t(name)} nameShort={t(nameShort)} color={color}
                           external={external} collapseSidebar={collapseSidebar}/>)
    }

    if (external) {
        return (
            <li className={"nav-item nav-button"}>
                <a href={baseAddress + href} className="nav-link">
                    <Fa icon={icon} className={color ? "col-" + color : ""}/>
                    <span>{t(nameShort ? nameShort : name)}</span>
                </a>
            </li>
        )
    }

    return (
        <li className={"nav-item nav-button"}>
            <NavLink to={href} onClick={collapseSidebar} className={({isActive}) => {
                return isActive ? "nav-link active" : "nav-link"
            }}>
                <Fa icon={icon} className={color ? "col-" + color : ""}/> <span>{t(name)}</span>
            </NavLink>
        </li>
    );
}

const VersionButton = ({toggleVersionModal, versionInfo}) => {
    if (versionInfo.updateAvailable) {
        return <button className="btn bg-white col-theme" onClick={toggleVersionModal}>
            <Fa icon={faDownload}/> Update Available!
        </button>;
    }

    return <button className="btn bg-transparent-light" onClick={toggleVersionModal}>
        {versionInfo.currentVersion}
    </button>;
}

const FooterButtons = ({collapseSidebar, toggleInfoModal, toggleVersionModal, versionInfo}) => {
    const {t} = useTranslation();
    const {toggleColorChooser} = useTheme();
    const openColorChooser = useCallback(() => {
        toggleColorChooser();
        collapseSidebar();
    }, [toggleColorChooser, collapseSidebar]);
    const {authRequired} = useAuth();

    return (
        <div className={"footer-buttons"}>
            <div className="mt-2 ms-md-3 text-center text-md-start">
                <button className="btn bg-transparent-light" onClick={openColorChooser}
                        title={t('html.label.themeSelect')}>
                    <Fa icon={faPalette}/>
                </button>
                <button className="btn bg-transparent-light" onClick={toggleInfoModal}
                        title={t('html.modal.info.text')}>
                    <Fa icon={faQuestionCircle}/>
                </button>
                {authRequired ?
                    <a className="btn bg-transparent-light" href={baseAddress + "/auth/logout"} id="logout-button">
                        <Fa icon={faDoorOpen}/> {t('html.login.logout')}
                    </a> : ''}
            </div>
            <div className="ms-md-3 text-center text-md-start">
                <VersionButton toggleVersionModal={toggleVersionModal} versionInfo={versionInfo}/>
            </div>
        </div>
    )
}

const SidebarCollapse = ({item, open, setOpen, collapseSidebar}) => {
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
                <Fa icon={item.icon} className={item?.color ? "col-" + item?.color : ""}/>
                <span>{t(item.name)}</span>
            </button>
            <Collapse in={open}>
                <div id={item.name + "-collapse"}>
                    <div className="sidebar-collapse py-2 collapse-inner rounded">
                        {item.contents
                            .filter(content => content !== undefined)
                            .map(content =>
                                <Item key={JSON.stringify(content)}
                                      inner
                                      active={false}
                                      item={content}
                                      collapseSidebar={collapseSidebar}
                                />)}
                    </div>
                </div>
            </Collapse>
        </li>
    )
}

const renderItem = (item, i, openCollapse, setOpenCollapse, t, windowWidth, collapseSidebar) => {
    if (item.contents) {
        return <SidebarCollapse key={i}
                                item={item}
                                open={windowWidth < 660 || (openCollapse && openCollapse === i)}
                                setOpen={() => setOpenCollapse(i)}
                                collapseSidebar={collapseSidebar}/>
    }

    if (item.href !== undefined) {
        return <Item key={i}
                     active={false}
                     item={item}
                     collapseSidebar={collapseSidebar}
        />
    }

    if (item.name) {
        return <div key={i} className="sidebar-heading">{t(item.name)}</div>
    }

    return <hr key={i} className="sidebar-divider"/>
}

const Sidebar = ({page, items, openItemIndex, keepOpen}) => {
    const {t} = useTranslation();
    const {currentTab, sidebarExpanded, setSidebarExpanded} = useNavigation();
    const {authRequired, hasPermission, hasChildPermission} = useAuth();

    const [openCollapse, setOpenCollapse] = useState(openItemIndex);
    const toggleCollapse = collapse => {
        setOpenCollapse(openCollapse === collapse ? undefined : collapse);
    }

    const windowWidth = useWindowWidth();

    const collapseSidebar = useCallback(() => {
        setSidebarExpanded(windowWidth > 1350);
        if (windowWidth < 660) {
            window.scrollTo({top: 0});
        }
    }, [setSidebarExpanded, windowWidth]);
    const collapseConditionallyOnItemClick = useCallback(() => {
        if (windowWidth < 660) {
            setTimeout(collapseSidebar, 10);
        }
    }, [collapseSidebar, windowWidth]);
    useEffect(collapseSidebar, [windowWidth, currentTab, setSidebarExpanded, collapseSidebar]);

    const [infoModalOpen, setInfoModalOpen] = useState(false);
    const toggleInfoModal = useCallback(() => {
        setInfoModalOpen(!infoModalOpen);
        collapseConditionallyOnItemClick();
    }, [setInfoModalOpen, infoModalOpen, collapseConditionallyOnItemClick]);

    const [versionModalOpen, setVersionModalOpen] = useState(false);
    const toggleVersionModal = useCallback(() => {
        setVersionModalOpen(!versionModalOpen);
        collapseConditionallyOnItemClick();
    }, [setVersionModalOpen, versionModalOpen, collapseConditionallyOnItemClick]);

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
    }, []);

    const isVisible = (item) => {
        return !authRequired || !item.permission || hasPermission(item.permission) || hasChildPermission(item.permission)
    }

    return (
        <>
            {(sidebarExpanded || keepOpen) &&
                <ul className={"navbar-nav sidebar sidebar-dark accordion"} id="accordionSidebar">
                    <Logo/>
                    <PageNavigationItem page={page}/>
                    <Divider showMargin={items.length && !items[0].contents && items[0].href === undefined}/>
                    {items.length ? items.filter(item => item !== undefined)
                        .filter(isVisible)
                        .map((item, i) => renderItem(item, i, openCollapse, toggleCollapse, t, windowWidth, collapseConditionallyOnItemClick)) : ''}
                    <Divider/>
                    <FooterButtons
                        collapseSidebar={collapseConditionallyOnItemClick}
                        toggleInfoModal={toggleInfoModal}
                        toggleVersionModal={toggleVersionModal}
                        versionInfo={versionInfo}
                    />
                </ul>}
            <PluginInformationModal open={infoModalOpen} toggle={toggleInfoModal}/>
            <VersionInformationModal open={versionModalOpen} toggle={toggleVersionModal} versionInfo={versionInfo}/>
        </>
    )
}

export default Sidebar