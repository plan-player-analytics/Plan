import React from 'react';
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faServer} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import {Link} from "react-router";
import {useAuth} from "../../../hooks/authenticationHook.jsx";

const ServerPageLinkButton = ({uuid, className}) => {
    const {t} = useTranslation();
    const {hasPermission} = useAuth();

    const canSeeServer = hasPermission('access.server');
    if (!canSeeServer) return <></>

    return (
        <Link to={`/server/${uuid}`}
              className={`btn bg-servers ${className || ''}`}>
            <Fa icon={faServer}/> {t('html.label.serverPage')}
        </Link>
    )
};

export default ServerPageLinkButton