import {Modal} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React, {useCallback} from "react";
import {useNavigation} from "../../hooks/navigationHook";
import {useTranslation} from "react-i18next";
import ActivityIndexHelp from "./help/ActivityIndexHelp";
import {faQuestionCircle} from "@fortawesome/free-regular-svg-icons";
import NewPlayerRetentionHelp from "./help/NewPlayerRetentionHelp";
import PlayerRetentionGraphHelp from "./help/PlayerRetentionGraphHelp";

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
                <button className="btn bg-theme" onClick={toggle}>OK</button>
            </Modal.Footer>
        </Modal>
    );
}

export default HelpModal;