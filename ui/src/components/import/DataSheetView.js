import React, {useState} from 'react';
import {useDispatch, useSelector} from 'react-redux';
import {AgGridReact} from 'ag-grid-react';
import {AllModules} from 'ag-grid-enterprise';
import {useEffect} from 'react';
import {JobFinished} from './reducers/create-import';
import {makeStyles} from '@material-ui/core/styles';
import {ColumnDef, ExtendedSize} from './ColumnDef';
import {Box, Fab} from '@material-ui/core';
import useWindowSize from '../utils/useWindowSize';
import SaveOutlinedIcon from '@material-ui/icons/SaveOutlined';

Object.unfreeze = function (o) {
  var oo = undefined;
  if (o instanceof Array) {
    oo = [];
    var clone = function (v) {
      oo.push(v);
    };
    o.forEach(clone);
  } else if (o instanceof String) {
    oo = new String(o).toString();
  } else if (typeof o == 'object') {
    oo = {};
    for (var property in o) {
      oo[property] = o[property];
    }
  }
  return oo;
};

const useStyles = makeStyles((theme) => ({
  fab: {
    position: 'absolute',
    bottom: theme.spacing(2),
    right: theme.spacing(20)
  }
}));

const DataSheetView = () => {
  const classes = useStyles();
  const dispatch = useDispatch();
  const immutableRows = useSelector((state) => state.import.rows);
  const errSelected = useSelector((state) => state.import.errSelected);
  const [gridApi, setGridApi] = useState(null);
  const [rowsChanged, setRowsChanged] = useState({});
  const validationLoading = useSelector((state) => state.import.validationLoading);
  var rows = immutableRows.map(Object.unfreeze);
  const job = useSelector((state) => state.import.job);
  rows.secret = 'mutable';
  const colDefinition = job && job.isExtendedSize ? ColumnDef.concat(ExtendedSize) : ColumnDef;

  const agGridReady = (params) => {
    setGridApi(params.api);
    dispatch(JobFinished());
    params.api.setRowData(rows);
    var allColumnIds = [];
    params.columnApi.getAllColumns().forEach(function (column) {
      allColumnIds.push(column.colId);
    });
    params.columnApi.autoSizeColumns(allColumnIds, true);
  };

  const onCellChanged = (input) => {
    if (input.newValue != input.OldValue) {
      var toAdd = {};
      toAdd[input.data.id] = input.data;
      setRowsChanged(Object.assign(toAdd, rowsChanged));
    }
    console.log(rowsChanged);
  };

  const getContextMenuItems = (params) => {
    return [
      {
        name: 'Delete selected Row(s)',
        action: () => {
          const selectedRows = params.api.getSelectedRows();
          params.api.applyTransaction({remove: selectedRows});
        },
        cssClasses: ['redBoldFont']
      }
    ];
  };

  useEffect(() => {
    if (gridApi && rows) {
      gridApi.setRowData(rows);
    }
  }, [validationLoading]);

  useEffect(() => {
    if (gridApi && errSelected.ids && errSelected.ids.length > 0) {
      const instance = gridApi.getFilterInstance('id');
      instance.setModel({values: errSelected.ids.map((id) => id.toString())}).then(() => gridApi.onFilterChanged());
    }

    if (errSelected.ids === null) {
      const instance = gridApi.getFilterInstance('id');
      instance.setModel(null).then(() => gridApi.onFilterChanged());
    }
  });

  const size = useWindowSize();
  const themeType = useSelector((state) => state.theme.themeType);
  // const condition = rows && rows.length > 0 && isLoading;

  return (
    <Box>
      {rows && (
        <div
          id="validation-grid"
          style={{height: size.height - 165, width: '100%', marginTop: 25}}
          className={themeType ? 'ag-theme-material-dark' : 'ag-theme-material'}
        >
          <AgGridReact
            immutableRows={false}
            getRowNodeId={(data) => data.id}
            pivotMode={false}
            pivotColumnGroupTotals={'before'}
            sideBar={true}
            autoGroupColumnDef={{
              width: 20,
              cellRendererParams: {
                suppressCount: true,
                innerRenderer: 'nameCellRenderer'
              }
            }}
            onCellValueChanged={onCellChanged}
            columnDefs={colDefinition}
            groupDefaultExpanded={4}
            rowHeight={18}
            animateRows={true}
            groupMultiAutoColumn={true}
            groupHideOpenParents={true}
            rowSelection={'multiple'}
            enableRangeSelection={true}
            undoRedoCellEditing={true}
            undoRedoCellEditingLimit={1000}
            ensureDomOrder={true}
            defaultColDef={{
              minWidth: 80,
              filter: true,
              sortable: true,
              resizable: true
              // valueSetter: (params) => {
              //   dispatch(setCell({
              //     index: params.node.childIndex,
              //      field: params.colDef.field,
              //       newValue: params.newValue}));
              //   return true;
              // }
            }}
            onGridReady={agGridReady}
            modules={AllModules}
            getContextMenuItems={getContextMenuItems}
          ></AgGridReact>
        </div>
      )}
      <Fab className={classes.fab} color="primary">
        <SaveOutlinedIcon></SaveOutlinedIcon>
      </Fab>
    </Box>
  );
};

export default DataSheetView;
