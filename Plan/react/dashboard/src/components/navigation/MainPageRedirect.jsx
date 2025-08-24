import {useAuth} from "../../hooks/authenticationHook";
import {useMetadata} from "../../hooks/metadataHook";
import {Navigate} from "react-router";
import React, {useEffect, useState} from "react";
import {staticSite} from "../../service/backendConfiguration";

const RedirectPlaceholder = () => {
    const [redirectStart] = useState(Date.now())
    const [dateDiff, setDateDiff] = useState(0)

    useEffect(() => {
        const interval = setInterval(() => {
            if (dateDiff <= 50) {
                setDateDiff(Date.now() - redirectStart);
            } else {
                clearInterval(interval);
            }
        }, 500);
        return () => clearInterval(interval);
    }, [redirectStart, dateDiff])

    if (dateDiff > 50) {
        return <>
            <p style={{marginLeft: "14rem"}}></p>
            <p className="m-4">Redirecting..</p>
            <div style={{maxWidth: "500px"}}>
                <p className="m-4">
                    This is taking longer than expected.
                </p>
                <p className="m-4">
                    Make sure the Plan webserver is enabled.<br/>(This page can show up if the Plan webserver goes
                    offline.)
                </p>
                <p className="m-4">
                    If you are trying to set up a development environment,
                    change package.json "proxy" to address of your Plan webserver.
                </p>
                <p className="m-4">
                    <button className="btn bg-plan" onClick={() => window.location.reload()}>Click to Refresh the
                        page & try again.
                    </button>
                </p>
            </div>
        </>
    } else {
        return <>
            <p style={{marginLeft: "14rem"}}></p>
            <p className="m-4">Redirecting..</p>
        </>
    }
}

const MainPageRedirect = () => {
    const {authLoaded, authRequired, loggedIn, user, hasPermission} = useAuth();
    const {isProxy, serverName, serverUUID} = useMetadata();

    if (staticSite) {
        const urlParams = new URLSearchParams(window.location.search);
        const redirect = urlParams.get('redirect');
        if (redirect) {
            return (<Navigate to={redirect} replace={true}/>)
        }
    }

    if (!authLoaded || !serverName || !serverUUID) {
        return <RedirectPlaceholder/>
    }

    const redirectBasedOnPermissions = () => {
        if (isProxy && hasPermission('access.network')) {
            return (<Navigate to={"/network/overview"} replace={true}/>)
        } else if (hasPermission('access.server.' + serverUUID)) {
            return (<Navigate to={"/server/" + serverUUID + "/overview"}
                              replace={true}/>)
        } else if (hasPermission('access.player')) {
            return (<Navigate to={"/players"} replace={true}/>)
        } else if (hasPermission('access.player.self')) {
            return (<Navigate to={"/player/" + (user.playerUUID ? user.playerUUID : user.username)} replace={true}/>)
        }
    };

    if (authRequired && !loggedIn) {
        if (!window.location.pathname.startsWith("/login")) {
            return (<Navigate
                to={"/login?from=" + encodeURIComponent(window.location.pathname + window.location.search + window.location.hash)}
                replace={true}/>)
        } else {
            return (<Navigate to="/login" replace={true}/>)
        }
    } else if (authRequired && loggedIn) {
        return redirectBasedOnPermissions();
    } else {
        return (<Navigate
            to={isProxy ? "/network/overview" : "/server/" + serverUUID + "/overview"}
            replace={true}/>)
    }
}

export default MainPageRedirect
