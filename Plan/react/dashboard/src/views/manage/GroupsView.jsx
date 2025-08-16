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
import {Trans, useTranslation} from "react-i18next";
import {
    ConfigurationStorageContextProvider,
    useConfigurationStorageContext
} from "../../hooks/context/configurationStorageContextHook";
import SideNavTabs from "../../components/layout/SideNavTabs";
import Select from "../../components/input/Select";
import Scrollable from "../../components/Scrollable";
import OpaqueText from "../../components/layout/OpaqueText";
import {useAlertPopupContext} from "../../hooks/context/alertPopupContext";
import {DropdownStatusContextProvider, useDropdownStatusContext} from "../../hooks/context/dropdownStatusContextHook";
import {useNavigation} from "../../hooks/navigationHook";
import {faQuestionCircle} from "@fortawesome/free-regular-svg-icons";
import {useAuth} from "../../hooks/authenticationHook";
import Checkbox from "../../components/input/Checkbox.jsx";
import TextInput from "../../components/input/TextInput.jsx";
import UnsavedChangesText from "../../components/text/UnsavedChangesText.jsx";
import ActionButton from "../../components/input/ActionButton.jsx";
import DangerButton from "../../components/input/button/DangerButton.jsx";
import OutlineButton from "../../components/input/OutlineButton.jsx";

const GroupsHeader = ({groupName, icon}) => {
    return (
        <span className={"float-start"}><FontAwesomeIcon icon={icon || faUserGroup}/> {groupName}</span>
    )
}

