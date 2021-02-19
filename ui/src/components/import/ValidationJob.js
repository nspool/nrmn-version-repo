import {Box, Chip, CircularProgress, Fab, Grid, Typography} from '@material-ui/core';
import React, {useEffect} from 'react';
import {useDispatch, useSelector} from 'react-redux';
import DataSheetView from './DataSheetView';
import PlaylistAddCheckOutlinedIcon from '@material-ui/icons/PlaylistAddCheckOutlined';
import {makeStyles} from '@material-ui/core/styles';
import {Redirect, useParams} from 'react-router';
import {RowUpdateRequested, JobRequested, ResetState, SubmitingestRequested, ValidationRequested} from './reducers/create-import';
import CloudUploadIcon from '@material-ui/icons/CloudUpload';
import AccountBalanceOutlinedIcon from '@material-ui/icons/AccountBalanceOutlined';
import {Backdrop} from '@material-ui/core';
import SaveOutlinedIcon from '@material-ui/icons/SaveOutlined';
import ValidationDrawer from './ValidationDrawer';

const useStyles = makeStyles((theme) => {
  return {
    root: {
      display: 'flex',
      '& > *': {
        margin: theme.spacing(1)
      }
    },
    hide: {
      display: 'none'
    },
    extendedIcon: {
      marginRight: theme.spacing(1)
    }
  };
});

const ValidationJob = () => {
  const {jobId} = useParams();
  const dispatch = useDispatch();
  const classes = useStyles();
  const job = useSelector((state) => state.import.job);
  const indexMap = useSelector((state) => state.import.indexChanged);
  const isLoading = useSelector((state) => state.import.isLoading);
  const editLoading = useSelector((state) => state.import.editLoading);
  const enableSubmit = useSelector((state) => state.import.enableSubmit);
  const validationLoading = useSelector((state) => state.import.validationLoading);
  const submitLoading = useSelector((state) => state.import.submitLoading);
  const ingestSuccess = useSelector((state) => state.import.ingestSuccess);

  const handleValidate = () => {
    if (job.id) {
      dispatch(ValidationRequested(job.id));
    }
  };

  const handleSubmit = () => {
    if (job.id) {
      dispatch(SubmitingestRequested(job.id));
    }
  };

  const handleSave = () => {
    dispatch(RowUpdateRequested({jobId: jobId, rows: indexMap}));
  };

  useEffect(() => {
    if (jobId) {
      dispatch(JobRequested(jobId));
    }
    return function clean() {
      dispatch(ResetState());
    };
  }, []);

  if (ingestSuccess) {
    return <Redirect to="/"></Redirect>;
  }
  const enableSaved = Object.keys(indexMap || {}).length > 0;
  const jobReady = job && Object.keys(job).length > 0;
  return jobReady ? (
    <Box style={{paddingRight: 60}}>
      <ValidationDrawer></ValidationDrawer>
      <Grid container>
        <Grid item lg={8} md={8}>
          <Typography variant="h4" color="primary">
            {job.reference}
          </Typography>
        </Grid>
        <Grid item lg={4} md={4}>
          <Grid container justify="space-between" spacing={1}>
            <Grid item>
              <Fab onClick={handleSave} disabled={!enableSaved} variant="extended" size="small" color="primary">
                <SaveOutlinedIcon className={classes.extendedIcon} />
                Save
              </Fab>
            </Grid>
            <Grid item>
              <Fab
                variant="extended"
                disabled={editLoading || isLoading}
                onClick={() => handleValidate()}
                size="small"
                label="Validate"
                color="secondary"
              >
                <PlaylistAddCheckOutlinedIcon className={classes.extendedIcon} />
                Validate
              </Fab>
            </Grid>
            <Grid item>
              <Fab variant="extended" size="small" onClick={handleSubmit} label="Submit" disabled={!enableSubmit} color="primary">
                <CloudUploadIcon className={classes.extendedIcon} />
                Submit
              </Fab>
            </Grid>
          </Grid>
        </Grid>
      </Grid>
      <Grid container spacing={1} justify="flex-start">
        <Grid item>
          <Chip
            size="small"
            avatar={<AccountBalanceOutlinedIcon></AccountBalanceOutlinedIcon>}
            label={job.program.programName}
            variant="outlined"
            mt={1}
          ></Chip>
        </Grid>
        <Grid item>
          <Chip size="small" color="secondary" label={job.source} variant="outlined"></Chip>
        </Grid>
        {job.isExtendedSize && (
          <Grid item>
            <Chip size="small" color="secondary" label={'Extended Size'} variant="outlined"></Chip>
          </Grid>
        )}
      </Grid>
      <DataSheetView></DataSheetView>
      {submitLoading && (
        <Backdrop open={submitLoading}>
          <Typography variant="h2">Ingesting...</Typography>
          <CircularProgress size={200} style={{color: '#ccc'}}></CircularProgress>
        </Backdrop>
      )}
      {isLoading && (
        <Backdrop open={isLoading}>
          <CircularProgress size={200} style={{color: '#ccc'}}></CircularProgress>
        </Backdrop>
      )}
      {validationLoading && (
        <Backdrop open={validationLoading}>
          <CircularProgress size={200} style={{color: '#ccc'}}></CircularProgress>
        </Backdrop>
      )}
    </Box>
  ) : (
    <Box>
      <Typography>No Data</Typography>
    </Box>
  );
};

export default ValidationJob;
