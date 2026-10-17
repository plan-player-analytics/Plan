import {useExtensionGraph} from "../../dataHooks/graph/useExtensionGraph";
import {ErrorViewBody} from "../../views/ErrorView";
import {ChartLoader} from "../navigation/Loader";
import {useExtensionGraphAsOptions} from "../../dataHooks/graph/options/useExtensionGraphAsOptions";
import LineGraph from "../graphs/LineGraph";
import {ExtensionGraph as DataType} from "../../dataHooks/model/extension/ExtensionGraph";
import FunctionPlotGraph from "../graphs/FunctionPlotGraph";

type Props = {
    graph: string;
    server: string;
    player?: string;
}

export const ExtensionGraph = (props: Props) => {
    const {data: graph, error} = useExtensionGraph(props);

    if (error) return <ErrorViewBody error={error}/>;
    if (!graph) return <ChartLoader/>;

    return <Inner graphName={props.graph} graph={graph}/>;
}

type InnerProps = {
    graphName: string;
    graph: DataType;
}

const Inner = ({graphName, graph}: InnerProps) => {
    const graphOptions = useExtensionGraphAsOptions(graph);

    const dateXAxis = graph.xAxisType === "DATE_MILLIS";

    return dateXAxis
        ? <LineGraph
            id={graphName} options={graphOptions}
            alreadyOffsetTimezone={undefined}
            extraOptions={undefined}
            extremes={undefined}
            legendEnabled={undefined}
            onMouseLeave={undefined}
            onSetExtremes={undefined}
            selectedRange={undefined}
            series={undefined}
            yAxis={undefined}
            tall={false}
        />
        : <FunctionPlotGraph
            id={graphName} options={graphOptions}
            series={undefined}
            legendEnabled={undefined}
            yPlotLines={undefined}
            yPlotBands={undefined}
            xPlotLines={undefined}
            xPlotBands={undefined}
            tall={false}/>
}