import React, {useEffect, useState} from 'react';
import {Box, Divider, Grid, Typography, Button} from '@mui/material';
import {getEntity} from '../../../api/api';
import {useParams} from 'react-router';
import {Edit} from '@mui/icons-material';
import {NavLink} from 'react-router-dom';
import EntityContainer from '../../containers/EntityContainer';
import CustomTextInput from '../../input/CustomTextInput';

const ObservableItemView = () => {
  const id = useParams()?.id;
  const [data, setData] = useState({});

  useEffect(() => {
    async function fetchObservableItem() {
      await getEntity(`reference/observableItem/${id}`).then((res) => setData(res.data));
    }
    fetchObservableItem();
  }, [id]);

  return (
    <EntityContainer name="Sites" goBackTo="/reference/observableItems">
      <Box m={2} display="flex" flexDirection="row" width="100%">
        <Box flexGrow={1}>
          <Typography variant="h4">Observable Items</Typography>
        </Box>
        <Box>
          <Button variant="outlined" component={NavLink} to={`/reference/observableItem/${id}/edit`} startIcon={<Edit>edit</Edit>}>
            Edit
          </Button>
        </Box>
      </Box>
      <Box p={2}>
        <Grid container spacing={2}>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="ID" formData={data.observableItemId} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Species Name" formData={data.observableItemName} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Observable Item Type" formData={data.obsItemTypeName} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Common Name" formData={data.commonName} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Aphia ID" formData={data.aphiaId ?? '---'} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Aphia Relation" formData={data.aphiaRelTypeName ?? '---'} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Superseded By" formData={data.supersededBy ?? '---'} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Superseded Names" formData={data.supersededNames ?? '---'} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Superseded IDs" formData={data.supersededIds ?? '---'} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Letter Code" formData={data.letterCode ?? '---'} />
          </Grid>
        </Grid>
      </Box>
      <Divider />
      <Box p={2}>
        <Grid container spacing={2}>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Phylum" formData={data.phylum} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Class" formData={data.class} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Order" formData={data.order} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Family" formData={data.family} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Genus" formData={data.genus} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Report Group" formData={data.reportGroup} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Habitat Group" formData={data.habitatGroups} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Species Epithet" formData={data.speciesEpithet} />
          </Grid>

          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Length-Weight a" formData={data.lengthWeightA} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Length-Weight b" formData={data.lengthWeightB} />
          </Grid>
          <Grid item xs={6}>
            <CustomTextInput readOnlyInput label="Length-Weight cf" formData={data.lengthWeightCf} />
          </Grid>
          <Grid item xs={12}>
            <Box p={2} pl={8}>
              {data.obsItemAttribute ? (
                data.obsItemAttribute
              ) : (
                <Typography variant="subtitle2" component="i">
                  No Other Attributes
                </Typography>
              )}
            </Box>
          </Grid>
        </Grid>
      </Box>
    </EntityContainer>
  );
};

export default ObservableItemView;
