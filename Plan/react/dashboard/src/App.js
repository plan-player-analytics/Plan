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
import {NavigationContextProvider} from "./hooks/navigationHook";
import ServerPage from "./views/ServerPage";
import ServerOverview from "./views/ServerOverview";
import MainPageRedirect from "./components/navigation/MainPageRedirect";
import ServerOnlineActivity from "./views/ServerOnlineActivity";

const OverviewRedirect = () => {
    return (<Navigate to={"overview"} replace={true}/>)
}

const ContextProviders = ({children}) => (
    <AuthenticationContextProvider>
        <MetadataContextProvider>
            <ThemeContextProvider>
                <NavigationContextProvider>
                    {children}
                </NavigationContextProvider>
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
                            <Route path="" element={<MainPageRedirect/>}/>
                            <Route path="/player/:identifier" element={<PlayerPage/>}>
                                <Route path="" element={<OverviewRedirect/>}/>
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
                            <Route path="/server/:identifier" element={<ServerPage/>}>
                                <Route path="" element={<OverviewRedirect/>}/>
                                <Route path="overview" element={<ServerOverview/>}/>
                                <Route path="online-activity" element={<ServerOnlineActivity/>}/>
                                <Route path="sessions" element={<></>}/>
                                <Route path="pvppve" element={<></>}/>
                                <Route path="playerbase" element={<></>}/>
                                <Route path="players" element={<></>}/>
                                <Route path="geolocations" element={<></>}/>
                                <Route path="performance" element={<></>}/>
                                <Route path="plugins-overview" element={<></>}/>
                            </Route>
                        </Routes>
                    </BrowserRouter>
                </div>
            </ContextProviders>
        </div>
    );
}

export default App;
