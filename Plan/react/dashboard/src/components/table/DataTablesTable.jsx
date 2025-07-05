import React, {useCallback, useEffect, useState} from 'react';
import {useTheme} from "../../hooks/themeHook";
import {Card, Dropdown, InputGroup} from "react-bootstrap";
import Select from "../input/Select";
import SearchField from "../input/SearchField";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {
    faAngleLeft,
    faAngleRight,
    faAnglesLeft,
    faAnglesRight,
    faSort,
    faSortAsc,
    faSortDesc
} from "@fortawesome/free-solid-svg-icons";
import Toggle from "../input/Toggle";
import CollapseWithButton from "../layout/CollapseWithButton";
import {faMinusSquare, faPlusSquare} from "@fortawesome/free-regular-svg-icons";
import {Trans, useTranslation} from "react-i18next";
import {download, generateCsv, mkConfig} from "export-to-csv";

const PaginationOption = ({onClick, children, selected}) => (
    <li>
        <button className={"btn " + (selected ? "bg-theme" : '')} onClick={onClick}>{children}</button>
    </li>
)

const Pagination = ({page, maxPage, setPage}) => {
    const firstPage = () => setPage(0);
    const previousPage = () => setPage(Math.max(0, page - 1));
    const nextPage = () => setPage(Math.min(maxPage - 1, page + 1));
    const lastPage = () => setPage(maxPage - 1);

    const elements = [];
    elements.push(<PaginationOption key={"<<"} onClick={firstPage}><FontAwesomeIcon
        icon={faAnglesLeft}/></PaginationOption>)
    elements.push(<PaginationOption key={"<"} onClick={previousPage}><FontAwesomeIcon
        icon={faAngleLeft}/></PaginationOption>)
    const pagesStart = Math.max(1, page - 2);
    const pagesEnd = Math.min(maxPage, pagesStart + 7);
    for (let i = pagesStart; i <= pagesEnd; i++) {
        elements.push(<PaginationOption key={i} selected={page === i - 1}
                                        onClick={() => setPage(i - 1)}>{i}</PaginationOption>)
    }
    elements.push(<PaginationOption key={">"} onClick={nextPage}><FontAwesomeIcon
        icon={faAngleRight}/></PaginationOption>)
    elements.push(<PaginationOption key={">>"} onClick={lastPage}><FontAwesomeIcon
        icon={faAnglesRight}/></PaginationOption>)

    return (
        <ul className={"dataTables_paginate input-group pagination"}>
            {elements}
        </ul>
    )
}

const SortIcon = ({selected, reversed}) => {
    if (!selected) return <FontAwesomeIcon className={"opaque-text"} icon={faSort}/>;
    if (reversed) return <FontAwesomeIcon icon={faSortAsc}/>;
    return <FontAwesomeIcon icon={faSortDesc}/>;
}

const VisibleColumnsSelector = ({columns, visibleColumnIndexes, toggleColumn}) => {
    const {t} = useTranslation();
    return (
        <CollapseWithButton title={t('html.label.table.visibleColumns')} coverToggle>
            <Card style={{position: "absolute", zIndex: 3}}>
                <Card.Body>
                    <ul style={{listStyle: "none", paddingLeft: "0.25rem", margin: 0}}>
                        {columns.map((column, i) => {
                            return <li key={JSON.stringify(column.data)}>
                                <Toggle value={visibleColumnIndexes.includes(i)}
                                        onValueChange={() => toggleColumn(i)}>{column.title}</Toggle>
                            </li>
                        })}
                    </ul>
                </Card.Body>
            </Card>
        </CollapseWithButton>
    )
}

const ExportMenu = ({matchingData}) => {
    const {t} = useTranslation();
    const [generating, setGenerating] = useState(false);

    const hasData = matchingData.length > 0;

    const exportCSV = useCallback(async () => {
        setGenerating(true);

        const rows = matchingData.map(row => {
            const mapped = {};
            for (let entry of Object.entries(row)) {
                if (entry[1] === undefined || entry[1]["$$typeof"] === undefined) {
                    mapped[entry[0]] = entry[1];
                }
            }
            return mapped;
        })

        const csvConfig = mkConfig({
            useKeysAsHeaders: true,
            filename: "data-" + new Date().toISOString().replaceAll(":", '').substring(0, 17)
        });
        const csvOutput = generateCsv(csvConfig)(rows);
        download(csvConfig)(csvOutput);
        setGenerating(false)
    }, [matchingData, setGenerating])

    return (
        <>{hasData &&
            <Dropdown>
                <Dropdown.Toggle variant={""} id="dropdown-basic"
                                 style={{'--bs-btn-color': 'var(--color-forms-input-text)'}}>
                    {generating &&
                        <FontAwesomeIcon icon={"gear"} className={"fa-spin"} title={t('html.label.export')}/>}
                    {!generating && <FontAwesomeIcon icon={"file-export"} title={t('html.label.export')}/>}
                </Dropdown.Toggle>
                <Dropdown.Menu>
                    <Dropdown.Item onClick={exportCSV}><FontAwesomeIcon
                        icon={"file-export"}/> {t('html.label.export')} CSV</Dropdown.Item>
                </Dropdown.Menu>
            </Dropdown>
        }</>
    )
}

