import '@testing-library/jest-dom/extend-expect';
import { render, waitFor, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { describe, beforeAll, afterEach } from '@jest/globals';
import { Router } from 'react-router-dom';
import * as axiosInstance from '../../../../api/api';
import { AxiosResponse } from 'axios';
import DiverList from '../DiverList';
import { createMemoryHistory } from 'history';
import stateFilterHandler from '../../../../common/state-event-handler/StateFilterHandler';

jest.setTimeout(10000);

describe('<DiverList/> filter testing', () => {
  let mockGetResult;
  let mockGetFiltersForId;
  let mockResetStateFilters;
  const columns = ['initials', 'fullName'];

  beforeAll(() => {
    mockGetResult = jest.spyOn(axiosInstance, 'getResult');
    mockGetFiltersForId = jest.spyOn(stateFilterHandler, 'getFiltersForId');
    mockResetStateFilters = jest.spyOn(stateFilterHandler, 'resetStateFilters');

    // silence errors caused by not setting an AG Grid licence
    jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    mockGetResult.mockReset();
    mockGetFiltersForId.mockReset();
  });

  test('Render necessary fields and no filter restored', async () => {
    const canned = require('./DiverList.filter.data.json');

    // Override function so that it return the data we set.
    mockGetResult.mockImplementation((url) => {

      const raw = {
        config: undefined,
        data: canned,
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        status: 200,
        statusText: url
      };

      return new Promise<AxiosResponse>((resolve => {
        resolve(raw);
      }));
    });

    const history = createMemoryHistory({initialEntries:[{state: {resetFilters: true}}]});
    const {container, rerender} = render(<Router location={history.location} navigator={history}><DiverList/></Router>);

    // Data loaded due to mock object being called once
    await waitFor(() => expect(mockGetResult).toHaveBeenCalledTimes(1), {timeout: 10000})
      .then(() => {
        // verify default columns exist

        columns.forEach(x => {
          expect(container.querySelector('[col-id="' + x + '"]')).toBeInTheDocument();
        });
      })
      .finally(() => {
        // Refresh the dom tree
        rerender(<Router location={history.location} navigator={history}><DiverList/></Router>);

        expect(screen.getByText('Apple Orange')).toBeInTheDocument();
        expect(screen.getByText('Cherry Melon')).toBeInTheDocument();
        expect(screen.getByText('Rock Melon')).toBeInTheDocument();

        // Restore filter not called if you pass the resetFilter false to the component
        expect(mockGetFiltersForId).toBeCalledTimes(0);
        expect(mockResetStateFilters).toBeCalledTimes(1);
      });
  });

  test('Render necessary fields with filter restored', async () => {
    const canned = require('./DiverList.filter.data.json');

    // Filter set will cause some items disappeared
    mockGetFiltersForId.mockImplementation((id) => {
      return '{"fullName":{"filterType":"text","type":"contains","filter":"Orange"}}';
    });

    // Override function so that it return the data we set.
    mockGetResult.mockImplementation((url) => {

      const raw = {
        config: undefined,
        data: canned,
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        status: 200,
        statusText: url
      };

      return new Promise<AxiosResponse>((resolve => {
        resolve(raw);
      }));
    });

    const history = createMemoryHistory({initialEntries:[{state: {resetFilters: false}}]});
    const {container, rerender} = render(<Router location={history.location} navigator={history}><DiverList/></Router>);

    // Data loaded due to mock object being called once
    await waitFor(() => expect(mockGetResult).toHaveBeenCalledTimes(1), {timeout: 10000})
      .then(() => {
        // verify default columns exist
        columns.forEach(x => {
          expect(container.querySelector('[col-id="' + x + '"]')).toBeInTheDocument();
        });
      })
      .finally(() => {
        // Refresh the dom tree
        rerender(<Router location={history.location} navigator={history}><DiverList/></Router>);

        // Restore filter called
        expect(mockGetFiltersForId).toBeCalledTimes(1);
        expect(mockResetStateFilters).toBeCalledTimes(0);

        // id = 1 so show this one
        expect(screen.getByText('Apple Orange')).toBeInTheDocument();

        // id != 1 so no show
        screen.findByText('Cherry Melon').then(i => expect(i).toBe({}));
        screen.findByText('Rock Melon').then(i => expect(i).toBe({}));
      });
  });
});
