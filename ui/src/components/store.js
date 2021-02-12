import {configureStore, getDefaultMiddleware} from '@reduxjs/toolkit';

import {themeReducer} from './layout/theme-reducer';
import {toggleReducer} from './layout/layout-reducer';
import {importReducer} from './import/reducers/create-import';
import {uploadReducer} from './import/reducers/upload';
import {authReducer} from './auth/auth-reducer';
import {formReducer} from './data-entities/form-reducer';
import createSagaMiddleware from 'redux-saga';
import importMiddleware from './import/middleware/create-import';
import FileMiddleware from './import/middleware/validation-job';
import getEntitiesWatcher from './data-entities/middleware/entities';
import {all} from 'redux-saga/effects';
import LoginWatcher from './auth/auth-middleware';
import {jobReducer} from './job/jobReducer';
import jobWatcher from './job/jobMiddleware';

const initialiseSagaMiddleware = createSagaMiddleware();

const middleware = [...getDefaultMiddleware(), initialiseSagaMiddleware];

const store = configureStore({
  reducer: {
    theme: themeReducer,
    auth: authReducer,
    toggle: toggleReducer,
    import: importReducer,
    form: formReducer,
    job: jobReducer,
    upload: uploadReducer
  },
  middleware
});

function* rootSaga() {
  yield all([importMiddleware(), FileMiddleware(), LoginWatcher(), getEntitiesWatcher(), jobWatcher()]);
}

initialiseSagaMiddleware.run(rootSaga);

export default store;
