import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {Modal} from "react-bootstrap";
import {faCheckCircle, faDownload} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import ActionButton from "../input/button/ActionButton.jsx";
import ModalCloseButton from "../input/button/ModalCloseButton.jsx";
import ExternalLink from "../input/button/ExternalLink.jsx";

const UpdateAvailableModal = ({open, toggle, versionInfo}) => {
    const {t} = useTranslation();
    return (
        <Modal id="versionModal" aria-labelledby="versionModalLabel" show={open} onHide={toggle}>
            <Modal.Header>
                <Modal.Title id="versionModalLabel">
                    <Fa icon={faDownload}/> {t('html.modal.version.title')} {versionInfo.newVersion} {t('html.modal.version.available')}
                </Modal.Title>
                <ModalCloseButton onClick={toggle}/>
            </Modal.Header>
            <Modal.Body>
                <p>You have version {versionInfo.currentVersion}.</p>
                <p>New
                    release: {versionInfo.newVersion}{versionInfo.isRelease ? '' : " (" + t('html.modal.version.dev') + ")"}</p>
                <ExternalLink href={versionInfo.changelogUrl}>
                    {t('html.modal.version.changelog')}
                </ExternalLink>
                <ExternalLink href={versionInfo.downloadUrl}>
                    {t('html.modal.version.download')} Plan-{versionInfo.newVersion}.jar
                </ExternalLink>
            </Modal.Body>
            <Modal.Footer>
                <ActionButton onClick={toggle}>OK</ActionButton>
            </Modal.Footer>
        </Modal>
    )
}

/*eslint no-template-curly-in-string: "off"*/
const NewestVersionModal = ({open, toggle, versionInfo}) => {
    const {t} = useTranslation();
    return (
        <Modal id="versionModal" aria-labelledby="versionModalLabel" show={open} onHide={toggle}>
            <Modal.Header>
                <Modal.Title id="versionModalLabel">
                    <Fa icon={faCheckCircle}/> {t('html.version.current').replace('${0}', versionInfo.currentVersion)}.
                </Modal.Title>
                <ModalCloseButton onClick={toggle}/>
            </Modal.Header>
            <Modal.Body>
                {t('plugin.version.isLatest')}
            </Modal.Body>
            <Modal.Footer>
                <ActionButton onClick={toggle}>OK</ActionButton>
            </Modal.Footer>
        </Modal>
    );
}

const VersionInformationModal = ({open, toggle, versionInfo}) => {
    return versionInfo.updateAvailable
        ? <UpdateAvailableModal open={open} toggle={toggle} versionInfo={versionInfo}/>
        : <NewestVersionModal open={open} toggle={toggle} versionInfo={versionInfo}/>
}

export default VersionInformationModal;