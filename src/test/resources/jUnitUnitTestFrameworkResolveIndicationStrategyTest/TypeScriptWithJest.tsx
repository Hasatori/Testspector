// __tests__/fetch.test.js
import React from 'react'

import {fireEvent, screen, waitFor} from '@testing-library/dom'
import '@testing-library/jest-dom/extend-expect'

import {AppProps} from "../index";
import {render} from "../util/TestUtils";
import Login from "../components/user/login/Login";
import {BrowserRouter} from "react-router-dom";


beforeAll(() => {
});
afterEach(() => {
});
afterAll(() => {
});
export const initProps: AppProps = {
    authenticated: false,
    loggedIn: false,
    loading: false,
    loadingMessage: undefined,
    successMessage: undefined,
    failureMessage: undefined,
    warningMessage: undefined,
    infoMessage: undefined,
    user: {} as any,
    onLogOut: () => {
    },
    loadCurrentUser: () => {
    }
};
const routeComponentPropsMock = {
    history: {} as any,
    location: {} as any,
    match: {} as any,
};
test('loads and displays greeting', async () => {
    render(<BrowserRouter><Login {...routeComponentPropsMock} loading={false} login={() => {
    }} twoFactorRequired={false} loginTwoFactor={() => {
    }}/> </BrowserRouter>);

    fireEvent.click(screen.getByText('Load Greeting'))

    await waitFor(() => screen.getByRole('heading'))

    expect(screen.getByRole('heading')).toHaveTextContent('hello there')
    expect(screen.getByRole('button')).toHaveAttribute('disabled')
});
