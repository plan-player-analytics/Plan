import {Card} from "react-bootstrap";
import React from "react";
import {CardLoader} from "../../navigation/Loader";
import ServerPie from "../../graphs/ServerPie";
import {faNetworkWired} from "@fortawesome/free-solid-svg-icons";
import CardHeader from "../CardHeader";
import {useDataRequest} from "../../../hooks/dataFetchHook";
import {fetchServerPie} from "../../../service/networkService";
import {ErrorViewCard} from "../../../views/ErrorView";
import {useThemeStorage} from "../../../hooks/context/themeContextHook.jsx";
import {nameToCssVariable} from "../../../util/colors.js";

const ServerPieCard = () => {
    const {data, loadingError} = useDataRequest(fetchServerPie, []);
    const {usedUseCases} = useThemeStorage()

    if (!data) return <CardLoader/>;
    if (loadingError) return <ErrorViewCard error={loadingError}/>;

    const series = data.server_pie_series_30d;
    const colors = usedUseCases?.graphs?.pie?.colors?.map(nameToCssVariable) || data.server_pie_colors;

    return (
        <Card>
            <CardHeader icon={faNetworkWired} color={'sessions'} label={'html.label.serverPlaytime30days'}/>
            <ServerPie series={series} colors={colors}/>
        </Card>
    )
}

export default ServerPieCard;