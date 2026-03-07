import React, {CSSProperties, PropsWithChildren, RefObject, useEffect, useRef, useState} from 'react';
import {Transition, TransitionStatus} from 'react-transition-group';

const defaultDuration = 250;

type Props = {
    duration?: number;
} & PropsWithChildren;

type Animation = {
    [key in TransitionStatus]: CSSProperties;
};

const LoadIn = ({children, duration}: Props) => {
    const nodeRef: RefObject<HTMLDivElement | null> = useRef(null);

    if (!duration) duration = defaultDuration;
    const reduceAnimations = window.matchMedia(`(prefers-reduced-motion: reduce)`).matches;

    const defaultStyle = reduceAnimations ? {
        transition: `opacity ${duration}ms ease-in-out`,
        opacity: 0
    } : {
        transition: `opacity ${duration}ms ease-in-out, transform ${duration}ms ease-in-out`,
        opacity: 0,
        transform: "scale(0.99)"
    }

    const transitionStyles: Animation = reduceAnimations ? {
        entering: {
            opacity: 1,
        },
        entered: {
            opacity: 1,
        },
        exited: {
            opacity: 0,
        },
        exiting: {
            opacity: 0,
        },
        unmounted: {}
    } : {
        entering: {
            opacity: 1,
            transform: "scale(1)"
        },
        entered: {
            opacity: 1,
            transform: "scale(1)"
        },
        exiting: {
            opacity: 0,
            transform: "scale(0.99)"
        },
        exited: {
            opacity: 0,
            transform: "scale(0.99)"
        },
        unmounted: {}
    };

    const [visible, setVisible] = useState(false);

    useEffect(() => {
        setTimeout(() => setVisible(true), 0);
        return () => {
            setVisible(false);
        }
    }, [setVisible])

    return (
        <Transition in={visible} timeout={duration} nodeRef={nodeRef}>
            {state => (
                <div ref={nodeRef} className={"load-in"} style={{
                    ...defaultStyle,
                    ...transitionStyles[state]
                }}>
                    {children}
                </div>
            )}
        </Transition>
    );
}

export default LoadIn