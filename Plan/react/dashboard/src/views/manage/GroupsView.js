import ErrorView from "../ErrorView";
import LoadIn from "../../components/animation/LoadIn";
import {Card, Col, InputGroup, Row} from "react-bootstrap";
import React, {useCallback, useEffect, useState} from "react";
import CardHeader from "../../components/cards/CardHeader";
import {
    faCheck,
    faExclamationTriangle,
    faFloppyDisk,
    faPlus,
    faRotateLeft,
    faTrash,
    faUserGroup,
    faUsersGear
} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon as Fa, FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {GroupEditContextProvider, useGroupEditContext} from "../../hooks/context/groupEditContextHook";
import {addGroup, deleteGroup, fetchGroups} from "../../service/manageService";
import {CardLoader, ChartLoader} from "../../components/navigation/Loader";
import {useTranslation} from "react-i18next";
import {
    ConfigurationStorageContextProvider,
    useConfigurationStorageContext
} from "../../hooks/context/configurationStorageContextHook";
import SideNavTabs from "../../components/layout/SideNavTabs";
import Select from "../../components/input/Select";
import Scrollable from "../../components/Scrollable";
import OpaqueText from "../../components/layout/OpaqueText";
import {useAlertPopupContext} from "../../hooks/context/alertPopupContext";

const GroupsHeader = ({groupName, icon}) => {
    return (
        <span className={"float-start"}><FontAwesomeIcon icon={icon || faUserGroup}/> {groupName}</span>
    )
}

