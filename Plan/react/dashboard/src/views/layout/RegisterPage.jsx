import React, {useCallback, useEffect, useState} from 'react';

import logo from '../../Flaticon_circle.png'
import {Alert, Card, Col, Row} from "react-bootstrap";
import {Link, useNavigate} from "react-router-dom";
import {useTranslation} from "react-i18next";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faPalette} from "@fortawesome/free-solid-svg-icons";
import {useTheme} from "../../hooks/themeHook";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import {useAuth} from "../../hooks/authenticationHook";
import FinalizeRegistrationModal from "../../components/modal/FinalizeRegistrationModal";
import {fetchRegisterCheck, postRegister} from "../../service/authenticationService";
import {useMetadata} from "../../hooks/metadataHook";
import ActionButton from "../../components/input/ActionButton.jsx";

const Logo = () => {
    return (
        <Col md={12} className='mt-5 text-center'>
            <img alt="logo" className="w-15" src={logo}/>
        </Col>
    )
};

const RegisterCard = ({children}) => {
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

const RegisterForm = ({register}) => {
    const {t} = useTranslation();

    const {registrationDisabled} = useMetadata();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const onRegister = useCallback(event => {
        event.preventDefault();
        register(username, password).then(() => setPassword(''));
    }, [username, password, setPassword, register]);

    if (registrationDisabled) {
        return (
            <p>{t('html.register.disabled')}</p>
        )
    }

    return (
        <form className="user">
            <div className="mb-3">
                <input autoComplete="username" className="form-control form-control-user"
                       id="inputUser"
                       placeholder={t('html.login.username')} type="text"
                       value={username} onChange={event => setUsername(event.target.value)}/>
                <div className={"form-text"}>{t('html.register.usernameTip')}</div>
            </div>
            <div className="mb-3">
                <input autoComplete="current-password" className="form-control form-control-user"
                       id="inputPassword" placeholder={t('html.login.password')} type="password"
                       value={password} onChange={event => setPassword(event.target.value)}/>
                <div className={"form-text"}>{t('html.register.passwordTip')}</div>
            </div>
            <ActionButton className="btn-user w-100" id="register-button" onClick={onRegister}>
                {t('html.register.register')}
            </ActionButton>
        </form>
    );
}

const ColorChooserButton = () => {
    const {t} = useTranslation();
    const {toggleColorChooser} = useTheme();

    return (
        <div className='text-center'>
            <button className="btn col-theme" onClick={toggleColorChooser}
                    title={t('html.label.themeSelect')}>
                <Fa icon={faPalette}/>
            </button>
        </div>
    )
}

const LoginLink = () => {
    const {t} = useTranslation();

    return (
        <div className='text-center'>
            <Link to='/login' className='col-theme small'>{t('html.register.login')}</Link>
        </div>
    )
}

const RegisterPage = () => {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const {authLoaded, authRequired, loggedIn} = useAuth();

    const [finalizeRegistrationModalOpen, setFinalizeRegistrationModalOpen] = useState(false);

    const [registerCode, setRegisterCode] = useState(undefined);
    const [failMessage, setFailMessage] = useState('');

    const toggleRegistrationModal = useCallback(() => setFinalizeRegistrationModalOpen(!finalizeRegistrationModalOpen),
        [setFinalizeRegistrationModalOpen, finalizeRegistrationModalOpen])

    useEffect(() => {
        document.body.classList.add("bg-theme", "plan-bg-gradient");

        return () => {
            document.body.classList.remove("bg-theme", "plan-bg-gradient");
        }
    }, []);

    const checkRegistration = async (code) => {
        if (!code) {
            setFinalizeRegistrationModalOpen(false);
            return setFailMessage("Register code was not received.");
        }
        if (!finalizeRegistrationModalOpen) {
            setFinalizeRegistrationModalOpen(true);
        }

        const {data, error} = await fetchRegisterCheck(code);
        if (error) {
            setFailMessage(t('html.register.error.checkFailed') + error)
        } else if (data?.success) {
            navigate('/login?registerSuccess=true');
        } else {
            setTimeout(() => {
                checkRegistration(code);
            }, 5000);
        }
    }

    const register = async (username, password) => {
        if (!username || username.length < 1) {
            return setFailMessage(t('html.register.error.noUsername'));
        }
        if (username.length > 50) {
            return setFailMessage(t('html.register.error.usernameLength') + username.length);
        }
        if (!password || password.length < 1) {
            return setFailMessage(t('html.register.error.noPassword'));
        }

        const {data, error} = await postRegister(username, password);

        if (error) {
            setFailMessage(t('html.register.error.failed') + (error?.data.error ? error.data.error : error.message));
        } else if (data?.code) {
            setRegisterCode(data.code);
            setFinalizeRegistrationModalOpen(true);
            setTimeout(() => {
                checkRegistration(data.code);
            }, 10000);
        } else {
            setFailMessage(t('html.register.error.failed') + data ? data.error : t('generic.noData'));
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
            <style>{'#wrapper{background-image:none;}'}</style>
            <main className="container">
                <Logo/>
                <RegisterCard>
                    <div className="text-center">
                        <h1 className="h4 col-text mb-4">{t('html.register.createNewUser')}</h1>
                    </div>
                    {failMessage && <Alert className='alert-danger'>{failMessage}</Alert>}
                    <RegisterForm register={register}/>
                    <hr className="col-secondary"/>
                    <LoginLink/>
                    <ColorChooserButton/>
                </RegisterCard>
            </main>
            <aside>
                <ColorSelectorModal/>
                <FinalizeRegistrationModal
                    show={finalizeRegistrationModalOpen}
                    toggle={toggleRegistrationModal}
                    registerCode={registerCode}
                />
            </aside>
        </>
    )
};

export default RegisterPage