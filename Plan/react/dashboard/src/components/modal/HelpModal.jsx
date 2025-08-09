import {Modal} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React, {useCallback} from "react";
import {useNavigation} from "../../hooks/navigationHook";
import {useTranslation} from "react-i18next";
import ActivityIndexHelp from "./help/ActivityIndexHelp";
import {faQuestionCircle} from "@fortawesome/free-regular-svg-icons";
import NewPlayerRetentionHelp from "./help/NewPlayerRetentionHelp";
import PlayerRetentionGraphHelp from "./help/PlayerRetentionGraphHelp";
import GroupPermissionHelp from "./help/GroupPermissionHelp";
import ActionButton from "../input/ActionButton.jsx";

const HelpModal = () => {
    const {t} = useTranslation();
    const {helpModalTopic, setHelpModalTopic} = useNavigation();
    const toggle = useCallback(() => setHelpModalTopic(undefined), [setHelpModalTopic]);

    const helpTopics = {
        "activity-index": {
            title: t('html.label.activityIndex'),
            body: <ActivityIndexHelp/>
        },
        "new-player-retention": {
            title: t('html.label.newPlayerRetention'),
            body: <NewPlayerRetentionHelp/>
        },
        "player-retention-graph": {
            title: t('html.label.playerRetention'),
            body: <PlayerRetentionGraphHelp/>
        },
        "group-permissions": {
            title: t('html.label.managePage.groupHeader'),
            body: <GroupPermissionHelp/>
        }
    }

    const helpTopic = helpTopics[helpModalTopic];
    return (
        <Modal id="versionModal" aria-labelledby="versionModalLabel" show={Boolean(helpTopic)} onHide={toggle}
               size="lg">
            <Modal.Header>
                <Modal.Title id="versionModalLabel">
                    <Fa icon={faQuestionCircle}/> {helpTopic?.title}
                </Modal.Title>
                <button aria-label="Close" className="btn-close" type="button" onClick={toggle}/>
            </Modal.Header>
            <Modal.Body>
                {helpTopic?.body}
            </Modal.Body>
            <Modal.Footer>
                <ActionButton onClick={toggle}>OK</ActionButton>
            </Modal.Footer>
        </Modal>
    );
}

export default HelpModal;