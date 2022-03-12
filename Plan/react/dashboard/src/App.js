import './style/main.sass';
import './style/sb-admin-2.css'
import './style/style.css';

import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import React from "react";
import PlayerPage from "./views/PlayerPage";
import PlayerOverview from "./views/PlayerOverview";
import PlayerSessions from "./views/PlayerSessions";
import PlayerPvpPve from "./views/PlayerPvpPve";
import PlayerServers from "./views/PlayerServers";
import PlayerPluginData from "./views/PlayerPluginData";
import {ThemeContextProvider} from "./hooks/themeHook";

const PlayerRedirect = () => {
    return (<Navigate to={"overview"} replace={true}/>)
}

function App() {
    return (
        <div className="App">
            <ThemeContextProvider>
                <div id="wrapper">
                    <BrowserRouter>
                        <Routes>
                            <Route path="/" element={<Navigate to="/player/AuroraLS3" replace={true}/>}/>
                            <Route path="/player/:identifier" element={<PlayerPage/>}>
                                <Route path="" element={<PlayerRedirect/>}/>
                                <Route path="overview" element={<PlayerOverview/>}/>
                                <Route path="sessions" element={<PlayerSessions/>}/>
                                <Route path="pvppve" element={<PlayerPvpPve/>}/>
                                <Route path="servers" element={<PlayerServers/>}/>
                                <Route path="plugins/:serverName" element={<PlayerPluginData/>}/>
                            </Route>
                        </Routes>
                    </BrowserRouter>
                </div>
            </ThemeContextProvider>
        </div>
    );
}

export default App;
