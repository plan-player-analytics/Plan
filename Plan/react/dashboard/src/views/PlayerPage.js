import React from "react";
import Sidebar from "../components/navigation/Sidebar";
import {Outlet} from "react-router-dom";
import ColorSelectorModal from "../components/modal/ColorSelectorModal";
import {NightModeCss} from "../hooks/themeHook";


const PlayerPage = () => {
    return (
        <>
            <NightModeCss/>
            <Sidebar/>
            <div className="d-flex flex-column" id="content-wrapper">
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        <Outlet/>
                    </main>
                    <aside>
                        <ColorSelectorModal/>
                    </aside>
                </div>
            </div>
        </>
    )
}

export default PlayerPage;