const DataTablesTable = ({id, rowKeyFunction, options, colorClass, expandComponent}) => {
    const {t} = useTranslation();
    const {nightModeEnabled} = useTheme();

    const columns = options.columns;
    const [sortBy, setSortBy] = useState(options.order[0][0] || 0);
    const [sortReversed, setSortReversed] = useState(options.order[0][1] === 'asc');
    const [visibleColumnIndexes, setVisibleColumnIndexes] = useState(columns.map((_, i) => i));
    const toggleColumn = useCallback(index => {
        const currentSortIndex = visibleColumnIndexes[sortBy];
        if (visibleColumnIndexes.includes(index)) {
            if (visibleColumnIndexes.length === 1) return;
            const newVisible = visibleColumnIndexes.filter(i => i !== index);
            newVisible.sort((a, b) => a - b);
            setVisibleColumnIndexes(newVisible);
            if (currentSortIndex === index) {
                setSortBy(0);
                setSortReversed(false);
            } else if (index <= currentSortIndex) {
                setSortBy(sortBy - 1);  // Keep the current sort
            }
        } else {
            const newVisible = [index, ...visibleColumnIndexes];
            newVisible.sort((a, b) => a - b);
            setVisibleColumnIndexes(newVisible);
            if (currentSortIndex >= index) {
                setSortBy(sortBy + 1);  // Keep the current sort
            }
        }
    }, [visibleColumnIndexes, setVisibleColumnIndexes, sortBy, setSortBy, setSortReversed]);
    const visibleColumns = visibleColumnIndexes.map(i => columns[i]);
    const invisibleColumns = columns.filter((c, i) => !visibleColumnIndexes.includes(i));

    const [selectedPaginationCount, setSelectedPaginationCount] = useState(options.paginationCount || 0)
    const paginationCountOptions = ["10", "25", "100"];
    const paginationCount = Number(paginationCountOptions[selectedPaginationCount]);

    const sortingFunction = (a, b) => {
        const key = visibleColumns[sortBy].data._ || visibleColumns[sortBy].data;
        const valA = a[key];
        const valB = b[key];
        if (valA === undefined && valB === undefined) return 0;
        if (valA === undefined) return sortReversed ? 1 : -1;
        if (valB === undefined) return sortReversed ? 1 : -1;
        const isNumberA = typeof valA === 'number' || !isNaN(valA);
        const isNumberB = typeof valB === 'number' || !isNaN(valB);
        if (isNumberA && isNumberB) {
            return sortReversed ? valA - valB : valB - valA;
        }
        return sortReversed ? valB.localeCompare(valA) : valA.localeCompare(valB);
    }
    const changeSort = index => {
        setSortBy(index);
        setSortReversed(index === sortBy && !sortReversed);
    }

    const keys = visibleColumns.flatMap(column => [column.data._ || column.data, column.data.display]);
    const [filter, setFilter] = useState('');
    const filterWords = filter.split(' ').filter(word => word);
    const matchingData = options.data.filter(row => {
        if (!filter) return true;

        return Boolean(keys.some(key => filterWords.some(word => String(row[key]).toLowerCase().includes(word))));
    });
    matchingData.sort(sortingFunction);

    const [expandedRows, setExpandedRows] = useState([]);
    const toggleRow = useCallback(i => {
        if (expandedRows.includes(i)) {
            setExpandedRows(expandedRows.filter(item => item !== i));
        } else {
            setExpandedRows([...expandedRows, i]);
        }
    }, [setExpandedRows, expandedRows]);
    useEffect(() => {
        if (visibleColumnIndexes.length === columns.length) {
            setExpandedRows([]);
        }
    }, [visibleColumnIndexes, columns, setExpandedRows])

    const [page, setPage] = useState(0);
    const maxPage = Math.ceil(matchingData.length / paginationCount);

    const rows = matchingData.slice(page * paginationCount, Math.min(page * paginationCount + paginationCount, options.data.length));

    useEffect(() => {
        setPage(0);
    }, [filter, paginationCount, sortBy, sortReversed]);

    if (!rowKeyFunction) {
        rowKeyFunction = (row, column) => {
            return JSON.stringify(row) + "-" + JSON.stringify(column?.data);
        }
    }

    // Overflow protection
    const onResize = useCallback(() => {
        const container = document.getElementById(id + "-container");
        const table = document.getElementById(id);
        if (!container || !table) return;
        const overflowing = table.scrollWidth > container.clientWidth;
        if (overflowing) toggleColumn(visibleColumnIndexes[visibleColumnIndexes.length - 1]);
    }, [id, toggleColumn, visibleColumnIndexes]);
    useEffect(() => {
        window.addEventListener('resize', onResize);
        return () => window.removeEventListener('resize', onResize);
    }, [onResize]);

    const someColumnsHidden = expandComponent || columns.length !== visibleColumns.length;

    return (
        <div id={id + "-container"}>
            <div className={"float-start"}>
                <InputGroup className={"dataTables_length"}>
                    <label className={"input-group-text"}>{t('html.label.table.showPerPage')}</label>
                    <Select options={paginationCountOptions} selectedIndex={selectedPaginationCount}
                            setSelectedIndex={setSelectedPaginationCount}/>
                </InputGroup>
            </div>
            <div className={"float-end"}>
                <SearchField className={"dataTables_filter"} value={filter} setValue={setFilter}/>
            </div>
            {columns.length > 2 && <div className={"float-start dataTables_columns"}>
                <VisibleColumnsSelector columns={columns} visibleColumnIndexes={visibleColumnIndexes}
                                        toggleColumn={toggleColumn}/>
            </div>}
            <div className={"float-end dataTables_columns"}>
                <ExportMenu matchingData={matchingData} columns={columns}/>
            </div>
            <table id={id}
                   className={"datatable table table-bordered table-striped" + (nightModeEnabled ? " table-dark" : '')}
                   style={{width: "100%"}}>
                <thead id={id + '-head'} className={colorClass}>
                <tr>
                    {visibleColumns.map((column, i) => <th key={JSON.stringify(column.data)}>
                        <button onClick={() => changeSort(i)}>
                            {column.title} <span className={"float-end"}>
                        <SortIcon selected={i === sortBy}
                                  reversed={sortReversed}/>
                        </span>
                        </button>
                    </th>)}
                </tr>
                </thead>
                <tbody id={id + '-body'}>
                {!rows.length && <tr>
                    {visibleColumns.map((column, i) => <td key={"col-" + rowKeyFunction(0, column)}>
                        {i === 0 && t('html.label.noDataToDisplay')}
                    </td>)}
                </tr>}
                {rows.map(row => <React.Fragment key={"frag-" + rowKeyFunction(row, null)}>
                    <tr key={"row-" + rowKeyFunction(row, null)}>
                        {visibleColumns.map((column, i) => {
                            if (column.data._ !== undefined) {
                                return <td key={"col-" + rowKeyFunction(row, column)}>
                                    {i === 0 && someColumnsHidden &&
                                        <button style={{paddingRight: "0.5rem"}}
                                                onClick={() => toggleRow(rowKeyFunction(row, null))}>
                                            <FontAwesomeIcon
                                                icon={expandedRows.includes(rowKeyFunction(row, null)) ? faMinusSquare : faPlusSquare}
                                                className={"col-green"}/>
                                        </button>}
                                    {row[column.data.display]}
                                </td>
                            } else {
                                return <td key={"col-" + rowKeyFunction(row, column)}>
                                    {i === 0 && someColumnsHidden &&
                                        <button style={{paddingRight: "0.5rem"}}
                                                onClick={() => toggleRow(rowKeyFunction(row, null))}>
                                            <FontAwesomeIcon
                                                icon={expandedRows.includes(rowKeyFunction(row, null)) ? faMinusSquare : faPlusSquare}
                                                className={"col-green"}/>
                                        </button>}
                                    {row[column.data]}
                                </td>
                            }
                        })}
                    </tr>
                    {expandedRows.includes(rowKeyFunction(row, null)) &&
                        <tr key={"hidden-row" + rowKeyFunction(row, null)}>
                            <td colSpan={visibleColumns.length}>
                                {expandComponent && expandComponent({row})}
                                {invisibleColumns.map(column => {
                                    if (column.data._ !== undefined) {
                                        return <p key={"p-" + rowKeyFunction(row, column)}>
                                            <b>{column.title}:</b> {row[column.data.display]}</p>
                                    } else {
                                        return <p key={"p-" + rowKeyFunction(row, column)}>
                                            <b>{column.title}:</b> {row[column.data]}</p>
                                    }
                                })}
                            </td>
                        </tr>}
                </React.Fragment>)}
                </tbody>
            </table>
            <p className={"dataTables_info float-start"}
               style={{maxWidth: "40%", textOverflow: "ellipsis", whiteSpace: "nowrap"}}>
                <Trans i18nKey={"html.label.table.showNofM"}
                       defaults={"Showing {{n}} of {{m}} entries"}
                       values={{
                           n: `${page * paginationCount + 1}-${page * paginationCount + rows.length}`,
                           m: matchingData.length
                       }}/>
            </p>
            <div className={"float-end"} style={{maxWidth: "60%"}}>
                <Pagination page={page} setPage={setPage} maxPage={maxPage}/>
            </div>
        </div>
    )
};

export default DataTablesTable