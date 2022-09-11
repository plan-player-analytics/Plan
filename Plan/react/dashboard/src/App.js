import './style/main.sass';
import './style/sb-admin-2.css'
import './style/style.css';

import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import React from "react";
import {ThemeContextProvider} from "./hooks/themeHook";
import axios from "axios";
import ErrorView from "./views/ErrorView";
import {faMapSigns} from "@fortawesome/free-solid-svg-icons";
import {MetadataContextProvider} from "./hooks/metadataHook";
import {AuthenticationContextProvider} from "./hooks/authenticationHook";
import {NavigationContextProvider} from "./hooks/navigationHook";
import MainPageRedirect from "./components/navigation/MainPageRedirect";

const PlayerPage = React.lazy(() => import("./views/layout/PlayerPage"));
const PlayerOverview = React.lazy(() => import("./views/player/PlayerOverview"));
const PlayerSessions = React.lazy(() => import("./views/player/PlayerSessions"));
const PlayerPvpPve = React.lazy(() => import("./views/player/PlayerPvpPve"));
const PlayerServers = React.lazy(() => import("./views/player/PlayerServers"));
const PlayerPluginData = React.lazy(() => import("./views/player/PlayerPluginData"));

const ServerPage = React.lazy(() => import("./views/layout/ServerPage"));
const ServerOverview = React.lazy(() => import("./views/server/ServerOverview"));
const OnlineActivity = React.lazy(() => import("./views/server/OnlineActivity"));
const ServerSessions = React.lazy(() => import("./views/server/ServerSessions"));
const ServerPvpPve = React.lazy(() => import("./views/server/ServerPvpPve"));
const PlayerbaseOverview = React.lazy(() => import("./views/server/PlayerbaseOverview"));
const ServerPlayers = React.lazy(() => import("./views/server/ServerPlayers"));
const ServerGeolocations = React.lazy(() => import("./views/server/ServerGeolocations"));
const LoginPage = React.lazy(() => import("./views/layout/LoginPage"));
const ServerPerformance = React.lazy(() => import("./views/server/ServerPerformance"));
const ServerPluginData = React.lazy(() => import("./views/server/ServerPluginData"));
const ServerWidePluginData = React.lazy(() => import("./views/server/ServerWidePluginData"));
const ServerJoinAddresses = React.lazy(() => import("./views/server/ServerJoinAddresses"));

const NetworkPage = React.lazy(() => import("./views/layout/NetworkPage"));
const NetworkOverview = React.lazy(() => import("./views/network/NetworkOverview"));
const NetworkServers = React.lazy(() => import("./views/network/NetworkServers"));
const NetworkSessions = React.lazy(() => import("./views/network/NetworkSessions"));
const NetworkJoinAddresses = React.lazy(() => import("./views/network/NetworkJoinAddresses"));
const NetworkGeolocations = React.lazy(() => import("./views/network/NetworkGeolocations"));

const PlayersPage = React.lazy(() => import("./views/layout/PlayersPage"));
const AllPlayers = React.lazy(() => import("./views/players/AllPlayers"));

const ErrorsPage = React.lazy(() => import("./views/layout/ErrorsPage"));
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

const Lazy = ({children}) => (
    <React.Suspense fallback={<></>}>
        {children}
    </React.Suspense>
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
                            <Route path="/" element={<MainPageRedirect/>}/>
                            <Route path="/login" element={<Lazy><LoginPage/></Lazy>}/>
                            <Route path="/player/:identifier" element={<Lazy><PlayerPage/></Lazy>}>
                                <Route path="" element={<Lazy><OverviewRedirect/></Lazy>}/>
                                <Route path="overview" element={<Lazy><PlayerOverview/></Lazy>}/>
                                <Route path="sessions" element={<Lazy><PlayerSessions/></Lazy>}/>
                                <Route path="pvppve" element={<Lazy><PlayerPvpPve/></Lazy>}/>
                                <Route path="servers" element={<Lazy><PlayerServers/></Lazy>}/>
                                <Route path="plugins/:serverName" element={<Lazy><PlayerPluginData/></Lazy>}/>
                                <Route path="*" element={<ErrorView error={{
                                    message: 'Unknown tab address, please correct the address',
                                    title: 'No such tab',
                                    icon: faMapSigns
                                }}/>}/>
                            </Route>
                            <Route path="/players" element={<Lazy><PlayersPage/></Lazy>}>
                                <Route path="" element={<Lazy><AllPlayers/></Lazy>}/>
                                <Route path="*" element={<Lazy><AllPlayers/></Lazy>}/>
                            </Route>
                            <Route path="/server/:identifier" element={<Lazy><ServerPage/></Lazy>}>
                                <Route path="" element={<Lazy><OverviewRedirect/></Lazy>}/>
                                <Route path="overview" element={<Lazy><ServerOverview/></Lazy>}/>
                                <Route path="online-activity" element={<Lazy><OnlineActivity/></Lazy>}/>
                                <Route path="sessions" element={<Lazy><ServerSessions/></Lazy>}/>
                                <Route path="pvppve" element={<Lazy><ServerPvpPve/></Lazy>}/>
                                <Route path="playerbase" element={<Lazy><PlayerbaseOverview/></Lazy>}/>
                                <Route path="join-addresses" element={<Lazy><ServerJoinAddresses/></Lazy>}/>
                                <Route path="retention" element={<></>}/>
                                <Route path="players" element={<Lazy><ServerPlayers/></Lazy>}/>
                                <Route path="geolocations" element={<Lazy><ServerGeolocations/></Lazy>}/>
                                <Route path="performance" element={<Lazy><ServerPerformance/></Lazy>}/>
                                <Route path="plugins-overview" element={<Lazy><ServerPluginData/></Lazy>}/>
                                <Route path="plugins/:plugin" element={<Lazy><ServerWidePluginData/></Lazy>}/>
                                <Route path="*" element={<ErrorView error={{
                                    message: 'Unknown tab address, please correct the address',
                                    title: 'No such tab',
                                    icon: faMapSigns
                                }}/>}/>
                            </Route>
                            <Route path="/network" element={<Lazy><NetworkPage/></Lazy>}>
                                <Route path="" element={<Lazy><OverviewRedirect/></Lazy>}/>
                                <Route path="overview" element={<Lazy><NetworkOverview/></Lazy>}/>
                                <Route path="serversOverview" element={<Lazy><NetworkServers/></Lazy>}/>
                                <Route path="sessions" element={<Lazy><NetworkSessions/></Lazy>}/>
                                <Route path="join-addresses" element={<Lazy><NetworkJoinAddresses/></Lazy>}/>
                                <Route path="players" element={<Lazy><AllPlayers/></Lazy>}/>
                                <Route path="geolocations" element={<Lazy><NetworkGeolocations/></Lazy>}/>
                                <Route path="plugins-overview" element={<Lazy><ServerPluginData/></Lazy>}/>
                                <Route path="plugins/:plugin" element={<Lazy><ServerWidePluginData/></Lazy>}/>
                                <Route path="*" element={<ErrorView error={{
                                    message: 'Unknown tab address, please correct the address',
                                    title: 'No such tab',
                                    icon: faMapSigns
                                }}/>}/>
                            </Route>
                            <Route path="/errors" element={<Lazy><ErrorsPage/></Lazy>}/>
                            <Route path="/docs" element={<Lazy><SwaggerView/></Lazy>}/>
                        </Routes>
                    </BrowserRouter>
                </div>
            </ContextProviders>
        </div>
    );
}

export default App;
