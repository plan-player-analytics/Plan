import {useAuth} from "../../hooks/authenticationHook";
import {useMetadata} from "../../hooks/metadataHook";
import {Navigate} from "react-router-dom";
import React, {useEffect, useState} from "react";

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
                    change package.json "proxy" to your Plan webserver address.
                </p>
                <p className="m-4">
                    <button className="btn bg-plan" onClick={() => window.location.reload()}>Click to Refresh the
                        page & try again.
                    </button>
                </p>
            </div>
        </>
    } else {
        return <p className="m-4">Redirecting..</p>
    }
}

const MainPageRedirect = () => {
    const {authLoaded, authRequired, loggedIn, user} = useAuth();
    const {isProxy, serverName} = useMetadata();

    console.log(authLoaded, authRequired, loggedIn, user)

    if (!authLoaded || !serverName) {
        return <RedirectPlaceholder/>
    }

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