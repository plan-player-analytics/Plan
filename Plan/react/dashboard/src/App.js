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
import axios from "axios";
import ErrorView from "./views/ErrorView";
import {faMapSigns} from "@fortawesome/free-solid-svg-icons";
import {MetadataContextProvider} from "./hooks/metadataHook";
import {AuthenticationContextProvider} from "./hooks/authenticationHook";

const PlayerRedirect = () => {
    return (<Navigate to={"overview"} replace={true}/>)
}

const ContextProviders = ({children}) => (
    <AuthenticationContextProvider>
        <MetadataContextProvider>
            <ThemeContextProvider>
                {children}
            </ThemeContextProvider>
        </MetadataContextProvider>
    </AuthenticationContextProvider>
)

function App() {
    axios.defaults.withCredentials = true;

    return (
        <div className="App">
            <ContextProviders>
                <div id="wrapper">
                    <BrowserRouter>
                        <Routes>
                            <Route path="/player/:identifier" element={<PlayerPage/>}>
                                <Route path="" element={<PlayerRedirect/>}/>
                                <Route path="overview" element={<PlayerOverview/>}/>
                                <Route path="sessions" element={<PlayerSessions/>}/>
                                <Route path="pvppve" element={<PlayerPvpPve/>}/>
                                <Route path="servers" element={<PlayerServers/>}/>
                                <Route path="plugins/:serverName" element={<PlayerPluginData/>}/>
                                <Route path="*" element={<ErrorView error={{
                                    message: 'Unknown tab address, please correct the address',
                                    title: 'No such tab',
                                    icon: faMapSigns
                                }}/>}/>
                            </Route>
                        </Routes>
                    </BrowserRouter>
                </div>
            </ContextProviders>
        </div>
    );
}

export default App;
