import {useMetadata} from "../../hooks/metadataHook";
import {useAuth} from "../../hooks/authenticationHook.tsx";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faBars, faClockRotateLeft, faCog, faDoorOpen, faPalette, faSyncAlt} from "@fortawesome/free-solid-svg-icons";
import {useTheme} from "../../hooks/themeHook";
import {Dropdown} from "react-bootstrap";
import {localeService} from "../../service/localeService";
import {useTranslation} from "react-i18next";
import {useNavigation} from "../../hooks/navigationHook";
import {baseAddress, staticSite} from "../../service/backendConfiguration";
import FormattedDate from "../text/FormattedDate";

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
            {languages.map(lang =>
                <option key={lang.name} value={lang.name}>{lang.displayName}</option>)}
        </select>
    )
}

const Header = ({page, tab, hideUpdater}) => {
    const {authRequired, user} = useAuth();
    const {toggleColorChooser} = useTheme();
    const {t} = useTranslation();

    const {requestUpdate, lastUpdate, updating, toggleSidebar} = useNavigation();

    const {getPlayerHeadImageUrl} = useMetadata();
    const headImageUrl = user ? getPlayerHeadImageUrl(user.playerName, user.playerUUID) : undefined
    return (
        <nav className="nav-header nav mt-3 align-items-center justify-content-between container-fluid">
            <div className="d-sm-flex">
                <h1 className="h3 mb-0 header">
                    <button onClick={toggleSidebar}>
                        <Fa icon={faBars} className={"sidebar-toggler"}/>
                    </button>
                    {page}
                    {tab ? <>{' '}&middot; {t(tab)}</> : ''}</h1>
            </div>

            {!hideUpdater && <>
                <span className="topbar-divider"/>
                <div className="refresh-element">
                    {!staticSite && <button onClick={requestUpdate}>
                        <Fa icon={faSyncAlt} spin={Boolean(updating)}/>
                    </button>}
                    {staticSite && <span title={t('html.label.exported')}><Fa icon={faClockRotateLeft}/></span>}
                    {' '}
                    <span className="refresh-time"><FormattedDate date={lastUpdate.date} react/></span>
                </div>
            </>}

            <div className="ms-auto">
                <LanguageSelector/>
            </div>

            <div className="topbar-divider"/>

            <Dropdown className="nav-item">
                <Dropdown.Toggle variant='' style={{'--bs-btn-color': 'var(--color-forms-input-text)'}}>
                    {authRequired && user ? <>
                        <span className="me-1 login-username">{user.username} </span>
                        <img alt="user img" className="rounded-circle" src={headImageUrl} style={{height: "2rem"}}/>
                    </> : <>
                        <Fa icon={faCog} className="me-2"/>
                    </>}
                </Dropdown.Toggle>

                <Dropdown.Menu>
                    <Dropdown.Item onClick={toggleColorChooser}>
                        <Fa icon={faPalette}/> {t('html.label.themeSelect')}
                    </Dropdown.Item>
                    {authRequired ? <Dropdown.Item href={baseAddress + "/auth/logout"}>
                        <Fa icon={faDoorOpen}/> {t('html.login.logout')}
                    </Dropdown.Item> : ''}
                </Dropdown.Menu>
            </Dropdown>
        </nav>
    )
}

export default Header;
