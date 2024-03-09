import React, {useEffect} from "react";
import SwaggerUI from "swagger-ui"
import "swagger-ui/dist/swagger-ui.css"

import {baseAddress} from "../service/backendConfiguration"
import {useAuth} from "../hooks/authenticationHook";

const SwaggerView = () => {
    const {hasPermission} = useAuth();
    const seeDocs = hasPermission('access.docs');

    useEffect(() => {
        if (seeDocs) {
            SwaggerUI({
                dom_id: "#swagger-ui",
                url: baseAddress + "/docs/swagger.json"
            });
        }
    }, [seeDocs]);

    return (
        <>
            {seeDocs && <main id="swagger-ui" className="col-12"></main>}
        </>
    )
}

export default SwaggerView;