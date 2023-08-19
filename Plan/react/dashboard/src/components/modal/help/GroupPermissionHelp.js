import React from 'react';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faExclamationTriangle} from "@fortawesome/free-solid-svg-icons";
import {useMetadata} from "../../../hooks/metadataHook";
import {Trans} from "react-i18next";

const GroupPermissionHelp = () => {
    const {mainCommand} = useMetadata();
    return (
        <div className={"group-help"}>
            <p><Trans i18nKey="html.label.help.manage.groups.line-1"/></p>
            <p><Trans i18nKey="html.label.help.manage.groups.line-2" values={{
                command: <code>/{mainCommand} register</code>,
                permission: <code>{"plan.webgroup.{group_name}"}</code>
            }}/></p>
            <p><Trans i18nKey="html.label.help.manage.groups.line-3"
                      values={{command: <code>/{mainCommand} setgroup {"{username} {group_name}"}</code>}}/></p>
            <p><Trans i18nKey="html.label.help.manage.groups.line-4" values={{
                icon: <FontAwesomeIcon icon={faExclamationTriangle}/>,
                permission: <i>manage.groups</i>,
                commands: <code>/{mainCommand} reload</code>
            }}/></p>
            <hr/>
            <h3><Trans i18nKey="html.label.help.manage.groups.line-5"/></h3>
            <p><Trans i18nKey="html.label.help.manage.groups.line-6"
                      values={{permission1: <i>page.network</i>, permission2: <i>page.network.overview</i>}}/></p>
            <hr/>
            <h3><Trans i18nKey="html.label.help.manage.groups.line-7"/></h3>
            <p><Trans i18nKey="html.label.help.manage.groups.line-8"/></p>
            <ul>
                <li>
                    <Trans i18nKey="html.label.help.manage.groups.line-9"
                           values={{permission1: <i>access</i>, permission2: <i>access.network</i>}}/>
                </li>
                <li><Trans i18nKey="html.label.help.manage.groups.line-10" values={{permission: <i>page</i>}}/></li>
                <li><Trans i18nKey="html.label.help.manage.groups.line-11" values={{
                    permission1: <i>access</i>,
                    permission2: <i>page.network.overview.numbers</i>,
                    permission3: <i>access.network</i>
                }}/></li>
            </ul>
            <hr/>
            <h3><Trans i18nKey="html.label.help.manage.groups.line-12"/></h3>
            <p><Trans i18nKey="html.label.help.manage.groups.line-13"/></p>
            <p><Trans i18nKey="html.label.help.manage.groups.line-14"/></p>
            <hr/>
            <p><Trans i18nKey="html.label.help.manage.groups.line-15" values={{
                link: <a rel="noopener noreferrer" target="_blank"
                         href={"https://github.com/plan-player-analytics/Plan/wiki/Web-permissions"}>https://github.com/plan-player-analytics/Plan/wiki/Web-permissions</a>
            }}/></p>
        </div>
    )
};

export default GroupPermissionHelp