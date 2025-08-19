import React from 'react';
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {useTranslation} from "react-i18next";
import {Link} from "react-router-dom";
import {faUser} from "@fortawesome/free-solid-svg-icons";
import {useAuth} from "../../../hooks/authenticationHook.jsx";

const PlayerPageLinkButton = ({uuid, className}) => {
    const {t} = useTranslation();
    const {authRequired, hasPermission, user} = useAuth();

    const canSeePlayer = hasPermission('access.player') || !authRequired
        || hasPermission('access.player.self') && uuid === user.playerUUID;
    if (!canSeePlayer) return <></>;

    return (
        <Link to={`/player/${uuid}`} className={`btn bg-players-online ${className || ''}`}>
            <Fa icon={faUser}/> {t('html.label.playerPage')}
        </Link>
    )
};

export default PlayerPageLinkButton