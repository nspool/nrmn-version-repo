import React from 'react';
import Box from "@material-ui/core/Box";
import Alert from '@material-ui/lab/Alert';
import Grid from "@material-ui/core/Grid";
import Paper from "@material-ui/core/Paper";
import Form from "@rjsf/material-ui"
import makeStyles from "@material-ui/core/styles/makeStyles";
import CircularProgress from "@material-ui/core/CircularProgress";
import Button from "@material-ui/core/Button";
import green from "@material-ui/core/colors/green";



const useStyles = makeStyles((theme) => ({
  root: {
    '& > * + *': {
      marginTop: theme.spacing(2)
    },
  },
  rootFlex: {
    display: 'flex',
    alignItems: 'center'
  },
  wrapper: {
    marginTop: theme.spacing(3),
    position: 'relative',
  },
  buttonSuccess: {
    backgroundColor: green[500],
    '&:hover': {
      backgroundColor: green[700],
    },
  },
  buttonProgress: {
    position: 'absolute',
    top: '50%',
    left: '50%',
    marginTop: -12,
    marginLeft: -12,
  }
}));

const BaseForm = (params) => {

  const classes = useStyles();

  const loading = params.loading;

  function submitForm(formData) {
    params.onSubmit(formData);
  }

  function getErrors(errors) {
    return errors.map((  item, key) => {
      return <div key={key}>{item}</div>
    })
  }

  let errorAlert = params.errors ? <Alert  severity="error" variant="filled" >{getErrors(params.errors)}
  </Alert> : "";

  return <>
    <Grid
        container
        spacing={0}
        alignItems="center"
        justify="center"
        style={{ minHeight: "70vh" }}
    >
      <Paper elevation={0}>
        <Box pt={4} px={6} pb={6} className={classes.root} >
          <Form
              schema={params.schema}
              uiSchema={params.uiSchema}
              onSubmit={submitForm}
              gutterBottom={true}
              showErrorList={true}
          >
            {errorAlert}
            <div className={classes.rootFlex}>
              <div className={classes.wrapper}>
                <Button type="submit"
                        variant="contained"
                        color="primary"
                        disabled={loading}
                >Submit</Button>
                {loading && <CircularProgress size={20} className={classes.buttonProgress} />}
              </div>
            </div>
          </Form>
        </Box>
      </Paper>
    </Grid>
  </>;
}

export default BaseForm;
