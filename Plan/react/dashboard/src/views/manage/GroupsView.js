import ErrorView from "../ErrorView";
import LoadIn from "../../components/animation/LoadIn";
import {Card, Col, Row} from "react-bootstrap";
import React from "react";
import CardHeader from "../../components/cards/CardHeader";
import {faFloppyDisk, faTrash, faUserGroup, faUsersGear} from "@fortawesome/free-solid-svg-icons";
import Accordion from "../../components/accordion/Accordion";
import {FontAwesomeIcon as Fa, FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {GroupEditContextProvider, useGroupEditContext} from "../../hooks/context/groupEditContextHook";
import {fetchGroups} from "../../service/manageService";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {CardLoader} from "../../components/navigation/Loader";

const GroupsHeader = ({groupName}) => {
    return (
        <tr>
            <td><FontAwesomeIcon icon={faUserGroup}/> {groupName}</td>
        </tr>
    )
}

const PermissionDropdown = ({permission, checked, indeterminate, togglePermission, children, childNodes, root}) => {
    if (childNodes.length) {
        if (permission === undefined) {
            return <>{children}</>;
        } else {
            return (
                <details open style={root ? {marginLeft: "0.5rem"} : {marginLeft: "2.1rem"}}>
                    <summary>
                        <input className={"form-check-input"} type={"checkbox"} value={indeterminate ? "" : checked}
                               checked={checked}
                            // ref={input => {
                            //     if (input) input.indeterminate = indeterminate
                            // }}
                               onChange={() => togglePermission(permission)}
                        /> {permission}
                    </summary>

                    {children}
                </details>
            )
        }
    } else {
        return (
            <li style={root ? {marginLeft: "1.4rem"} : {marginLeft: "3rem"}}>
                <input className={"form-check-input"} type={"checkbox"} value={indeterminate ? "" : checked}
                       checked={checked}
                    // ref={input => {
                    //     if (input) input.indeterminate = indeterminate
                    // }}
                       onChange={() => togglePermission(permission)}
                /> {permission}
            </li>
        )
    }
}

const PermissionTree = ({nodes, isIndeterminate, isChecked, togglePermission}) => {
    if (!nodes.length) {
        return <></>;
    }
    return (
        <>
            {nodes.map(node => <>
                <PermissionDropdown permission={node.permission} root={node.parentIndex === -1}
                                    indeterminate={isIndeterminate(node.permission)}
                                    checked={isChecked(node.permission)}
                                    togglePermission={togglePermission}
                                    childNodes={node.children}>
                    <PermissionTree nodes={node.children}
                                    togglePermission={togglePermission}
                                    isChecked={isChecked}
                                    isIndeterminate={isIndeterminate}/>
                </PermissionDropdown>
            </>)}
        </>
    )
}

const GroupsBody = () => {
    const {
        permissionTree,
        togglePermission,
        isChecked,
        isIndeterminate
    } = useGroupEditContext();

    return (
        <Row>
            <Col>
                <h3>Permissions</h3>
                <PermissionTree nodes={[permissionTree]}
                                childNodes={permissionTree.children}
                                togglePermission={togglePermission}
                                isChecked={isChecked}
                                isIndeterminate={isIndeterminate}/>
            </Col>
        </Row>
    );
}

const GroupsCard = ({groups}) => {
    const headers = [];
    const slices = groups.map(group => {
        return {
            body: <GroupEditContextProvider groupName={group.name}><GroupsBody/></GroupEditContextProvider>,
            header: <GroupsHeader groupName={group.name}/>,
            color: 'light-green',
            outline: false
        }
    })
    return (
        <Card>
            <CardHeader icon={faUsersGear} color="theme" label={"Manage Group Permissions"}>
                <button className={"float-end btn bg-theme"} style={{margin: "-0.5rem"}} onClick={() => {
                }}>
                    <Fa icon={faFloppyDisk}/> Save
                </button>
                <button className={"float-end btn"} style={{margin: "-0.5rem", marginRight: "0.5rem"}} onClick={() => {
                }}>
                    <Fa icon={faTrash}/> Discard Changes
                </button>
            </CardHeader>
            <Accordion headers={headers} slices={slices}></Accordion>
        </Card>
    )
}

const GroupsView = () => {
    const {data, loadingError} = useDataRequest(fetchGroups, [null]);

    if (loadingError) return <ErrorView error={loadingError}/>
    if (!data) return <CardLoader/>

    return (
        <LoadIn>
            <Row>
                <Col md={12}>
                    <GroupsCard groups={data.groups}/>
                </Col>
            </Row>
        </LoadIn>
    )
};

export default GroupsView