import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCalendar, faHandPointer} from "@fortawesome/free-regular-svg-icons";
import Scrollable from "../../Scrollable";
import SessionAccordion from "../../accordion/SessionAccordion";
import React from "react";

const RecentSessionsCard = ({id, sessions, isPlayer, isNetwork}) => {
    const {t} = useTranslation();
    return (
        <Card id={'session-list'}>
            <Card.Header>
                <h6 className="col-text" style={{width: '100%'}}>
                    <Fa icon={faCalendar} className="col-sessions"/> {t('html.label.recentSessions')}
                    <span className="float-end">
                    <Fa icon={faHandPointer}/> <small>{t('html.text.clickToExpand')}</small>
                </span>
                </h6>
            </Card.Header>
            <Scrollable>
                <SessionAccordion sessions={sessions} isPlayer={isPlayer} isNetwork={isNetwork}/>
            </Scrollable>
        </Card>
    )
}

export default RecentSessionsCard;