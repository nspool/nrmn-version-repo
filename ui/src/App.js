import React from 'react';
import clsx from 'clsx';
import {
  ThemeProvider,
  createMuiTheme,
  responsiveFontSizes,
  makeStyles
} from '@material-ui/core/styles';
import CssBaseline from '@material-ui/core/CssBaseline';
import TopBar from './components/layout/TopBar';
import SideMenu from './components/layout/SideMenu';
import Login from './components/auth/login';
import { blueGrey, deepPurple } from '@material-ui/core/colors';
import {
  BrowserRouter as Router,
  Switch,
  Route,
} from 'react-router-dom';
import XlxsUpload from './components/import/XlxsUpload';
import ValidationPage from './components/import/ValidationJob';
import GenericForm from './components/data-entities/GenericForm';
import { useSelector } from 'react-redux';
import EntityList from './components/data-entities/EntityList';
import Alert from '@material-ui/lab/Alert';
import { getFullPath } from './components/utils/helpers';
import AlertTitle from '@material-ui/lab/AlertTitle';
import GenericDetailsView from './components/data-entities/GenericDetailsView';
import Homepage from './components/layout/Homepage';

const drawerWidth = process.env.REACT_APP_LEFT_DRAWER_WIDTH ?
    process.env.REACT_APP_LEFT_DRAWER_WIDTH : 180;

const useStyles = makeStyles((theme) => ({
  root: {
    display: 'flex',
  },
  mainContent: {
    marginTop: 50,
    flexGrow: 1,
    padding: theme.spacing(3),
    transition: theme.transitions.create('margin', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
    marginLeft: `-${drawerWidth}px`
  },
  contentShift: {
    transition: theme.transitions.create('margin', {
      easing: theme.transitions.easing.easeOut,
      duration: theme.transitions.duration.enteringScreen,
    }),
    marginLeft: 0,
  }
}));

const App = () => {
  const classes = useStyles();
  const themeState = useSelector(state => state.theme);
  const leftSideMenuIsOpen = useSelector(state => state.toggle.leftSideMenuIsOpen);

  let theme = createMuiTheme({
    palette: {
      text: {
        primary: themeState.themeType ? '#eee' : '#607d8b',
        secondary: themeState.themeType ? '#999' : '#555'
      },
      primary: blueGrey,
      secondary: {
        main: deepPurple[300]
      },
      type: themeState.themeType ? 'dark' : 'light',
    },
    props: {
      MuiTextField: {
        variant: 'outlined',
        margin: 'dense',
        notched: 'true',
      }
    },
    overrides: {
      MuiCssBaseline: {
        '@global': {
          '.ag-root-wrapper-body': {
            minHeight: 400
          }

        },
      },
    },
  });
  theme = responsiveFontSizes(theme);

  return (
    <div className={classes.root}>
      <ThemeProvider theme={theme}>
        <Router>
          <CssBaseline />
          <TopBar></TopBar>
          <SideMenu></SideMenu>
          <main
            className={clsx(classes.mainContent, {
              [classes.contentShift]: leftSideMenuIsOpen
            })}
          >
            <Switch>
              <Route path='/validation/:jobId' component={ValidationPage} />
              <Route path='/upload' component={XlxsUpload} />
              <Route path="/login" component={Login} />
              <Route path="/edit/:entityName/:id?" component={GenericForm} />
              <Route path="/view/:entityName/:id?" component={GenericDetailsView} />
              <Route path="/list/:entityName" component={EntityList} />
              <Route path="/notfound" render={(route) =>
                <Alert severity="error"  >
                  <AlertTitle>API Resource Not Found</AlertTitle>
                  {`The requested resource ${getFullPath(route.location)} is not available`}
                </Alert>}
              />
              <Route path={'/'} component={Homepage} />
              <Route render={(route) =>
                <Alert severity="error"  >
                  <AlertTitle>Path Not Found</AlertTitle>
                    The requested resource <strong>{getFullPath(route.location)}</strong> is not available
                  </Alert>}
              />
            </Switch>
          </main>
        </Router>
      </ThemeProvider>
    </div>
  );
};

export default App;