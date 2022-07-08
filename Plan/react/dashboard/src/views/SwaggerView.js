import React, {useEffect} from "react";
import SwaggerUI from "swagger-ui"

const SwaggerView = () => {

    useEffect(() => {
        SwaggerUI({
            dom_id: "#swagger-ui"
        });
    }, []);

    return (
        <main id="swagger-ui"></main>
    )
}

export default SwaggerView;