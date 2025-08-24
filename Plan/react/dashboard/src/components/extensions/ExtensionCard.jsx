import React, {useEffect, useState} from "react";
import {Card, Col} from "react-bootstrap";
import ExtensionIcon from "./ExtensionIcon";
import Datapoint from "../Datapoint";
import Masonry from 'masonry-layout'
import ExtensionTable from "./ExtensionTable";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import End from "../layout/End";
import {MinecraftChat} from "react-mcjsonchat";
import ColoredText from "../text/ColoredText";
import {Link} from "react-router";
import FormattedTime from "../text/FormattedTime.jsx";
import FormattedDate from "../text/FormattedDate.jsx";

export const ExtensionCardWrapper = ({extension, children}) => {
    const [windowWidth, setWindowWidth] = useState(window.innerWidth);

    const onResize = () => {
        setWindowWidth(window.innerWidth);
    }

    useEffect(() => {
        window.addEventListener("resize", onResize);
        return () => {
            window.removeEventListener("resize", onResize);
        }
    }, [])

    const wide = extension.wide;

    const baseWidth = windowWidth < 1000 ? 6 : 4;
    const width = (wide ? 2 : 1) * baseWidth;

    return <Col lg={width} md={width} className="extension-wrapper">{children}</Col>
}

const ExtensionTab = ({tab}) => {
    return (<>
        {tab.tabInformation.elementOrder.map((type, i) => {
            switch (type) {
                case "VALUES":
                    return <ExtensionValues key={i} tab={tab}/>
                case "TABLE":
                    return <ExtensionTables key={i} tab={tab}/>
                default:
                    return ''
            }
        })}
    </>);
}

export const ExtensionValueTableCell = ({data}) => {
    if (!data) return '-';

    const title = data.description.description;
    if (data.type === 'STRING') {
        return (<ColoredText text={data.value}/>);
    } else if (data.type === 'LINK') {
        return (<Link to={data.value?.link}><ColoredText text={data.value?.text}/></Link>);
    } else if (data.type === 'COMPONENT') {
        return (<MinecraftChat component={JSON.parse(data.value)}/>)
    } else if (data.type === 'TIME_MILLISECONDS') {
        return <FormattedTime timeMs={value}/>;
    } else if (data.type === 'DATE_YEAR') {
        return <FormattedDate date={value}/>;
    } else if (data.type === 'DATE_SECOND') {
        return <FormattedDate date={value} includeSeconds/>;
    } else {
        return (<span title={title}>{data.value}</span>);
    }
}

const ExtensionValue = ({data}) => {
    const color = data.description.icon.colorClass;
    const colorClass = color?.startsWith("col-") ? color : "col-" + color;
    const icon = [data.description.icon.familyClass, data.description.icon.iconName];
    const name = data.description.text;
    const title = data.description.description;
    if (data.type === 'STRING') {
        return (
            <p title={title}>
                {icon && <Fa icon={icon} className={colorClass}/>} {name}
                {<End><ColoredText text={data.value}/></End>}
            </p>
        );
    } else if (data.type === 'LINK') {
        return (
            <p title={title}>
                {icon && <Fa icon={icon} className={colorClass}/>} {name}
                {<End><Link to={data.value?.link}><ColoredText text={data.value?.text}/></Link></End>}
            </p>
        );
    } else if (data.type === 'COMPONENT') {
        return (<p title={title}>
            {icon && <Fa icon={icon} className={colorClass}/>} {name}
            <End><MinecraftChat component={JSON.parse(data.value)}/></End>
        </p>)
    } else if (data.type === 'BOOLEAN') {
        return <p title={title}>
            {icon && <Fa icon={icon} className={colorClass}/>} {name}
            <End>{t(data.value ? 'plugin.generic.yes' : 'plugin.generic.no')}</End>
        </p>;
    } else if (data.type === 'TIME_MILLISECONDS') {
        return <p title={title}>
            {icon && <Fa icon={icon} className={colorClass}/>} {name}
            <End><FormattedTime timeMs={data.value}/></End>
        </p>;
    } else if (data.type === 'DATE_YEAR') {
        return <p title={title}>
            {icon && <Fa icon={icon} className={colorClass}/>} {name}
            <End><FormattedDate date={data.value}/></End>
        </p>;
    } else if (data.type === 'DATE_SECOND') {
        return <p title={title}>
            {icon && <Fa icon={icon} className={colorClass}/>} {name}
            <End><FormattedDate date={data.value} includeSeconds/></End>
        </p>;
    } else {
        return (<Datapoint name={name}
                           title={title}
                           icon={icon}
                           color={color}
                           value={data.value}
        />);
    }
}

const ExtensionValues = ({tab}) => {
    return (
        <>
            {Boolean(tab.values.length) && <Card.Body>
                {tab.values.map((data, i) => {
                        return (<ExtensionValue key={i} data={data}/>);
                    }
                )}
            </Card.Body>}
        </>
    )
}

const ExtensionTables = ({tab}) => {
    return (<>
        {tab.tableData.map((table, i) => (
            <ExtensionTable key={i} table={table}/>
        ))}
    </>);
}

const ExtensionCard = ({extension}) => {
    const [openTabIndex, setOpenTabIndex] = useState(0);

    const toggleTabIndex = index => {
        setOpenTabIndex(index);

        requestAnimationFrame(() => {
            const masonryRow = document.getElementById('extension-masonry-row');
            const masonry = Masonry.data(masonryRow);
            if (masonry) {
                masonry.layout();
            }
        });
    }

    return <Card>
        <Card.Header>
            <h6>
                <ExtensionIcon
                    icon={extension.extensionInformation.icon}/> {extension.extensionInformation.pluginName}
            </h6>
        </Card.Header>
        <ul className="nav nav-tabs tab-nav-right" role="tablist">
            {extension.onlyGenericTab ? '' :
                extension.tabs.map((tab, i) => <li key={JSON.stringify(tab)} role="presentation"
                                                   className="nav-item col-text">
                    <button className={"nav-link col-text"
                        + (openTabIndex === i ? ' active' : '')} onClick={() => toggleTabIndex(i)}>
                        <ExtensionIcon icon={tab.tabInformation.icon}/> {tab.tabInformation.tabName}
                    </button>
                </li>)
            }
        </ul>
        {extension.tabs.map((tab, i) => openTabIndex === i ? <ExtensionTab tab={tab} key={i}/> : '')}
    </Card>
}

export default ExtensionCard