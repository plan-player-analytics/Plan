import './style/main.sass';
import './style/sb-admin-2.css'
import './style/default-colors.css';
import './style/style.css';
import './style/mobile.css';
import 'react-bootstrap-range-slider/dist/react-bootstrap-range-slider.css';

import {createBrowserRouter, createRoutesFromElements, Navigate, Route, RouterProvider} from "react-router-dom";
import React, {useCallback, useEffect} from "react";
import {ThemeContextProvider, useTheme} from "./hooks/themeHook";
import axios from "axios";
import ErrorView from "./views/ErrorView";
import {faMapSigns} from "@fortawesome/free-solid-svg-icons";
import {MetadataContextProvider} from "./hooks/metadataHook";
import {AuthenticationContextProvider} from "./hooks/authenticationHook";
import {NavigationContextProvider} from "./hooks/navigationHook";
import MainPageRedirect from "./components/navigation/MainPageRedirect";
import {baseAddress, staticSite} from "./service/backendConfiguration";
import {PageExtensionContextProvider} from "./hooks/pageExtensionHook";
import ErrorBoundary from "./components/ErrorBoundary";
import {AlertPopupContextProvider} from "./hooks/context/alertPopupContext";
import {PreferencesContextProvider} from "./hooks/preferencesHook";
import {ThemeStorageContextProvider} from "./hooks/context/themeContextHook.jsx";
import {ThemeStyleCss} from "./components/theme/ThemeStyleCss.jsx";

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
const ServerAllowList = React.lazy(() => import("./views/server/ServerAllowList"));
const PlayerbaseOverview = React.lazy(() => import("./views/server/PlayerbaseOverview"));
const ServerPlayers = React.lazy(() => import("./views/server/ServerPlayers"));
const ServerGeolocations = React.lazy(() => import("./views/server/ServerGeolocations"));
const ServerPerformance = React.lazy(() => import("./views/server/ServerPerformance"));
const ServerPluginHistory = React.lazy(() => import('./views/server/ServerPluginHistory'));
const ServerPluginData = React.lazy(() => import("./views/server/ServerPluginData"));
const ServerWidePluginData = React.lazy(() => import("./views/server/ServerWidePluginData"));
const ServerJoinAddresses = React.lazy(() => import("./views/server/ServerJoinAddresses"));
const ServerPlayerRetention = React.lazy(() => import("./views/server/ServerPlayerRetention"));

const NetworkPage = React.lazy(() => import("./views/layout/NetworkPage"));
const NetworkOverview = React.lazy(() => import("./views/network/NetworkOverview"));
const NetworkServers = React.lazy(() => import("./views/network/NetworkServers"));
const NetworkSessions = React.lazy(() => import("./views/network/NetworkSessions"));
const NetworkJoinAddresses = React.lazy(() => import("./views/network/NetworkJoinAddresses"));
const NetworkPlayerRetention = React.lazy(() => import("./views/network/NetworkPlayerRetention"));
const NetworkGeolocations = React.lazy(() => import("./views/network/NetworkGeolocations"));
const NetworkPlayerbaseOverview = React.lazy(() => import("./views/network/NetworkPlayerbaseOverview"));
const NetworkPerformance = React.lazy(() => import("./views/network/NetworkPerformance"));
const NetworkPluginHistory = React.lazy(() => import('./views/network/NetworkPluginHistory'));

const PlayersPage = React.lazy(() => import("./views/layout/PlayersPage"));
const AllPlayers = React.lazy(() => import("./views/players/AllPlayers"));

const QueryPage = React.lazy(() => import("./views/layout/QueryPage"));
const NewQueryView = React.lazy(() => import("./views/query/NewQueryView"));
const QueryResultView = React.lazy(() => import("./views/query/QueryResultView"));

const ManagePage = React.lazy(() => import("./views/layout/ManagePage"));
const GroupsView = React.lazy(() => import("./views/manage/GroupsView"));

