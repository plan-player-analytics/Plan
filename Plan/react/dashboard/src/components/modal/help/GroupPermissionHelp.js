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
                checking if Player has <code>{"plan.webgroup.{group_name}"}</code> permission. First one that is found
                is used.</p>
            <p>You can use <code>/{mainCommand} setgroup {"{username} {group_name}"}</code> to change permission group
                after
                registering (Needs <code>plan.setgroup.other</code> permission).</p>
            <p><FontAwesomeIcon icon={faExclamationTriangle}/> If you ever accidentally delete all groups
                with <i>manage.groups</i> permission just <code>/{mainCommand} reload</code>.</p>
            <h3>Permission inheritance</h3>
            <p>Permissions follow inheritance model, where higher level permission grants all lower ones,
                eg. <i>page.network</i> also gives <i>page.network.overview</i>, etc.</p>
            <ul>
                <li>
                    If given <i>page.network</i> the user can see everything on the network page.
                </li>
                <li>
                    If given <i>page.network.overview.graphs.online</i>, the user can
                    see just the Players Online Graph on Network Overview,
                    even if they don't have <i>page.network.overview</i> permission.
                </li>
            </ul>
            <h3>Access vs Page -permissions</h3>
            <p>You need to assign both access and page permissions for users.</p>
            <ul>
                <li>
                    <i>access</i> permissions allow user make the request to specific address,
                    eg. <i>access.network</i> allows request to /network.
                </li>
                <li>
                    <i>page</i> permissions determine what parts of the page are visible,
                    eg. <i>page.network.overview</i> allows viewing Network Overview.
                    These permissions also limit requests to the related data,
                    eg. <i>page.network.overview.numbers</i> allows request to /v1/network/overview.
                </li>
                <li>
                    <i>access</i> permissions are not required for data: <i>page.network.overview.numbers</i> allows
                    request to /v1/network/overview even without <i>access.network</i>.
                </li>
            </ul>
            <h3>Adding or deleting groups</h3>
            <p>Group names can be 100 characters long, and the Add group -form forces them to correct format. Only use
                characters supported by your permission system so that it can
                check <code>{"plan.webgroup.{group_name}"}</code> permission.</p>
            <p>When you delete a group you need to choose where users of that group are moved. There is no undo button,
                so choose carefully.</p>
            <h3>Saving changes</h3>
            <p>Group permissions are stored in the database.</p>
            <p>When you add a group or delete a group that action is saved right away.</p>
            <p>When you modify permissions those changes need to be saved by pressing the Save-button</p>
        </div>
    )
};

export default GroupPermissionHelp