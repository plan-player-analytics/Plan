import React from 'react';
import Sidebar from "../../components/navigation/Sidebar";
import Header from "../../components/navigation/Header";
import ErrorView from "../ErrorView";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";

const ErrorPage = ({error}) => {

    return (
        <>
            <Sidebar page={'Error occurred'} items={[]}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <Header page={error.title ? error.title : 'Unexpected error occurred'} hideUpdater/>
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        <ErrorView error={error}/>
                    </main>
                    <aside>
                        <ColorSelectorModal/>
                    </aside>
                </div>
            </div>
        </>
    )
};

export default ErrorPage