const ThemeEditorPage = React.lazy(() => import("./views/layout/ThemeEditorPage"));
const AddThemeView = React.lazy(() => import("./views/theme/AddThemeView"));
const ThemeEditorView = React.lazy(() => import("./views/theme/ThemeEditorView"));

const LoginPage = React.lazy(() => import("./views/layout/LoginPage"));
const RegisterPage = React.lazy(() => import("./views/layout/RegisterPage"));
const ErrorPage = React.lazy(() => import("./views/layout/ErrorPage"));
const ErrorsPage = React.lazy(() => import("./views/layout/ErrorsPage"));
const SwaggerView = React.lazy(() => import("./views/SwaggerView"));

const OverviewRedirect = () => {
    return (<Navigate to={"overview"} replace={true}/>)
}
const NewRedirect = () => {
    return (<Navigate to={"new"} replace={true}/>)
}
const GroupsRedirect = () => {
    return (<Navigate to={"groups"} replace={true}/>)
}

const ContextProviders = ({children}) => (
    <AuthenticationContextProvider>
        <MetadataContextProvider>
            <PreferencesContextProvider>
                <ThemeContextProvider>
                    <ThemeStorageContextProvider>
                        <NavigationContextProvider>
                            <AlertPopupContextProvider>
                                <PageExtensionContextProvider>
                                    {children}
                                </PageExtensionContextProvider>
                            </AlertPopupContextProvider>
                        </NavigationContextProvider>
                    </ThemeStorageContextProvider>
                </ThemeContextProvider>
            </PreferencesContextProvider>
        </MetadataContextProvider>
    </AuthenticationContextProvider>
)

const Lazy = ({children}) => {
    const fallbackFunction = useCallback((error) => <ErrorView error={error}/>, []);
    return (
        <React.Suspense fallback={<></>}>
            <ErrorBoundary fallbackFunction={fallbackFunction}>
                {children}
            </ErrorBoundary>
        </React.Suspense>
    );
}

const getBasename = () => {
    if (baseAddress) {
        const addressWithoutProtocol = baseAddress
            .replace("http://", "")
            .replace("https://", "");
        const startOfPath = addressWithoutProtocol.indexOf("/");
        return startOfPath >= 0 ? addressWithoutProtocol.substring(startOfPath) : "";
    } else {
        return "";
    }
}

