import React, {useState} from 'react';
import {Navigate, useParams} from 'react-router';
import {Box} from '@mui/material';
import DataSheetView from './DataSheetView';
import Alert from '@mui/material/Alert';

const ValidationJob = () => {
  const {id} = useParams();
  const [ingestState, setIngestState] = useState({});

  if (ingestState.data && ingestState.status === 200) {
    return <Navigate to={`/data/job/${id}/view`} />;
  }

  return (
    <>
      {ingestState.data && (
        <Box mx={2}>
          <Alert severity="error" variant="outlined">
            <p>Sheet failed to ingest. No survey data has been inserted.</p>
            <p>Error: {ingestState.data}</p>
          </Alert>
        </Box>
      )}
      <DataSheetView jobId={id} onIngest={setIngestState} />
    </>
  );
};

export default ValidationJob;