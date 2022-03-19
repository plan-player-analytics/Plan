import {useMetadata} from "../../hooks/metadataHook";
import {useAuth} from "../../hooks/authenticationHook";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCog, faDoorOpen, faPalette, faSyncAlt} from "@fortawesome/free-solid-svg-icons";
import DropdownMenu from "react-bootstrap-v5/lib/esm/DropdownMenu";
import DropdownItem from "react-bootstrap-v5/lib/esm/DropdownItem";
import {useTheme} from "../../hooks/themeHook";
import {Dropdown} from "react-bootstrap-v5";
import DropdownToggle from "react-bootstrap-v5/lib/esm/DropdownToggle";

const Header = ({page, tab}) => {
    const {requiresAuth, user} = useAuth();
    const {toggleColorChooser} = useTheme();

    const {getPlayerHeadImageUrl} = useMetadata();
    const headImageUrl = user ? getPlayerHeadImageUrl(user.username, user.linkedToUuid) : undefined
    // <!-- <li><a className="dropdown-item" href="#"><i className="fas fa-users-cog"></i> Web users</a></li>-->
    // <!-- <li><a className="dropdown-item" href="#"><i className="fas fa-code"></i> API access</a></li>-->
    // <!-- <li>-->
    // <!--    <hr className="dropdown-divider">-->
    // <!-- </li>-->
    return (
        <nav className="nav mt-3 align-items-center justify-content-between container-fluid">
            <div className="d-sm-flex">
                <h1 className="h3 mb-0 text-gray-800"><i
                    className="sidebar-toggler fa fa-fw fa-bars"/>{page}
                    {tab ? <>{' '}&middot; {tab}</> : ''}</h1>
            </div>

            <span className="topbar-divider"/>
            <div className="refresh-element">
                <Fa icon={faSyncAlt} spin={false}/> <span className="refresh-time">Today, 9:28</span>
            </div>

            <div className="ms-auto">
                <select aria-label="Language selector" className="form-select form-select-sm" id="langSelector"/>
            </div>

            <div className="topbar-divider"/>

            <Dropdown className="nav-item">
                <DropdownToggle variant=''>
                    {requiresAuth ? <>
                        <span className="me-2">{user.username} </span>
                        <img alt="user img" className="rounded-circle" src={headImageUrl} style={{height: "2rem"}}/>
                    </> : <>
                        <Fa icon={faCog} className="me-2"/>
                    </>}
                </DropdownToggle>

                <DropdownMenu>
                    <DropdownItem onClick={toggleColorChooser}>
                        <Fa icon={faPalette}/> Select a theme
                    </DropdownItem>
                    {requiresAuth ? <DropdownItem href="./auth/logout">
                        <Fa icon={faDoorOpen}/> Sign out
                    </DropdownItem> : ''}
                </DropdownMenu>
            </Dropdown>
        </nav>
    )
}

export default Header;