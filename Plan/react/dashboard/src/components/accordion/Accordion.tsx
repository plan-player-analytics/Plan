import React, { useState } from 'react';
import { useTheme } from '../../hooks/themeHook';

type AccordionSlice = {
    header: React.ReactNode;
    body: React.ReactNode;
    color: string;
    outline?: boolean;
};

type Props = {
    headers: React.ReactNode[];
    slices: AccordionSlice[];
    open?: boolean;
    style?: React.CSSProperties;
};

type SliceBodyProps = {
    i: number;
    open: boolean;
    slice: AccordionSlice;
    width: number;
};

type SliceHeaderProps = Omit<SliceBodyProps, 'width'> & {
    onClick: React.MouseEventHandler<HTMLTableRowElement>;
};

const SliceHeader = ({ i, open, onClick, slice }: SliceHeaderProps) => {
    let style = 'bg-' + slice.color + (slice.outline ? '-outline' : '');
    return (
        <tr
            id={'slice_h_' + i}
            aria-controls={'slice_t_' + i}
            aria-expanded={open ? 'true' : 'false'}
            className={'clickable collapsed ' + style}
            data-bs-target={'#slice_t_' + i}
            data-bs-toggle='collapse'
            onClick={onClick}>
            {slice.header}
        </tr>
    );
};

const SliceBody = ({ i, open, slice, width }: SliceBodyProps) => {
    if (!open) return <tr className={open ? 'open' : 'closed'} />;

    return (
        <tr
            className={'collapse' + (open ? ' show' : '')}
            data-bs-parent='#tableAccordion'
            id={'slice_t_' + i}>
            <td colSpan={width}>{slice.body}</td>
        </tr>
    );
};

const Slice = ({
    i,
    slice,
    open,
    onClick,
    width,
}: SliceBodyProps & SliceHeaderProps) => (
    <>
        <SliceHeader i={i} open={open} onClick={onClick} slice={slice} />
        <SliceBody i={i} open={open} slice={slice} width={width} />
    </>
);

const NoDataRow = ({ width }: { width: number }) => {
    const nLengthArray = Array.from(Array(width - 1).keys());
    return (
        <tr>
            <td>No Data</td>
            {nLengthArray.map((i) => (
                <td key={i}>-</td>
            ))}
        </tr>
    );
};

const Accordion = ({ headers, slices, open, style }: Props) => {
    const [openSlice, setOpenSlice] = useState(open ? 0 : -1);
    const { nightModeEnabled } = useTheme();

    const toggleSlice = (i: number) => {
        setOpenSlice(openSlice === i ? -1 : i);
    };

    const width = headers.length;
    return (
        <table
            className={
                'table accordion-striped' +
                (nightModeEnabled ? ' table-dark' : '')
            }
            id='tableAccordion'
            style={style}>
            <thead>
                <tr>
                    {headers.map((header, i) => (
                        <th key={i}>{header}</th>
                    ))}
                </tr>
            </thead>
            <tbody>
                {slices.length ? (
                    slices.map((slice, i) => (
                        <Slice
                            key={'slice-' + i}
                            i={i}
                            slice={slice}
                            width={width}
                            open={openSlice === i}
                            onClick={() => toggleSlice(i)}
                        />
                    ))
                ) : (
                    <NoDataRow width={width} />
                )}
            </tbody>
        </table>
    );
};

export default Accordion;
