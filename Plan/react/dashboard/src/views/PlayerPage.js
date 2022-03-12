import React, {useEffect, useState} from "react";
import Sidebar from "../components/navigation/Sidebar";
import {Outlet, useOutletContext, useParams} from "react-router-dom";
import ColorSelectorModal from "../components/modal/ColorSelectorModal";
import {NightModeCss} from "../hooks/themeHook";
import {fetchPlayer} from "../service/playerService";
import ErrorView from "./ErrorView";


const PlayerPage = () => {
    const [player, setPlayer] = useState(undefined);
    const [error, setError] = useState(undefined);

    const {identifier} = useParams();

    const updatePlayer = async () => {
        try {
            setPlayer(await fetchPlayer(identifier));
        } catch (e) {
            setError(e);
        }
    }

    useEffect(() => {
        updatePlayer()
    }, [identifier]);

    if (error) {
        return <>
            <NightModeCss/>
            <Sidebar/>
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
            <Sidebar/>
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