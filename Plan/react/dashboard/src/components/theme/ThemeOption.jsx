import React, {useEffect, useRef} from 'react';
import {getLocallyStoredThemes, ThemeContextProvider, useTheme} from "../../hooks/themeHook.jsx";
import {ThemeStorageContextProvider} from "../../hooks/context/themeContextHook.tsx";
import {ThemeStyleCss} from "./ThemeStyleCss.tsx";
import {Card, Col} from "react-bootstrap";
import logo from "../../Flaticon_circle.png";
import drawSine from "../../util/loginSineRenderer.js";
import {calculateCssHexColor} from "../../util/colors.js";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCheck, faDesktop, faPencilAlt} from "@fortawesome/free-solid-svg-icons";
import OutlineButton from "../input/button/OutlineButton.jsx";
import {useAuth} from "../../hooks/authenticationHook.tsx";
import {useNavigate} from "react-router";
import {useTranslation} from "react-i18next";

const StorageIcon = ({theme}) => {
    const {t} = useTranslation();
    const onlyLocal = getLocallyStoredThemes().includes(theme);

    if (!onlyLocal) return <></>

    return (
        <small>(<FontAwesomeIcon icon={faDesktop}/> {t('html.value.localMachine')})</small>
    )
}

const ThemeOption = ({theme, nightMode, selected, setSelected}) => {
    const ref = useRef();
    useEffect(() => {
        drawSine(`theme-plot-${theme}`, calculateCssHexColor("var(--color-data-players-online)", ref.current));
    }, [ref]);
    const {authLoaded, authRequired, hasPermission} = useAuth();
    const themeHook = useTheme();
    const navigate = useNavigate();

    const managedComponent = !!setSelected;
    const canEdit = !managedComponent && authLoaded && (!authRequired || hasPermission('access.theme.editor'))

    const onClickChoose = () => {
        if (managedComponent) {
            setSelected(theme);
        } else {
            themeHook.setTheme(theme);
        }
    }

    const onClickEdit = () => {
        themeHook.toggleColorChooser();
        navigate(`/theme-editor/${theme}`);
    }

    return (
        <Col xs={managedComponent ? 3 : 4} style={{height: '100px', marginBottom: '1.5rem'}}>
            <button
                className={`theme-option theme-${theme} col-12 ${nightMode ? 'night-mode-colors' : ''} ${selected ? 'selected' : ''}`}
                onClick={onClickChoose}>
                <ThemeContextProvider themeOverride={theme} key={theme}>
                    <ThemeStorageContextProvider>
                        <ThemeStyleCss applyToClass={`theme-${theme}`}/>
                        <div ref={ref}/>
                        <Card style={{backgroundColor: 'var(--color-layout-background)'}}>
                            <p className={'col-text'} style={{
                                position: 'absolute',
                                left: '50%',
                                top: '100%',
                                transform: 'translateX(-50%)',
                                margin: 0,
                                zIndex: 5,
                                display: 'inline',
                                width: "100%"
                            }}>{theme} {managedComponent && <StorageIcon theme={theme}/>}</p>
                            <div style={{
                                background: 'var(--color-sidebar-background)',
                                width: '23%',
                                height: '80px'
                            }}>
                                <img alt="logo" style={{height: '10px', marginTop: '-8px'}} src={logo}/>
                                <div style={{
                                    backgroundColor: 'var(--color-forms-input-background)',
                                    border: '1px solid var(--color-forms-input-border)',
                                    marginLeft: 'auto',
                                    marginRight: 'auto',
                                    marginTop: '-5px',
                                    width: '75%',
                                    height: '4px'
                                }}/>
                                <div style={{
                                    backgroundColor: 'var(--color-sidebar-text)',
                                    marginLeft: 'auto',
                                    marginRight: 'auto',
                                    marginTop: '4px',
                                    width: '65%',
                                    height: '3px'
                                }}/>
                                <div style={{
                                    backgroundColor: 'var(--color-sidebar-collapsible-section-background)',
                                    border: '1px solid var(--color-sidebar-collapsible-section-border)',
                                    marginLeft: '20%',
                                    marginRight: 'auto',
                                    marginTop: '3px',
                                    width: '65%',
                                    height: '18px',
                                    paddingTop: '3px'
                                }}>
                                    <div style={{
                                        backgroundColor: 'var(--color-sidebar-collapsible-section-text)',
                                        marginLeft: 'auto',
                                        marginRight: 'auto',
                                        marginTop: '0',
                                        width: '75%',
                                        height: '2px'
                                    }}/>
                                    <div style={{
                                        backgroundColor: 'var(--color-sidebar-collapsible-section-text)',
                                        marginLeft: 'auto',
                                        marginRight: 'auto',
                                        marginTop: '3px',
                                        width: '75%',
                                        height: '2px'
                                    }}/>
                                    <div style={{
                                        backgroundColor: 'var(--color-sidebar-collapsible-section-text)',
                                        marginLeft: 'auto',
                                        marginRight: 'auto',
                                        marginTop: '3px',
                                        width: '75%',
                                        height: '2px'
                                    }}/>
                                </div>
                                <div style={{
                                    backgroundColor: 'var(--color-sidebar-text)',
                                    marginLeft: '5px',
                                    marginRight: 'auto',
                                    marginTop: '4px',
                                    width: '4px',
                                    height: '4px'
                                }}/>
                            </div>
                            <div style={{width: '77%', height: '80px', position: "absolute", left: "23%"}}>
                                <div style={{
                                    backgroundColor: 'var(--color-layout-title)',
                                    marginLeft: '5px',
                                    marginRight: 'auto',
                                    marginTop: '4px',
                                    width: '20%',
                                    height: '5px'
                                }}/>
                                <div style={{display: "flex", alignItems: "center", marginTop: '-6px', height: "100%"}}>
                                    <Card style={{
                                        marginLeft: '4%',
                                        width: '62%',
                                        height: '50%'
                                    }}>
                                        <canvas id={`theme-plot-${theme}`} style={{height: "100%", padding: "0 5px"}}/>
                                    </Card>
                                    <Card style={{
                                        marginLeft: '4%',
                                        marginRight: '4%',
                                        width: '25%',
                                        height: '50%'
                                    }}>
                                        <div style={{
                                            backgroundColor: 'var(--color-data-players-unique)',
                                            marginLeft: '5px',
                                            marginRight: 'auto',
                                            marginTop: '4px',
                                            width: '4px',
                                            height: '4px'
                                        }}/>
                                        <div style={{
                                            backgroundColor: 'var(--color-data-players-new)',
                                            marginLeft: '5px',
                                            marginRight: 'auto',
                                            marginTop: '4px',
                                            width: '4px',
                                            height: '4px'
                                        }}/>
                                        <div style={{
                                            backgroundColor: 'var(--color-data-play-sessions)',
                                            marginLeft: '5px',
                                            marginRight: 'auto',
                                            marginTop: '4px',
                                            width: '4px',
                                            height: '4px'
                                        }}/>
                                        <div style={{
                                            backgroundColor: 'var(--color-data-performance-tps)',
                                            marginLeft: '5px',
                                            marginRight: 'auto',
                                            marginTop: '4px',
                                            width: '4px',
                                            height: '4px'
                                        }}/>
                                    </Card>
                                </div>
                            </div>
                        </Card>
                    </ThemeStorageContextProvider>
                </ThemeContextProvider>
                {selected && <FontAwesomeIcon style={{position: 'relative', bottom: '5rem', left: "0"}}
                                              className={'selected-check'} icon={faCheck}/>}
            </button>
            {canEdit &&
                <OutlineButton style={{
                    padding: "0.2rem 0.5em",
                    position: 'relative',
                    left: "75%",
                    bottom: selected ? "4.9rem" : "3.4rem"
                }} onClick={onClickEdit}>
                    <FontAwesomeIcon icon={faPencilAlt}/>
                </OutlineButton>}
        </Col>
    )
};

export default ThemeOption