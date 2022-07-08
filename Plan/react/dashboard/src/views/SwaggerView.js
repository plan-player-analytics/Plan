import React, {useEffect} from "react";
import SwaggerUI from "swagger-ui"

const SwaggerView = () => {

    useEffect(() => {
        SwaggerUI({
            dom_id: "#swagger-ui"
        });
    }, [SwaggerUI]);

    return (
        <main id="swagger-ui"></main>
    )
}

export default SwaggerView;