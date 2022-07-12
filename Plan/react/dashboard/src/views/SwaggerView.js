import React, {useEffect} from "react";
import SwaggerUI from "swagger-ui"
import "swagger-ui/dist/swagger-ui.css"

import {baseAddress} from "../service/backendConfiguration"

const SwaggerView = () => {

    useEffect(() => {
        SwaggerUI({
            dom_id: "#swagger-ui",
            url: baseAddress + "/docs/swagger.json"
        });
    }, []);

    return (
        <main id="swagger-ui" className="col-12"></main>
    )
}

export default SwaggerView;