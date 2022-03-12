import React, {useEffect, useState} from "react";
import Sidebar from "../components/navigation/Sidebar";
import {Outlet, useOutletContext, useParams} from "react-router-dom";
import ColorSelectorModal from "../components/modal/ColorSelectorModal";
import {NightModeCss} from "../hooks/themeHook";
import {fetchPlayer} from "../service/playerService";
import ErrorView from "./ErrorView";
import {faCalendar, faCampground, faCubes, faInfoCircle, faNetworkWired} from "@fortawesome/free-solid-svg-icons";


const PlayerPage = () => {
    const [player, setPlayer] = useState(undefined);
    const [error, setError] = useState(undefined);
    const [sidebarItems, setSidebarItems] = useState([]);

    const {identifier} = useParams();

    const updatePlayer = async (id) => {
        try {
            setPlayer(await fetchPlayer(id));
        } catch (e) {
            setError(e);
        }
    }

    useEffect(() => {
        updatePlayer(identifier)
    }, [identifier]);

    useEffect(() => {
        if (!player) return;

        const items = [
            {name: "Player Overview", icon: faInfoCircle, href: "overview"},
            {name: "Sessions", icon: faCalendar, href: "sessions"},
            {name: "PvP & PvE", icon: faCampground, href: "pvppve"},
            {name: "Servers Overview", icon: faNetworkWired, href: "servers"}
        ]

        player.extensions.map(extension => {
            return {name: `Plugins (${extension.serverName})`, icon: faCubes, href: `plugins/${extension.serverName}`}
        }).forEach(item => items.push(item));

        setSidebarItems(items);
        window.document.title = `Plan | ${player.info.name}`;
    }, [player])


    if (error) {
        return <>
            <NightModeCss/>
            <Sidebar items={sidebarItems}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        <ErrorView error={error}/>
                    </main>
                    <aside>
                        <ColorSelectorModal/>
                    </aside>
                </div>
            </div>
        </>
    }

    return player ? (
        <>
            <NightModeCss/>
            <Sidebar items={sidebarItems}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        <Outlet context={{player, updatePlayer}}/>
                    </main>
                    <aside>
                        <ColorSelectorModal/>
                    </aside>
                </div>
            </div>
        </>
    ) : <>
        <NightModeCss/>
        <div className="page-loader">
            <div className="loader-container">
                <span className="loader"/>
                <p className="loader-text">Please wait..</p>
            </div>
        </div>
    </>
}

export const usePlayer = () => {
    return useOutletContext();
}

export default PlayerPage;