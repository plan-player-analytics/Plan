// NodeJS library workaround where global is used instead of window.
(globalThis as any).global ||= globalThis;

import React from 'react';
import {createRoot} from "react-dom/client";
import './index.css';
import App from './App';

import '@fortawesome/fontawesome-free/css/all.min.css'

import {library} from '@fortawesome/fontawesome-svg-core';
import {fas} from '@fortawesome/free-solid-svg-icons';
import {far} from '@fortawesome/free-regular-svg-icons';
import {fab} from '@fortawesome/free-brands-svg-icons';

import {localeService} from "./service/localeService";

library.add(fab);
library.add(fas);
library.add(far);

localeService.init().then(() => {
    const container = document.getElementById('root')!;
    const root = createRoot(container, {
        onUncaughtError: (error, errorInfo) => {
            console.error(error, errorInfo);
            // ... log error report
        },
        onCaughtError: (error, errorInfo) => {
            console.error(error, errorInfo);
            // ... log error report
        }
    });
    root.render(
        <React.StrictMode>
            <App/>
        </React.StrictMode>
    );
});