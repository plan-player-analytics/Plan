import React from 'react';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faExclamationTriangle} from "@fortawesome/free-solid-svg-icons";
import {useMetadata} from "../../../hooks/metadataHook";
import {Trans} from "react-i18next";

/* eslint-disable jsx-a11y/anchor-has-content */
const GroupPermissionHelp = () => {
    const {mainCommand} = useMetadata();
    return (
        <div className={"group-help"}>
            <p><Trans i18nKey="html.label.help.manage.groups.line-1"/></p>
            <p><Trans i18nKey="html.label.help.manage.groups.line-2"
                      shouldUnescape={true}
                      values={{
                          command: `/${mainCommand} register`,
                          permission: "plan.webgroup.{group_name}"
                      }}
                      components={{1: <code/>, 2: <code/>}}
            /></p>
            <p><Trans i18nKey="html.label.help.manage.groups.line-3"
                      shouldUnescape={true}
                      values={{command: `/${mainCommand} setgroup {username} {group_name}`}}
                      components={{1: <code/>}}
            /></p>
            <p><Trans i18nKey="html.label.help.manage.groups.line-4"
                      shouldUnescape={true}
                      values={{
                          icon: '',
                          permission: "manage.groups",
                          command: `/${mainCommand} reload`,
                      }}
                      components={{1: <FontAwesomeIcon icon={faExclamationTriangle}/>, 2: <i/>, 3: <code/>}}
            /></p>
            <hr/>
            <h3><Trans i18nKey="html.label.help.manage.groups.line-5"/></h3>
            <p><Trans i18nKey="html.label.help.manage.groups.line-6"
                      values={{permission1: "page.network", permission2: "page.network.overview"}}
                      components={{1: <i/>, 2: <i/>}}
            /></p>
            <hr/>
            <h3><Trans i18nKey="html.label.help.manage.groups.line-7"/></h3>
            <p><Trans i18nKey="html.label.help.manage.groups.line-8"/></p>
            <ul>
                <li>
                    <Trans i18nKey="html.label.help.manage.groups.line-9"
                           values={{permission1: "access", permission2: "access.network"}}
                           components={{1: <i/>, 2: <i/>}}
                    />
                </li>
                <li><Trans i18nKey="html.label.help.manage.groups.line-10"
                           values={{permission: "page"}}
                           components={{1: <i/>}}
                /></li>
                <li><Trans i18nKey="html.label.help.manage.groups.line-11"
                           values={{
                               permission1: "access",
                               permission2: "page.network.overview.numbers",
                               permission3: "access.network"
                           }}
                           components={{1: <i/>, 2: <i/>, 3: <i/>}}
                /></li>
            </ul>
            <hr/>
            <h3><Trans i18nKey="html.label.help.manage.groups.line-12"/></h3>
            <p><Trans i18nKey="html.label.help.manage.groups.line-13"/></p>
            <p><Trans i18nKey="html.label.help.manage.groups.line-14"/></p>
            <hr/>
            <p><Trans i18nKey="html.label.help.manage.groups.line-15"
                      shouldUnescape={true}
                      values={{link: "https://github.com/plan-player-analytics/Plan/wiki/Web-permissions"}}
                      components={{
                          1: <a rel="noopener noreferrer" target="_blank"
                                href={"https://github.com/plan-player-analytics/Plan/wiki/Web-permissions"}/>
                      }}
            /></p>
        </div>
    )
};
/* eslint-enable jsx-a11y/anchor-has-content */

export default GroupPermissionHelp