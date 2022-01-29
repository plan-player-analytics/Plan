import React from "react";
import Sidebar from "../components/navigation/Sidebar";
import {Outlet} from "react-router-dom";


const PlayerPage = () => (
    <>
        <Sidebar/>
        <div className="d-flex flex-column" id="content-wrapper">
            <div id="content" style={{display: 'flex'}}>
                <main className="container-fluid mt-4">
                    <Outlet/>
                </main>
            </div>
        </div>
    </>
)

export default PlayerPage;