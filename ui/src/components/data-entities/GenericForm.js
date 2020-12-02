import React from "react";

import Form from "@rjsf/material-ui"
import {useDispatch, useSelector} from "react-redux";
import { Link } from 'react-router-dom'
import {useEffect} from 'react';
import {useParams, Redirect} from "react-router-dom";
import NestedApiField from './customWidgetFields/NestedApiField';
import pluralize from 'pluralize';
import config from "react-global-configuration";
import {Box} from "@material-ui/core";
import Alert from "@material-ui/lab/Alert";
import Paper from "@material-ui/core/Paper";
import Grid from "@material-ui/core/Grid";
import {titleCase} from "title-case";
import {LoadingBanner} from "../layout/loadingBanner";
import {createEntityRequested, itemRequested, updateEntityRequested} from "./middleware/entities";
import Typography from "@material-ui/core/Typography";
import Button from "@material-ui/core/Button";
import _ from 'lodash';

const renderError = (msgArray) => {
  return (msgArray.length > 0) ? <><Box><Alert severity="error" variant="filled">{msgArray}</Alert></Box></> : <></>;
}

const GenericForm = () => {

  const {entityName, id} = useParams();
  const schemaDefinition = config.get('api') || {};

  const editItem = useSelector(state => state.form.editItem);
  const entitySaved = useSelector(state => state.form.entitySaved);
  const errors = useSelector(state => state.form.errors);

  const dispatch = useDispatch();
  const singular = pluralize.singular(entityName);
  const entityTitle = singular.charAt(0).toUpperCase() + singular.slice(1)


  useEffect(() => {
    if (id !== undefined) {
      dispatch(itemRequested(entityName + "/" + id));
    }
  }, [entitySaved]);

  if (Object.keys(schemaDefinition).length === 0) {
    return renderError("ERROR: API Schema not found");
  }
  if ( typeof (schemaDefinition[entityTitle]) == 'undefined') {
    return renderError("ERROR: Entity '" + entityTitle + "' missing from API Schema");
  }

  const handleSubmit = (form) => {
    const data = {path: entityName, id: id, data: form.formData};
    (id) ?
      dispatch(updateEntityRequested(data)) :
      dispatch(createEntityRequested(data)) ;
  }

  const entityDef = schemaDefinition[entityTitle];

  let fullTitle = (id) ?  "Edit " + entityTitle + " '" + id + "'" : "Add '" + entityTitle + "'" ;
  const entitySchema = {title: fullTitle, ...entityDef}
  const JSSchema = {components: {schemas: schemaDefinition}, ...entitySchema};

  const uiSchemaHacks = Object.keys(entitySchema.properties).filter( key => {
    return entitySchema.properties[key].type === "string" && entitySchema.properties[key].format === "uri"
  } )
  const uiSchema = {};
  uiSchemaHacks.map( key => {
    uiSchema[key] = {'ui:field': "relationship"}
  });

  const fields = {
    relationship: NestedApiField
  }

  const formContent = ()=>{
    if (entitySaved && Object.keys(entitySaved).length === 0) {
      return <Form
          schema={JSSchema}
          uiSchema={uiSchema}
          onSubmit={handleSubmit}
          fields={fields}
          formData={editItem}
      />
    }
    else {
      const redirectPath = "/list/" + entityTitle;
      return <>
        <Typography variant="h4" >Entity saved successfully!</Typography>
        <ul>
          {
            entitySaved.map(ent => {
              return <li key={ _.uniqueId('entitySaved-')}>{ent.config.url}</li>
            })
          }
        </ul>
        <Button
            component={Link}
            to={redirectPath}
            color="secondary"
            aria-label={"List " + entityTitle}
            variant={"contained"}>
          List {entityName}
        </Button>
        </>

    }
  }

  if (errors.length > 0) {
    return renderError(errors)
  }
  else {
    if (schemaDefinition[entityTitle] === undefined) {
      return renderError(["Entity '" + entityName + "' cannot be found"]);
    }
    else {
      return (id && Object.keys(editItem).length === 0) ?
          <LoadingBanner variant={"h5"} msg={"Loading '" + titleCase(entityName) + "' form"  } /> :
          <Grid
              container
              spacing={0}
              alignItems="center"
              justify="center"
              style={{minHeight: "70vh"}}
          >
            <Paper>
              <Box mx="auto" bgcolor="background.paper" pt={2} px={3} pb={3}>
                {formContent()}
              </Box>
            </Paper>
          </Grid>
    }
  }
}

export default GenericForm;
