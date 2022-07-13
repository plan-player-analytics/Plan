import './style/main.sass';
import './style/sb-admin-2.css'
import './style/style.css';

import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import React from "react";
import PlayerPage from "./views/layout/PlayerPage";
import PlayerOverview from "./views/player/PlayerOverview";
import PlayerSessions from "./views/player/PlayerSessions";
import PlayerPvpPve from "./views/player/PlayerPvpPve";
import PlayerServers from "./views/player/PlayerServers";
import PlayerPluginData from "./views/player/PlayerPluginData";
import {ThemeContextProvider} from "./hooks/themeHook";
import axios from "axios";
import ErrorView from "./views/ErrorView";
import {faMapSigns} from "@fortawesome/free-solid-svg-icons";
import {MetadataContextProvider} from "./hooks/metadataHook";
import {AuthenticationContextProvider} from "./hooks/authenticationHook";
import {NavigationContextProvider} from "./hooks/navigationHook";
import ServerPage from "./views/layout/ServerPage";
import ServerOverview from "./views/server/ServerOverview";
import MainPageRedirect from "./components/navigation/MainPageRedirect";
import OnlineActivity from "./views/server/OnlineActivity";
import ServerSessions from "./views/server/ServerSessions";
import ServerPvpPve from "./views/server/ServerPvpPve";
import PlayerbaseOverview from "./views/server/PlayerbaseOverview";
import ServerPlayers from "./views/server/ServerPlayers";
import PlayersPage from "./views/layout/PlayersPage";
import AllPlayers from "./views/players/AllPlayers";
import ServerGeolocations from "./views/server/ServerGeolocations";

const SwaggerView = React.lazy(() => import("./views/SwaggerView"));

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
                            <Route path="/players" element={<PlayersPage/>}>
                                <Route path="" element={<AllPlayers/>}/>
                                <Route path="*" element={<AllPlayers/>}/>
                            </Route>
                            <Route path="/server/:identifier" element={<ServerPage/>}>
                                <Route path="" element={<OverviewRedirect/>}/>
                                <Route path="overview" element={<ServerOverview/>}/>
                                <Route path="online-activity" element={<OnlineActivity/>}/>
                                <Route path="sessions" element={<ServerSessions/>}/>
                                <Route path="pvppve" element={<ServerPvpPve/>}/>
                                <Route path="playerbase" element={<PlayerbaseOverview/>}/>
                                <Route path="retention" element={<></>}/>
                                <Route path="players" element={<ServerPlayers/>}/>
                                <Route path="geolocations" element={<ServerGeolocations/>}/>
                                <Route path="performance" element={<></>}/>
                                <Route path="plugins-overview" element={<></>}/>
                            </Route>
                            <Route path="docs" element={<React.Suspense fallback={<></>}>
                                <SwaggerView/>
                            </React.Suspense>}/>
                        </Routes>
                    </BrowserRouter>
                </div>
            </ContextProviders>
        </div>
    );
}

export default App;
