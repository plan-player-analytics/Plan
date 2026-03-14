import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon, FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCalendar, faHandPointer} from "@fortawesome/free-regular-svg-icons";
import SessionAccordion from "../../accordion/SessionAccordion";
import React from "react";
import {faInfoCircle} from "@fortawesome/free-solid-svg-icons";
import {useGenericFilter} from "../../../dataHooks/genericFilterContextHook.tsx";
import {TitleWithDates} from "../../text/TitleWithDates.tsx";

const RecentSessionsCard = ({id, sessions, isPlayer, isNetwork}) => {
    const {after, before} = useGenericFilter();
    const {t} = useTranslation();

    return (
        <Card id={id || 'session-list'}>
            <Card.Header>
                <h6 className="col-text" style={{width: '100%'}}>
                    <Fa icon={faCalendar} className="col-sessions me-1"/>
                    <TitleWithDates label={'html.label.sessions'}
                                    after={after} before={before}
                                    fallback={'html.label.recentSessions'}/>
                    <span className="float-end">
                    <Fa icon={faHandPointer}/> <small>{t('html.text.clickToExpand')}</small>
                </span>
                </h6>
            </Card.Header>
            {(Boolean(after) && Boolean(before)) && sessions?.length === 10000 &&
                <p className={"alert alert-info mb-0"}>
                    <FontAwesomeIcon icon={faInfoCircle}/> {t('html.description.moreDataThanFitInResponse')}
                </p>}
            <SessionAccordion sessions={sessions} isPlayer={isPlayer} isNetwork={isNetwork}/>
        </Card>
    )
}

export default RecentSessionsCard;