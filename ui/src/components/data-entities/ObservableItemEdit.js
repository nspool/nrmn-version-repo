import React, {useEffect, useReducer, useState} from 'react';
import {useParams, NavLink, Redirect} from 'react-router-dom';
import {Box, Button, CircularProgress, Divider, Grid, Typography} from '@material-ui/core';
import {Save, Delete} from '@material-ui/icons';
import Alert from '@material-ui/lab/Alert';
import PropTypes from 'prop-types';

import EntityContainer from '../containers/EntityContainer';

import CustomAutoCompleteInput from '../input/CustomAutoCompleteInput';
import CustomTextInput from '../input/CustomTextInput';
import CustomSearchInput from '../input/CustomSearchInput';

import {getResult, getObservableItemEdit, entityEdit, entityDelete} from '../../axios/api';

const ObservableItemEdit = () => {
  const observableItemId = useParams()?.id;

  const [saved, setSaved] = useState(false);
  const [errors, setErrors] = useState([]);
  const [options, setOptions] = useState({});

  const formReducer = (state, action) => {
    if (action.form) return {...state, ...action.form};
    switch (action.field) {
      default:
        return {...state, [action.field]: action.value};
    }
  };

  const [item, dispatch] = useReducer(formReducer, {
    observableItemName: '',
    commonName: '',
    speciesEpithet: '',
    supersededBy: '',
    letterCode: '',
    reportGroup: '',
    habitatGroup: '',
    phylum: '',
    class: '',
    order: '',
    family: '',
    genus: '',
    lengthWeightA: '',
    lengthWeightB: '',
    lengthWeightCf: ''
  });

  useEffect(() => getObservableItemEdit().then((options) => setOptions(options)), []);

  useEffect(() => {
    if (observableItemId) getResult(`reference/observableItem/${observableItemId}`).then((res) => dispatch({form: res.data}));
  }, [observableItemId]);

  const handleSubmit = () => {
    entityEdit(`reference/observableItem/${observableItemId}`, item).then((res) => {
      if (res.data.observableItemId) {
        setSaved(res.data);
      } else {
        setErrors(res.data.errors);
      }
    });
  };

  const handleDelete = () => {
    entityDelete(`reference/observableItem`, observableItemId).then((res) => {
      if (res.data.error) {
        setErrors([{banner: 'Unable to delete. Observable Item has linked observations.'}]);
      } else {
        setSaved({observableItemId: '-1'});
      }
    });
  };

  if (saved) {
    const id = saved['observableItemId'];
    return <Redirect to={`/reference/observableItem/${id}/saved`} />;
  }

  return (
    <EntityContainer name="Observable Items" goBackTo="/reference/observableItems">
      <Grid container alignItems="flex-start" direction="row">
        <Grid item xs={10}>
          <Box fontWeight="fontWeightBold">
            <Typography variant="h4">Edit Observable Item</Typography>
          </Box>
        </Grid>
        <Button style={{ float: 'right'}} onClick={handleDelete} startIcon={<Delete></Delete>}>
          Delete
        </Button>
      </Grid>
      <Grid container direction="column" justify="flex-start" alignItems="center">
        {observableItemId && Object.keys(item).length === 0 ? (
          <CircularProgress size={20} />
        ) : (
          <Box pt={2} pb={6} padding={2} width="90%">
            {errors.length > 0 ? (
              <Box py={2}>
                <Alert severity="error" variant="filled">
                  {errors[0]?.banner ? errors[0].banner : 'Please review this form for errors and try again.'}
                </Alert>
              </Box>
            ) : null}
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <CustomTextInput
                  label="Species Name"
                  formData={item.observableItemName}
                  field="observableItemName"
                  errors={errors}
                  onChange={(t) => dispatch({field: 'observableItemName', value: t})}
                />
              </Grid>
              <Grid item xs={6}>
                <CustomTextInput
                  label="Common Name"
                  formData={item.commonName}
                  field="commonName"
                  errors={errors}
                  onChange={(t) => dispatch({field: 'commonName', value: t})}
                />
              </Grid>
              <Grid item xs={6}>
                <CustomAutoCompleteInput
                  label="Species Epithet"
                  options={options.speciesEpithet}
                  formData={item.speciesEpithet}
                  field="speciesEpithet"
                  errors={errors}
                  onChange={(t) => dispatch({field: 'speciesEpithet', value: t})}
                />
              </Grid>
              <Grid item xs={6}>
                <CustomSearchInput
                  label="Superseded By"
                  formData={item.supersededBy}
                  field="supersededBy"
                  errors={errors}
                  onChange={(t) => dispatch({field: 'supersededBy', value: t})}
                />
              </Grid>
              <Grid item xs={6}>
                <CustomTextInput
                  label="Letter Code"
                  formData={item.letterCode}
                  field="letterCode"
                  errors={errors}
                  onChange={(t) => dispatch({field: 'letterCode', value: t})}
                />
              </Grid>
              <Grid item xs={6}>
                <CustomAutoCompleteInput
                  label="Report Group"
                  formData={item.reportGroup}
                  options={options.reportGroups}
                  field="reportGroup"
                  errors={errors}
                  onChange={(t) => dispatch({field: 'reportGroup', value: t})}
                />
              </Grid>
              <Grid item xs={6}>
                <CustomAutoCompleteInput
                  label="Habitat Group"
                  formData={item.habitatGroup}
                  options={options.habitatGroups}
                  field="habitatGroup"
                  errors={errors}
                  onChange={(t) => dispatch({field: 'habitatGroup', value: t})}
                />
              </Grid>
              <Grid item xs={12}>
                <Divider />
              </Grid>
              <Grid item xs={6}>
                <CustomAutoCompleteInput
                  label="Phylum"
                  formData={item.phylum}
                  options={options.phylum}
                  field="phylum"
                  errors={errors}
                  onChange={(t) => dispatch({field: 'phylum', value: t})}
                />
              </Grid>
              <Grid item xs={6}>
                <CustomAutoCompleteInput
                  label="Class"
                  formData={item.class}
                  options={options.className}
                  field="class"
                  errors={errors}
                  onChange={(t) => dispatch({field: 'class', value: t})}
                />
              </Grid>
              <Grid item xs={6}>
                <CustomAutoCompleteInput
                  label="Order"
                  formData={item.order}
                  options={options.order}
                  field="order"
                  errors={errors}
                  onChange={(t) => dispatch({field: 'order', value: t})}
                />
              </Grid>
              <Grid item xs={6}>
                <CustomAutoCompleteInput
                  label="Family"
                  formData={item.family}
                  options={options.family}
                  field="family"
                  errors={errors}
                  onChange={(t) => dispatch({field: 'family', value: t})}
                />
              </Grid>
              <Grid item xs={6}>
                <CustomAutoCompleteInput
                  label="Genus"
                  formData={item.genus}
                  options={options.genus}
                  field="genus"
                  errors={errors}
                  onChange={(t) => dispatch({field: 'genus', value: t})}
                />
              </Grid>
              <Grid item xs={12}>
                <Divider />
              </Grid>
              <Grid item xs={6}>
                <CustomTextInput
                  type="number"
                  label="Length-Weight a"
                  formData={item.lengthWeightA}
                  field="lengthWeightA"
                  errors={errors}
                  onChange={(t) => dispatch({field: 'lengthWeightA', value: t})}
                />
              </Grid>
              <Grid item xs={6}>
                <CustomTextInput
                  type="number"
                  label="Length-Weight b"
                  formData={item.lengthWeightB}
                  field="lengthWeightB"
                  errors={errors}
                  onChange={(t) => dispatch({field: 'lengthWeightB', value: t})}
                />
              </Grid>
              <Grid item xs={6}>
                <CustomTextInput
                  type="number"
                  label="Length-Weight cf"
                  formData={item.lengthWeightCf}
                  field="lengthWeightCf"
                  errors={errors}
                  onChange={(t) => dispatch({field: 'lengthWeightCf', value: t})}
                />
              </Grid>
            </Grid>
            <Box display="flex" justifyContent="center" mt={5}>
              <Button component={NavLink} to="/reference/observableItems">
                Cancel
              </Button>
              <Button style={{width: '50%', marginLeft: '5%', marginRight: '20%'}} onClick={handleSubmit} startIcon={<Save></Save>}>
                Save observable Item
              </Button>
            </Box>
          </Box>
        )}
      </Grid>
    </EntityContainer>
  );
};

ObservableItemEdit.propTypes = {
  clone: PropTypes.bool
};

export default ObservableItemEdit;