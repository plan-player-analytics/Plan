import React from 'react';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faExclamationTriangle} from "@fortawesome/free-solid-svg-icons";
import {useMetadata} from "../../../hooks/metadataHook";

const GroupPermissionHelp = () => {
    const {mainCommand} = useMetadata();
    return (
        <div className={"group-help"}>
            <p>This view allows you to modify web group permissions.</p>
            <p>User's web group is determined during <code>/{mainCommand} register</code> by
                checking if Player has <code>{"plan.webgroup.{group_name}"}</code> permission.</p>
            <p>You can use <code>/{mainCommand} setgroup {"{username} {group_name}"}</code> to change permission group
                after
                registering.</p>
            <p><FontAwesomeIcon icon={faExclamationTriangle}/> If you ever accidentally delete all groups
                with <i>manage.groups</i> permission just <code>/{mainCommand} reload</code>.</p>
            <hr/>
            <h3>Permission inheritance</h3>
            <p>Permissions follow inheritance model, where higher level permission grants all lower ones,
                eg. <i>page.network</i> also gives <i>page.network.overview</i>, etc.</p>
            <hr/>
            <h3>Access vs Page -permissions</h3>
            <p>You need to assign both access and page permissions for users.</p>
            <ul>
                <li>
                    <i>access</i> permissions allow user make the request to specific address,
                    eg. <i>access.network</i> allows request to /network.
                </li>
                <li>
                    <i>page</i> permissions determine what parts of the page are visible.
                    These permissions also limit requests to the related data endpoints.
                </li>
                <li>
                    <i>access</i> permissions are not required for data: <i>page.network.overview.numbers</i> allows
                    request to /v1/network/overview even without <i>access.network</i>.
                </li>
            </ul>
            <hr/>
            <h3>Saving changes</h3>
            <p>When you add a group or delete a group that action is <b>saved immediately after confirm</b> (no undo).
            </p>
            <p>When you modify permissions those changes need to be saved by pressing the Save-button</p>
            <hr/>
            <p>Documentation can be found from <a rel="noopener noreferrer" target="_blank"
                                                  href={"https://github.com/plan-player-analytics/Plan/wiki/Web-permissions"}>https://github.com/plan-player-analytics/Plan/wiki/Web-permissions</a>
            </p>
        </div>
    )
};

export default GroupPermissionHelp