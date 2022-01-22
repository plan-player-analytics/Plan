import player from './mockdata/player.json';

import PlayerOverview from "./views/PlayerOverview";
import PlayerSessions from "./views/PlayerSessions";

import './style/main.sass';
import './style/sb-admin-2.css'
import './style/style.css';

function App() {
    return (
        <div className="App">
            <div id="content" style={{display: 'flex'}}>
                <div className="container-fluid mt-4">
                    <PlayerOverview player={player}/>
                    <PlayerSessions player={player}/>
                </div>
            </div>
        </div>
    );
}

export default App;
