import React from 'react';
import Sidebar from "../../components/navigation/Sidebar";
import Header from "../../components/navigation/Header";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faBug} from "@fortawesome/free-solid-svg-icons";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchErrorLogs} from "../../service/metadataService";
import ErrorPage from "./ErrorPage";
import ErrorsAccordion from "../../components/accordion/ErrorsAccordion";
import {Card} from "react-bootstrap";
import {useAuth} from "../../hooks/authenticationHook.tsx";

const ErrorsPage = () => {
    const {hasPermission} = useAuth();
    const seeErrors = hasPermission('access.errors');
    const {data, loadingError} = useDataRequest(fetchErrorLogs, [], seeErrors);

    if (loadingError) return <ErrorPage error={loadingError}/>;

    return (
        <>
            <Sidebar items={[]}/>
            {seeErrors && <div className="d-flex flex-column" id="content-wrapper">
                <Header page={<><FontAwesomeIcon icon={faBug}/> Error Logs</>} hideUpdater/>
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        <Card>
                            <ErrorsAccordion errors={data}/>
                        </Card>
                    </main>
                    <aside>
                        <ColorSelectorModal/>
                    </aside>
                </div>
            </div>}
        </>
    )
};

export default ErrorsPage