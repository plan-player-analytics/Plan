import React from "react";
import "swagger-ui-dist/swagger-ui.css"

import {baseAddress} from "../service/backendConfiguration"
import {useAuth} from "../hooks/authenticationHook.tsx";
import {SwaggerUIBundle} from "swagger-ui-dist";

const SwaggerView = () => {
    const {hasPermission} = useAuth();
    const seeDocs = hasPermission('access.docs');

    return (
        <>
            {seeDocs && <main ref={(element) => {
                if (seeDocs) {
                    SwaggerUIBundle({
                        domNode: element,
                        url: baseAddress + "/docs/swagger.json"
                    });
                }
            }} id="swagger-ui" className="col-12"></main>}
        </>
    )
}

export default SwaggerView;