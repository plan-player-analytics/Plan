import player from './mockdata/player.json';

import PlayerOverview from "./views/PlayerOverview";
import PlayerSessions from "./views/PlayerSessions";

import './style/main.sass';
import './style/sb-admin-2.css'
import './style/style.css';
import PlayerPvpPve from "./views/PlayerPvpPve";
import PlayerServers from "./views/PlayerServers";
import Sidebar from "./components/navigation/Sidebar";

function App() {
    return (
        <div className="App">
            <div id="wrapper">
                <Sidebar/>
                <div className="d-flex flex-column" id="content-wrapper">
                    <div id="content" style={{display: 'flex'}}>
                        <div className="container-fluid mt-4">
                            <PlayerOverview player={player}/>
                            <PlayerSessions player={player}/>
                            <PlayerPvpPve player={player}/>
                            <PlayerServers player={player}/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default App;
