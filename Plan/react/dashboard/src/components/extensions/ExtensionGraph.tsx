import {useExtensionGraph} from "../../dataHooks/graph/useExtensionGraph";
import {ErrorViewCard} from "../../views/ErrorView";
import {CardLoader} from "../navigation/Loader";
import {useExtensionGraphAsOptions} from "../../dataHooks/graph/options/useExtensionGraphAsOptions";
import LineGraph from "../graphs/LineGraph";

type Props = {
    graph: string;
    server: string;
    player?: string;
}

export const ExtensionGraph = (props: Props) => {
    const {data: graph, error} = useExtensionGraph(props);

    if (error) return <ErrorViewCard error={error}/>;
    if (!graph) return <CardLoader/>;

    const graphOptions = useExtensionGraphAsOptions(graph);

    return <LineGraph id={props.graph} options={graphOptions} tall/>
}