const PermissionDropdown = ({permission, checked, indeterminate, togglePermission, children, childNodes, root}) => {
    const {t} = useTranslation();
    const {toggle, toggled} = useDropdownStatusContext();

    const translationKey = "html.manage.permission.description." + permission?.split('.').join("_");
    const translated = t(translationKey);

    if (childNodes.length) {
        if (permission === undefined) {
            return <>{children}</>;
        } else {
            return (
                <details open={!toggled.includes(permission)}
                         style={root ? {marginLeft: "0.5rem"} : {marginLeft: "2.1rem"}}>
                    <summary onClick={event => {
                        event.preventDefault();
                        toggle(permission);
                    }}>
                        <Checkbox indeterminate={indeterminate} checked={checked}
                                  onChange={() => togglePermission(permission)}
                        >{permission} {permission && translated !== translationKey &&
                            <OpaqueText inline>&middot; {translated}</OpaqueText>}
                        </Checkbox>
                        <hr style={{margin: 0}}/>
                    </summary>

                    {children}
                </details>
            )
        }
    } else {
        return (
            <li style={root ? {marginLeft: "1.4rem"} : {marginLeft: "3rem"}}>
                <Checkbox checked={checked} onChange={() => togglePermission(permission)}
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
                    <h3 style={{display: "inline-block"}}>
                        <Trans i18nKey="html.label.managePage.groupPermissions" values={{groupName}}/>
                    </h3>
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
    const {t} = useTranslation();
    const {dirty, requestSave} = useConfigurationStorageContext();

    return (
        <ActionButton className={"float-end"} style={{margin: "-0.5rem"}}
                      disabled={!dirty}
                      onClick={requestSave}>
            <Fa icon={faFloppyDisk}/> {t('html.label.managePage.changes.save')}
        </ActionButton>
    )
}

const DeleteGroupButton = ({groupName, groups, reloadGroupNames}) => {
    const [clicked, setClicked] = useState(false);
    const [moveToGroup, setMoveToGroup] = useState(0);
    const {addAlert} = useAlertPopupContext();
    const {t} = useTranslation();

    if (clicked) {
        const groupOptions = groups.filter(g => g.name !== groupName).map(g => g.name);
        return (
            <Card>
                <CardHeader icon={faTrash} label={<Trans i18nKey={"html.label.managePage.deleteGroup.header"}
                                                         values={{groupName}}/>}/>
                <Card.Body>
                    <InputGroup>
                        <div className={"input-group-text"}>
                            {t('html.label.managePage.deleteGroup.moveToSelect')}
                        </div>
                        <Select options={groupOptions}
                                selectedIndex={moveToGroup} setSelectedIndex={setMoveToGroup}/>
                    </InputGroup>

                    <p className={"mt-3"}>
                        <Trans i18nKey="html.label.managePage.deleteGroup.confirmDescription"
                               values={{groupName, moveTo: groupOptions[moveToGroup]}}/>
                    </p>

                    <DangerButton className={"mt-2"}
                                  onClick={() => {
                                      deleteGroup(groupName, groupOptions[moveToGroup]).then(({error}) => {
                                          if (error) {
                                              addAlert({
                                                  timeout: 15000,
                                                  color: "danger",
                                                  content: <>
                                                      <Fa icon={faExclamationTriangle}/>
                                                      {" "}
                                                      <Trans i18nKey={"html.label.managePage.alert.groupDeleteFail"}
                                                             values={{error: error?.message}}/>
                                                  </>
                                              });
                                          } else {
                                              reloadGroupNames();
                                              setClicked(false);
                                              addAlert({
                                                  timeout: 7500,
                                                  color: "success",
                                                  content: <>
                                                      <Fa icon={faCheck}/>
                                                      {" "}
                                                      <Trans i18nKey={"html.label.managePage.alert.groupDeleteSuccess"}
                                                             values={{groupName}}/>
                                                  </>
                                              });
                                          }
                                      })
                                  }}>
                        <Fa icon={faTrash}/> <Trans i18nKey="html.label.managePage.deleteGroup.confirm"
                                                    values={{groupName}}/>
                    </DangerButton>
                    <OutlineButton className={"mt-2"} style={{marginLeft: "0.5rem"}}
                                   onClick={() => {
                                       setClicked(false)
                                   }}>
                        <Fa icon={faRotateLeft}/> {t("command.confirmation.deny")}
                    </OutlineButton>
                </Card.Body>
            </Card>
        )
    }

    return (
        <OutlineButton className={"float-end"}
                       onClick={() => {
                           setClicked(true)
                       }}>
            <Fa icon={faTrash}/> <Trans i18nKey={"html.label.managePage.deleteGroup.header"} values={{groupName}}/>
        </OutlineButton>
    )
}

const DiscardButton = () => {
    const {t} = useTranslation();
    const {dirty, requestDiscard} = useConfigurationStorageContext();

    return (
        <>
            {dirty && <button className={"float-end btn"} style={{margin: "-0.5rem", marginRight: "0.5rem"}}
                              onClick={requestDiscard}>
                <Fa icon={faTrash}/> {t('html.label.managePage.changes.discard')}
            </button>}
        </>
    )
}

const AddGroupBody = ({groups, reloadGroupNames}) => {
    const {t} = useTranslation();
    const [value, setValue] = useState(undefined);
    const {addAlert} = useAlertPopupContext();

    const isInvalid = newValue => newValue && (newValue.length > 100 || groups.find(group => group.name === newValue));
    const invalid = isInvalid(value);
    return (
        <Card>
            <CardHeader icon={faPlus} label={t('html.label.managePage.addGroup.header')}/>
            <Card.Body>
                <TextInput icon={faUserGroup}
                           isInvalid={isInvalid}
                           invalidFeedback={t('html.label.managePage.addGroup.invalidName')}
                           placeholder={t('html.label.managePage.addGroup.name')}
                           value={value}
                           setValue={newValue => setValue(newValue.toLowerCase().replace(" ", "_"))}
                />
                <button className={"btn bg-plan mt-2"} disabled={invalid || !value || value.length === 0}
                        onClick={() => {
                            addGroup(value).then(({error}) => {
                                if (error) {
                                    addAlert({
                                        timeout: 15000,
                                        color: "danger",
                                        content: <>
                                            <Fa icon={faExclamationTriangle}/>
                                            {" "}
                                            <Trans i18nKey={"html.label.managePage.alert.groupAddFail"}
                                                   values={{error: error?.message}}/>
                                        </>
                                    });
                                } else {
                                    addAlert({
                                        timeout: 7500,
                                        color: "success",
                                        content: <>
                                            <Fa icon={faCheck}/>
                                            {" "}
                                            <Trans i18nKey={"html.label.managePage.alert.groupAddSuccess"}
                                                   values={{groupName: value}}/>
                                        </>
                                    });
                                    reloadGroupNames();
                                }
                            })
                        }}>
                    <Fa icon={faFloppyDisk}/> {t('html.label.managePage.addGroup.header')}
                </button>
            </Card.Body>
        </Card>
    )
}

const GroupsCard = ({groups, reloadGroupNames}) => {
    const {t} = useTranslation();
    const {setHelpModalTopic} = useNavigation();
    const openHelp = useCallback(() => setHelpModalTopic('group-permissions'), [setHelpModalTopic]);

    const slices = groups.map(group => {
        return {
            body: <GroupEditContextProvider groupName={group.name}>
                <GroupsBody groups={groups} reloadGroupNames={reloadGroupNames}/>
            </GroupEditContextProvider>,
            header: <GroupsHeader groupName={group.name}/>,
            color: 'light-green',
            outline: false
        }
    })

    slices.push({
        body: <AddGroupBody groups={groups} reloadGroupNames={reloadGroupNames}/>,
        header: <GroupsHeader groupName={t('html.label.managePage.addGroup.header')} icon={faPlus}/>,
        color: 'light-green',
        outline: false
    })

    return (
        <Card>
            <CardHeader icon={faUsersGear} color="theme" label={t('html.label.managePage.groupHeader')}>
                <button className={"btn bg-transparent col-help-icon"} style={{margin: "-0.5rem"}}
                        onClick={openHelp}>
                    <Fa icon={faQuestionCircle}/>
                </button>
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
    const {hasPermission} = useAuth();
    const seeManageGroups = hasPermission('manage.groups');

    const [updateRequested, setUpdateRequested] = useState(Date.now());
    const [data, setData] = useState(undefined);
    const [loadingError, setLoadingError] = useState(undefined);
    const loadGroupNames = useCallback(() => {
        if (!seeManageGroups) return;
        fetchGroups().then(({data, error}) => {
            setData(data);
            setLoadingError(error);
        });
    }, [setData, setLoadingError, seeManageGroups]);
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
            {seeManageGroups && <ConfigurationStorageContextProvider>
                <DropdownStatusContextProvider>
                    <Row>
                        <Col md={12}>
                            <GroupsCard groups={data.groups} reloadGroupNames={reloadGroupNames}/>
                        </Col>
                    </Row>
                </DropdownStatusContextProvider>
            </ConfigurationStorageContextProvider>}
        </LoadIn>
    )
};

export default GroupsView