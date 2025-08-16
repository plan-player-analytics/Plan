import {useCallback, useEffect, useState} from 'react';

export const useWindowWidth = () => {
    const [windowWidth, setWindowWidth] = useState(window.innerWidth);
    const updateWidth = useCallback(() => setWindowWidth(window.innerWidth), []);
    useEffect(() => {
        window.addEventListener('resize', updateWidth);
        return () => window.removeEventListener('resize', updateWidth);
    }, [updateWidth]);
    return windowWidth;
}