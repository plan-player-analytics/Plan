import {useAuth} from "../../hooks/authenticationHook";
import {useMetadata} from "../../hooks/metadataHook";
import {Navigate} from "react-router-dom";
import React from "react";

const MainPageRedirect = () => {
    const {authLoaded, authRequired, loggedIn, user} = useAuth();
    const {isProxy, serverName} = useMetadata();

    if (!authLoaded || !serverName) return <></>

    if (authRequired && !loggedIn) {
        return (<Navigate to={"login"} replace={true}/>)
    } else if (authRequired && loggedIn) {
        if (isProxy && user.permissions.includes('page.network')) {
            return (<Navigate to={"network/overview"} replace={true}/>)
        } else if (user.permissions.includes('page.server')) {
            return (<Navigate to={"server/overview"} replace={true}/>)
        } else if (user.permissions.includes('page.player.other')) {
            return (<Navigate to={"players"} replace={true}/>)
        } else if (user.permissions.includes('page.player.self')) {
            return (<Navigate to={"player/" + user.linkedToUuid} replace={true}/>)
        }
    } else {
        return (<Navigate to={isProxy ? "network/overview" : "server/" + encodeURIComponent(serverName) + "/overview"}
                          replace={true}/>)
    }
}

export default MainPageRedirect