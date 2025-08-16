import React from 'react';
import {useTranslation} from "react-i18next";
import {useMetadata} from "../../hooks/metadataHook";
import {Modal} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faHandPointRight} from "@fortawesome/free-regular-svg-icons";

const FinalizeRegistrationModal = ({show, toggle, registerCode}) => {
    const {t} = useTranslation();
    const {mainCommand} = useMetadata();

    return (
        <Modal id={"finalizeModal"}
               aria-labelledby={"finalizeModalLable"}
               show={show}
               onHide={toggle}
        >
            <Modal.Header>
                <Modal.Title id={"finalizeModalLabel"}>
                    <Fa icon={faHandPointRight}/> {t('html.register.completion')}
                </Modal.Title>
                <button aria-label="Close" className="btn-close" onClick={toggle}/>
            </Modal.Header>
            <Modal.Body>
                <p>{t('html.register.completion1')} {t('html.register.completion2')}</p>
                <p>{t('html.register.completion3')}</p>
                <p><code>/{mainCommand} register --code {registerCode}</code></p>
                <p>{t('html.register.completion4')}</p>
                <p><code>{mainCommand} register superuser --code {registerCode}</code></p>
            </Modal.Body>
        </Modal>
    )
};

export default FinalizeRegistrationModal