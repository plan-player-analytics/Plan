import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import logo from '../../Flaticon_circle.png';
import {
    faCalendar,
    faCampground,
    faCubes,
    faDoorOpen,
    faInfoCircle,
    faNetworkWired,
    faPalette,
    faQuestionCircle
} from "@fortawesome/free-solid-svg-icons";
import {NavLink} from "react-router-dom";
import {useTheme} from "../../hooks/themeHook";

const Logo = () => (
    <a className="sidebar-brand d-flex align-items-center justify-content-center">
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

const FooterButtons = () => {
    const {color, toggleColorChooser} = useTheme();

    return (
        <>
            <div className="mt-2 ms-md-3 text-center text-md-start">
                <button className={"btn bg-" + color} onClick={toggleColorChooser} type="button">
                    <Fa icon={faPalette}/>
                </button>
                <button className={"btn bg-" + color} data-bs-target="#informationModal" data-bs-toggle="modal"
                        type="button">
                    <Fa icon={faQuestionCircle}/>
                </button>
                <a className={"btn bg-" + color} href="../auth/logout" id="logout-button">
                    <Fa icon={faDoorOpen}/> Logout
                </a>
            </div>
            <div className="ms-md-3 text-center text-md-start">
                <button className={"btn bg-" + color} data-bs-target="#updateModal" data-bs-toggle="modal"
                        type="button">
                    6.0 build 1672
                </button>
            </div>
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