const PermissionDropdown = ({permission, checked, indeterminate, togglePermission, children, childNodes, root}) => {
    const {t} = useTranslation();

    const translationKey = "html.manage.permission.description." + permission?.split('.').join("_");
    const translated = t(translationKey);

    if (childNodes.length) {
        if (permission === undefined) {
            return <>{children}</>;
        } else {
            return (
                <details open style={root ? {marginLeft: "0.5rem"} : {marginLeft: "2.1rem"}}>
                    <summary>
                        <input className={"form-check-input"} type={"checkbox"} value={indeterminate ? "" : checked}
                               checked={checked}
                               ref={input => {
                                   if (input) input.indeterminate = indeterminate
                               }}
                               onChange={() => togglePermission(permission)}
                        /> {permission} {permission && translated !== translationKey &&
                        <OpaqueText inline>&middot; {translated}</OpaqueText>}
                        <hr style={{margin: 0}}/>
                    </summary>

                    {children}
                </details>
            )
        }
    } else {
        return (
            <li style={root ? {marginLeft: "1.4rem"} : {marginLeft: "3rem"}}>
                <input className={"form-check-input"} type={"checkbox"} value={checked}
                       checked={checked}
                       onChange={() => togglePermission(permission)}
                /> {permission} {permission && translated !== translationKey &&
                <OpaqueText inline>&middot; {translated}</OpaqueText>}
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
            {nodes.map(node => <section key={node.permission + "-1234"}>
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
            </section>)}
        </>
    )
}

const GroupsBody = ({groups, reloadGroupNames}) => {
    const {
        changed,
        groupName,
        permissionTree,
        togglePermission,
        isChecked,
        isIndeterminate
    } = useGroupEditContext();

    return (
        <Row>
            <Col>
                <div>
                    <h3 style={{display: "inline-block"}}>Permissions of {groupName}</h3>
                    <UnsavedChangesText visible={changed}/>
                    <DeleteGroupButton groupName={groupName} groups={groups} reloadGroupNames={reloadGroupNames}/>
                </div>
                <Scrollable>
                    {permissionTree?.children?.length && <PermissionTree nodes={[permissionTree]}
                                                                         childNodes={permissionTree.children}
                                                                         togglePermission={togglePermission}
                                                                         isChecked={isChecked}
                                                                         isIndeterminate={isIndeterminate}/>}
                    {!permissionTree?.children?.length && <ChartLoader/>}
                </Scrollable>
            </Col>
        </Row>
    );
}

const SaveButton = () => {
    const {dirty, requestSave} = useConfigurationStorageContext();

    return (
        <button className={"float-end btn bg-theme"} style={{margin: "-0.5rem"}}
                disabled={!dirty}
                onClick={requestSave}>
            <Fa icon={faFloppyDisk}/> Save
        </button>
    )
}

const DeleteGroupButton = ({groupName, groups, reloadGroupNames}) => {
    const [clicked, setClicked] = useState(false);
    const [moveToGroup, setMoveToGroup] = useState(0);
    const {addAlert} = useAlertPopupContext();

    if (clicked) {
        const groupOptions = groups.filter(g => g.name !== groupName).map(g => g.name);
        return (
            <Card>
                <CardHeader icon={faTrash} label={`Delete ${groupName}`}/>
                <Card.Body>
                    <InputGroup>
                        <div className={"input-group-text"}>
                            Move remaining users to group
                        </div>
                        <Select options={groupOptions}
                                selectedIndex={moveToGroup} setSelectedIndex={setMoveToGroup}/>
                    </InputGroup>

                    <p className={"mt-3"}>This will move all users of '{groupName}' to group
                        '{groupOptions[moveToGroup]}'. There is no undo!</p>

                    <button className={"btn bg-red mt-2"}
                            onClick={() => {
                                deleteGroup(groupName, groupOptions[moveToGroup]).then(({error}) => {
                                    if (error) {
                                        addAlert({
                                            timeout: 15000,
                                            color: "danger",
                                            content: <><Fa
                                                icon={faExclamationTriangle}/>{" Failed to delete group: " + error?.message}</>
                                        });
                                    } else {
                                        reloadGroupNames();
                                        setClicked(false);
                                        addAlert({
                                            timeout: 7500,
                                            color: "success",
                                            content: <><Fa icon={faCheck}/>{" Deleted group '" + groupName}'</>
                                        });
                                    }
                                })
                            }}>
                        <Fa icon={faTrash}/> Confirm & delete {groupName}
                    </button>
                    <button className={"btn bg-grey-outline mt-2"} style={{marginLeft: "0.5rem"}}
                            onClick={() => {
                                setClicked(false)
                            }}>
                        <Fa icon={faRotateLeft}/> Cancel
                    </button>
                </Card.Body>
            </Card>
        )
    }

    return (
        <button className={"float-end btn bg-grey-outline"}
                onClick={() => {
                    setClicked(true)
                }}>
            <Fa icon={faTrash}/> Delete {groupName}
        </button>
    )
}

const DiscardButton = () => {
    const {dirty, requestDiscard} = useConfigurationStorageContext();

    return (
        <>
            {dirty && <button className={"float-end btn"} style={{margin: "-0.5rem", marginRight: "0.5rem"}}
                              onClick={requestDiscard}>
                <Fa icon={faTrash}/> Discard Changes
            </button>}
        </>
    )
}

const UnsavedChangesText = ({visible}) => {
    const {dirty} = useConfigurationStorageContext();
    const show = visible !== undefined ? visible : dirty;
    if (show) {
        return (
            <p style={{display: "inline-block", marginLeft: "1rem", marginBottom: 0, opacity: 0.6}}>Unsaved changes</p>
        )
    } else {
        return <></>
    }
}

const AddGroupBody = ({groups, reloadGroupNames}) => {
    const [invalid, setInvalid] = useState(false);
    const [value, setValue] = useState(undefined);
    const {addAlert} = useAlertPopupContext();

    const onChange = (event) => {
        const newValue = event.target.value;
        setValue(newValue.toLowerCase().replace(" ", "_"));
        setInvalid(newValue.length > 100);
    }

    return (
        <Card>
            <CardHeader icon={faPlus} label={"Add group"}/>
            <Card.Body>
                <InputGroup>
                    <div className={"input-group-text"}>
                        <FontAwesomeIcon icon={faUserGroup}/>
                    </div>
                    <input type="text" className={"form-control" + (invalid ? " is-invalid" : '')}
                           placeholder={"Name of the group"}
                           value={value}
                           onChange={onChange}
                    />
                    {invalid && <div className="invalid-feedback">
                        Group name can be 100 characters maximum.
                    </div>}
                </InputGroup>
                <button className={"btn bg-plan mt-2"} disabled={invalid || !value || value.length === 0}
                        onClick={() => {
                            addGroup(value).then(({error}) => {
                                if (error) {
                                    addAlert({
                                        timeout: 15000,
                                        color: "danger",
                                        content: <><Fa
                                            icon={faExclamationTriangle}/>{" Failed to add group: " + error?.message}</>
                                    });
                                } else {
                                    addAlert({
                                        timeout: 7500,
                                        color: "success",
                                        content: <><Fa icon={faCheck}/>{" Added group '" + value}'</>
                                    });
                                    reloadGroupNames();
                                }
                            })
                        }}>
                    <Fa icon={faFloppyDisk}/> Add group
                </button>
            </Card.Body>
        </Card>
    )
}

const GroupsCard = ({groups, reloadGroupNames}) => {
    const slices = groups.map(group => {
        return {
            body: <GroupEditContextProvider groupName={group.name}><GroupsBody
                groups={groups} reloadGroupNames={reloadGroupNames}/></GroupEditContextProvider>,
            header: <GroupsHeader groupName={group.name}/>,
            color: 'light-green',
            outline: false
        }
    })

    slices.push({
        body: <AddGroupBody groups={groups} reloadGroupNames={reloadGroupNames}/>,
        header: <GroupsHeader groupName={"Add group"} icon={faPlus}/>,
        color: 'light-green',
        outline: false
    })

    return (
        <Card>
            <CardHeader icon={faUsersGear} color="theme" label={"Manage Group Permissions"}>
                <UnsavedChangesText/>
                <SaveButton/>
                <DiscardButton/>
            </CardHeader>
            <Card.Body>
                <SideNavTabs slices={slices} open></SideNavTabs>
            </Card.Body>
        </Card>
    )
}

const GroupsView = () => {
    const [updateRequested, setUpdateRequested] = useState(Date.now());
    const [data, setData] = useState(undefined);
    const [loadingError, setLoadingError] = useState(undefined);
    const loadGroupNames = useCallback(() => {
        fetchGroups().then(({data, error}) => {
            setData(data);
            setLoadingError(error);
        });
    }, [setData, setLoadingError]);
    useEffect(() => {
        loadGroupNames();
    }, [updateRequested, loadGroupNames]);

    const reloadGroupNames = useCallback(() => {
        setTimeout(() => setUpdateRequested(Date.now()), 1000);
    }, [setUpdateRequested])

    if (loadingError) return <ErrorView error={loadingError}/>;
    if (!data) return <CardLoader/>;

    return (
        <LoadIn>
            <ConfigurationStorageContextProvider>
                <Row>
                    <Col md={12}>
                        <GroupsCard groups={data.groups} reloadGroupNames={reloadGroupNames}/>
                    </Col>
                </Row>
            </ConfigurationStorageContextProvider>
        </LoadIn>
    )
};

export default GroupsView