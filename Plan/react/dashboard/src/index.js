import React from 'react';
import ReactDOM from 'react-dom';
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

localeService.init().then(() => ReactDOM.render(
    <React.StrictMode>
        <App/>
    </React.StrictMode>,
    document.getElementById('root')
));