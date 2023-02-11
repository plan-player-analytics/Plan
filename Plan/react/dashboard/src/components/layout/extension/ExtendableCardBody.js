import React, {useCallback, useEffect, useState} from 'react';
import {Card} from "react-bootstrap";
import {usePageExtension} from "../../../hooks/pageExtensionHook";

const ExtendableCardBody = ({id, className, children}) => {
    const [elementsBefore, setElementsBefore] = useState([]);
    const [elementsAfter, setElementsAfter] = useState([]);
    const {onRender, onUnmount} = usePageExtension();

    const render = useCallback(async () => {
        if (!onRender) return;
        setElementsBefore(await onRender(id, 'beforeElement'));
        setElementsAfter(await onRender(id, 'afterElement'));
    }, [setElementsBefore, setElementsAfter, id, onRender])
    useEffect(() => {
        render();

        return () => {
            if (!onUnmount) return;
            setElementsBefore([])
            setElementsAfter([])
            onUnmount(id, 'beforeElement');
            onUnmount(id, 'afterElement');
        }
    }, [setElementsBefore, setElementsAfter, id, onUnmount, render]);

    return (
        <>
            <div dangerouslySetInnerHTML={{__html: elementsBefore.join('')}}/>
            <Card.Body id={id} className={className ? "extendable " + className : "extendable"}>
                {children}
            </Card.Body>
            <div dangerouslySetInnerHTML={{__html: elementsAfter.join('')}}/>
        </>
    )
};

export default ExtendableCardBody