import {
  createSlice,
  createAction
} from "@reduxjs/toolkit";
import pluralize from "pluralize";


const formState = {
  entities: undefined,
  editItem: {},
  newlyCreatedEntity: {},
  errors: []
};


const formSlice = createSlice({
  name: "form",
  initialState: formState,
  reducers: {
    resetState: (state, action) => {
      state = formState;
    },
    entitiesLoaded: (state, action) => {
      state.newlyCreatedEntity = {};
      state.editItem = {};
      state.entities = action.payload;
      state.errors = [];
    },
    entitiesError: (state, action) => {
      const error = "Error while getting the entity data"
      state.entities = [];
      state.errors = [error];
    },
    itemLoaded: (state, action) => {
      state.editItem = action.payload;
    },
    setSelectedFormData: (state, action) => {
      let resp = {};
      const key = Object.keys(action.payload)[0];
      resp[key + "Selected"] = action.payload;
      resp[key] = action.payload[key]._links.self.href;
      state.editItem = {...state.editItem, ...resp};
    },
    selectedItemsLoaded: (state, action) => {
      let resp = {};
      const key = Object.keys(action.payload._embedded)[0];
      resp[key] = action.payload._embedded;
      resp[pluralize.singular(key) + "Selected"] = action.payload.selected;
      resp[pluralize.singular(key)] = action.payload.selected._links.self.href;
      state.editItem = {...state.editItem, ...resp};
    },
    entitiesCreated: (state, action) => {
      state.newlyCreatedEntity = action.payload;
    }
  },
});
export const formReducer = formSlice.reducer;
export const {
  resetState,
  entitiesLoaded,
  entitiesError,
  entitiesCreated,
  itemLoaded,
  selectedItemsLoaded,
  setSelectedFormData
} = formSlice.actions;