const router = createBrowserRouter(
    createRoutesFromElements(
        <>
            <Route path="" element={<MainPageRedirect/>}/>
            <Route path="/" element={<MainPageRedirect/>}/>
            <Route path="index.html" element={<MainPageRedirect/>}/>
            {!staticSite && <Route path="/login" element={<Lazy><LoginPage/></Lazy>}/>}
            {!staticSite && <Route path="/register" element={<Lazy><RegisterPage/></Lazy>}/>}
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
                <Route path="" element={<OverviewRedirect/>}/>
                <Route path="overview" element={<Lazy><ServerOverview/></Lazy>}/>
                <Route path="online-activity" element={<Lazy><OnlineActivity/></Lazy>}/>
                <Route path="sessions" element={<Lazy><ServerSessions/></Lazy>}/>
                <Route path="pvppve" element={<Lazy><ServerPvpPve/></Lazy>}/>
                <Route path="allowlist" element={<Lazy><ServerAllowList/></Lazy>}/>
                <Route path="playerbase" element={<Lazy><PlayerbaseOverview/></Lazy>}/>
                <Route path="join-addresses" element={<Lazy><ServerJoinAddresses/></Lazy>}/>
                <Route path="retention" element={<Lazy><ServerPlayerRetention/></Lazy>}/>
                <Route path="players" element={<Lazy><ServerPlayers/></Lazy>}/>
                <Route path="geolocations" element={<Lazy><ServerGeolocations/></Lazy>}/>
                <Route path="performance" element={<Lazy><ServerPerformance/></Lazy>}/>
                <Route path="plugin-history" element={<Lazy><ServerPluginHistory/></Lazy>}/>
                <Route path="plugins-overview" element={<Lazy><ServerPluginData/></Lazy>}/>
                <Route path="plugins/:plugin" element={<Lazy><ServerWidePluginData/></Lazy>}/>
                <Route path="*" element={<ErrorView error={{
                    message: 'Unknown tab address, please correct the address',
                    title: 'No such tab',
                    icon: faMapSigns
                }}/>}/>
            </Route>
            <Route path="/network" element={<Lazy><NetworkPage/></Lazy>}>
                <Route path="" element={<OverviewRedirect/>}/>
                <Route path="overview" element={<Lazy><NetworkOverview/></Lazy>}/>
                <Route path="serversOverview" element={<Lazy><NetworkServers/></Lazy>}/>
                <Route path="sessions" element={<Lazy><NetworkSessions/></Lazy>}/>
                {!staticSite &&
                    <Route path="performance" element={<Lazy><NetworkPerformance/></Lazy>}/>}
                <Route path="playerbase" element={<Lazy><NetworkPlayerbaseOverview/></Lazy>}/>
                <Route path="retention" element={<Lazy><NetworkPlayerRetention/></Lazy>}/>
                <Route path="join-addresses" element={<Lazy><NetworkJoinAddresses/></Lazy>}/>
                <Route path="players" element={<Lazy><AllPlayers/></Lazy>}/>
                <Route path="geolocations" element={<Lazy><NetworkGeolocations/></Lazy>}/>
                <Route path="plugin-history" element={<Lazy><NetworkPluginHistory/></Lazy>}/>
                <Route path="plugins-overview" element={<Lazy><ServerPluginData/></Lazy>}/>
                <Route path="plugins/:plugin" element={<Lazy><ServerWidePluginData/></Lazy>}/>
                <Route path="*" element={<ErrorView error={{
                    message: 'Unknown tab address, please correct the address',
                    title: 'No such tab',
                    icon: faMapSigns
                }}/>}/>
            </Route>
            {!staticSite && <Route path="/manage" element={<Lazy><ManagePage/></Lazy>}>
                <Route path="" element={<GroupsRedirect/>}/>
                <Route path="groups" element={<Lazy><GroupsView/></Lazy>}/>
            </Route>}
            {!staticSite && <Route path="/query" element={<Lazy><QueryPage/></Lazy>}>
                <Route path="" element={<NewRedirect/>}/>
                <Route path="new" element={<Lazy><NewQueryView/></Lazy>}/>
                <Route path="result" element={<Lazy><QueryResultView/></Lazy>}/>
            </Route>}
            {!staticSite && <Route path="/errors" element={<Lazy><ErrorsPage/></Lazy>}/>}
            {!staticSite && <Route path="/docs" element={<Lazy><SwaggerView/></Lazy>}/>}
            <Route path="/theme-editor" element={
                <Lazy>
                    <ThemeEditorPage/>
                </Lazy>
            }>
                <Route path="" element={<NewRedirect/>}/>
                <Route path=":identifier" element={<Lazy><ThemeEditorView/></Lazy>}/>
                <Route path="new" element={<Lazy><AddThemeView/></Lazy>}/>
            </Route>
            <Route path="*" element={<Lazy><ErrorPage error={{
                message: 'Page not found, please correct the address',
                title: 'No such page',
                icon: faMapSigns
            }}/></Lazy>}/>
        </>
    ), {basename: getBasename()}
);

const Wrapper = ({children}) => {
    const {nightModeEnabled} = useTheme();

    useEffect(() => {
        if (nightModeEnabled) {
            document.body.classList.add('night-mode-colors');
        } else {
            document.body.classList.remove('night-mode-colors');
        }
    }, [nightModeEnabled]);

    return (
        <div id={`wrapper`}>
            {children}
        </div>
    )
}

function App() {
    axios.defaults.withCredentials = true;

    return (
        <div className="App">
            <ContextProviders>
                <ThemeStyleCss/>
                <Wrapper>
                    <RouterProvider router={router}/>
                </Wrapper>
            </ContextProviders>
        </div>
    );
}

export default App;
