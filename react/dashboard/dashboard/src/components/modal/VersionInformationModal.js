import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {Modal} from "react-bootstrap-v5";
import {faCheckCircle, faDownload} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";

// TODO translate

const UpdateAvailableModal = ({open, toggle, versionInfo}) => {
    const {t} = useTranslation();
    return (
        <Modal id="versionModal" aria-labelledby="versionModalLabel" show={open} onHide={toggle}>
            <Modal.Header>
                <Modal.Title id="versionModalLabel">
                    <Fa icon={faDownload}/> {t('html.modal.version.title')} {versionInfo.newVersion} {t('html.modal.version.available')}
                </Modal.Title>
                <button aria-label="Close" className="btn-close" type="button" onClick={toggle}/>
            </Modal.Header>
            <Modal.Body>
                <p>You have version {versionInfo.currentVersion}.</p>
                <p>New
                    release: {versionInfo.newVersion}{versionInfo.isRelease ? '' : " (" + t('html.modal.version.dev') + ")"}</p>
                <a className="btn col-plan" href={versionInfo.changelogUrl} rel="noopener noreferrer" target="_blank">
                    {t('html.modal.version.changelog')}
                </a>
                <a className="btn col-plan" href={versionInfo.downloadUrl} rel="noopener noreferrer" target="_blank">
                    {t('html.modal.version.download')} Plan-{versionInfo.newVersion}.jar
                </a>
            </Modal.Body>
            <Modal.Footer>
                <button className="btn bg-plan" onClick={toggle}>OK</button>
            </Modal.Footer>
        </Modal>
    )
}

const NewestVersionModal = ({open, toggle, versionInfo}) => {
    return (
        <Modal id="versionModal" aria-labelledby="versionModalLabel" show={open} onHide={toggle}>
            <Modal.Header>
                <Modal.Title id="versionModalLabel">
                    <Fa icon={faCheckCircle}/> You have version {versionInfo.currentVersion}.
                </Modal.Title>
                <button aria-label="Close" className="btn-close" type="button" onClick={toggle}/>
            </Modal.Header>
            <Modal.Body>
                You're using the latest version {versionInfo.currentVersion}. (No updates available)
            </Modal.Body>
            <Modal.Footer>
                <button className="btn bg-plan" onClick={toggle}>OK</button>
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