import {useMetadata} from "../../hooks/metadataHook";
import {useAuth} from "../../hooks/authenticationHook";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faBars, faCog, faDoorOpen, faPalette, faSyncAlt} from "@fortawesome/free-solid-svg-icons";
import DropdownMenu from "react-bootstrap-v5/lib/esm/DropdownMenu";
import DropdownItem from "react-bootstrap-v5/lib/esm/DropdownItem";
import {useTheme} from "../../hooks/themeHook";
import {Dropdown} from "react-bootstrap-v5";
import DropdownToggle from "react-bootstrap-v5/lib/esm/DropdownToggle";
import {localeService} from "../../service/localeService";
import {useTranslation} from "react-i18next";
import {useNavigation} from "../../hooks/navigationHook";

const LanguageSelector = () => {
    const languages = localeService.getLanguages();

    const onSelect = ({target}) => {
        localeService.loadLocale(target.value)
    }

    return (
        <select onChange={onSelect}
                aria-label="Language selector"
                className="form-select form-select-sm"
                id="langSelector"
                defaultValue={localeService.clientLocale}>
            {languages.map((lang, i) =>
                <option key={i} value={lang.name}>{lang.displayName}</option>)}
        </select>
    )
}

const Header = ({page, tab}) => {
    const {authRequired, user} = useAuth();
    const {toggleColorChooser} = useTheme();
    const {t} = useTranslation();

    const {requestUpdate, updating, lastUpdate, toggleSidebar} = useNavigation();

    const {getPlayerHeadImageUrl} = useMetadata();
    const headImageUrl = user ? getPlayerHeadImageUrl(user.playerName, user.linkedToUuid) : undefined
    // <!-- <li><a className="dropdown-item" href="#"><i className="fas fa-users-cog"></i> Web users</a></li>-->
    // <!-- <li><a className="dropdown-item" href="#"><i className="fas fa-code"></i> API access</a></li>-->
    // <!-- <li>-->
    // <!--    <hr className="dropdown-divider">-->
    // <!-- </li>-->
    return (
        <nav className="nav mt-3 align-items-center justify-content-between container-fluid">
            <div className="d-sm-flex">
                <h1 className="h3 mb-0 text-gray-800">
                    <button onClick={toggleSidebar}>
                        <Fa icon={faBars} className={"sidebar-toggler"}/>
                    </button>
                    {page}
                    {tab ? <>{' '}&middot; {t(tab)}</> : ''}</h1>
            </div>

            <span className="topbar-divider"/>
            <div className="refresh-element">
                <button onClick={requestUpdate}>
                    <Fa icon={faSyncAlt} spin={updating}/>
                </button>
                {' '}
                <span className="refresh-time">{lastUpdate.formatted}</span>
            </div>

            <div className="ms-auto">
                <LanguageSelector/>
            </div>

            <div className="topbar-divider"/>

            <Dropdown className="nav-item">
                <DropdownToggle variant=''>
                    {authRequired && user ? <>
                        <span className="me-1 login-username">{user.username} </span>
                        <img alt="user img" className="rounded-circle" src={headImageUrl} style={{height: "2rem"}}/>
                    </> : <>
                        <Fa icon={faCog} className="me-2"/>
                    </>}
                </DropdownToggle>

                <DropdownMenu>
                    <DropdownItem onClick={toggleColorChooser}>
                        <Fa icon={faPalette}/> {t('html.label.themeSelect')}
                    </DropdownItem>
                    {authRequired ? <DropdownItem href="./auth/logout">
                        <Fa icon={faDoorOpen}/> {t('html.login.logout')}
                    </DropdownItem> : ''}
                </DropdownMenu>
            </Dropdown>
        </nav>
    )
}

export default Header;