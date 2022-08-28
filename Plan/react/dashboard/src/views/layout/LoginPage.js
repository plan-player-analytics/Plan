import React, {useCallback, useEffect, useState} from 'react';

import logo from '../../Flaticon_circle.png'
import {Alert, Card, Col, Row} from "react-bootstrap-v5";
import {Link, useNavigate} from "react-router-dom";
import {useTranslation} from "react-i18next";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faPalette} from "@fortawesome/free-solid-svg-icons";
import {useTheme} from "../../hooks/themeHook";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import drawSine from "../../util/loginSineRenderer";
import {fetchLogin} from "../../service/authenticationService";
import ForgotPasswordModal from "../../components/modal/ForgotPasswordModal";
import {useAuth} from "../../hooks/authenticationHook";
import {baseAddress} from "../../service/backendConfiguration";

const Logo = () => {
    return (
        <Col md={12} className='mt-5 text-center'>
            <img alt="logo" className="w-15" src={logo}/>
        </Col>
    )
};

const LoginCard = ({children}) => {
    return (
        <Row className="justify-content-center container-fluid">
            <Col xl={6} lg={7} md={9}>
                <Card className='o-hidden border-0 shadow-lg my-5'>
                    <Card.Body className='p-0'>
                        <Row>
                            <Col lg={12}>
                                <div className='p-5'>
                                    {children}
                                </div>
                            </Col>
                        </Row>
                    </Card.Body>
                </Card>
            </Col>
        </Row>
    )
}

const LoginForm = ({login}) => {
    const {t} = useTranslation();

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const onLogin = useCallback(event => {
        event.preventDefault();
        login(username, password).then(() => setPassword(''));
    }, [username, password, setPassword, login]);

    return (
        <form className="user">
            <div className="mb-3">
                <input autoComplete="username" className="form-control form-control-user"
                       id="inputUser"
                       placeholder={t('html.login.username')} type="text"
                       value={username} onChange={event => setUsername(event.target.value)}/>
            </div>
            <div className="mb-3">
                <input autoComplete="current-password" className="form-control form-control-user"
                       id="inputPassword" placeholder={t('html.login.password')} type="password"
                       value={password} onChange={event => setPassword(event.target.value)}/>
            </div>
            <button className="btn bg-plan btn-user w-100" id="login-button" onClick={onLogin}>
                {t('html.login.login')}
            </button>
        </form>
    );
}

const ColorChooserButton = () => {
    const {t} = useTranslation();
    const {toggleColorChooser} = useTheme();

    return (
        <div className='text-center'>
            <button className="btn col-plan" onClick={toggleColorChooser}
                    title={t('html.label.themeSelect')}>
                <Fa icon={faPalette}/>
            </button>
        </div>
    )
}
const ForgotPasswordButton = ({onClick}) => {
    const {t} = useTranslation();

    return (
        <div className='text-center'>
            <button className='col-plan small' onClick={onClick}>{t('html.login.forgotPassword')}</button>
        </div>
    )
}

const CreateAccountLink = () => {
    const {t} = useTranslation();

    return (
        <div className='text-center'>
            <Link to='/register' className='col-plan small'>{t('html.login.register')}</Link>
        </div>
    )
}

const Decoration = () => {
    useEffect(() => {
        drawSine('decoration');
    })

    return (
        <Row className='justify-content-center'>
            <canvas className="col-xl-3 col-lg-3 col-md-5" id="decoration" style={{height: "100px"}}/>
        </Row>
    );
}

const LoginPage = () => {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const {authLoaded, authRequired, loggedIn, updateLoginDetails} = useAuth();

    const [forgotPasswordModalOpen, setForgotPasswordModalOpen] = useState(false);

    const [failMessage, setFailMessage] = useState('');
    const [redirectTo, setRedirectTo] = useState(undefined);

    const togglePasswordModal = useCallback(() => setForgotPasswordModalOpen(!forgotPasswordModalOpen),
        [setForgotPasswordModalOpen, forgotPasswordModalOpen])

    useEffect(() => {
        document.body.classList.add("bg-plan", "plan-bg-gradient");

        const urlParams = new URLSearchParams(window.location.search);
        const cameFrom = urlParams.get('from');
        if (cameFrom) setRedirectTo(cameFrom);

        return () => {
            document.body.classList.remove("bg-plan", "plan-bg-gradient");
        }
    }, [setRedirectTo])

    const login = async (username, password) => {
        if (!username || username.length < 1) {
            return setFailMessage(t('html.register.error.noUsername'));
        }
        if (username.length > 50) {
            return setFailMessage(t('html.register.error.usernameLength') + username.length);
        }
        if (!password || password.length < 1) {
            return setFailMessage(t('html.register.error.noPassword'));
        }

        const {data, error} = await fetchLogin(username, password);

        if (error) {
            if (error.message === 'Request failed with status code 403') {
                // Too many logins, reload browser to show forbidden page
                window.location.reload();
            } else {
                setFailMessage(t('html.login.failed') + (error.data && error.data.error ? error.data.error : error.message));
            }
        } else if (data && data.success) {
            await updateLoginDetails();
            if (redirectTo && !redirectTo.startsWith('http') && !redirectTo.startsWith('file') && !redirectTo.startsWith('javascript')) {
                navigate(baseAddress + redirectTo.substring(redirectTo.indexOf('/')) + (window.location.hash ? window.location.hash : ''));
            } else {
                navigate(baseAddress + '/');
            }
        } else {
            setFailMessage(t('html.login.failed') + data ? data.error : t('generic.noData'));
        }
    }

    if (!authLoaded) {
        return <></>
    }

    if (!authRequired || loggedIn) {
        navigate('../');
    }

    return (
        <>
            <main className="container">
                <Logo/>
                <LoginCard>
                    {failMessage && <Alert className='alert-danger'>{failMessage}</Alert>}
                    <LoginForm login={login}/>
                    <hr className="bg-secondary"/>
                    <ForgotPasswordButton onClick={togglePasswordModal}/>
                    <CreateAccountLink/>
                    <ColorChooserButton/>
                </LoginCard>
                <Decoration/>
            </main>
            <aside>
                <ColorSelectorModal/>
                <ForgotPasswordModal show={forgotPasswordModalOpen} toggle={togglePasswordModal}/>
            </aside>
        </>
    )
};

export default LoginPage