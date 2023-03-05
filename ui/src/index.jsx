import React from 'react'
import ReactDOM from 'react-dom/client'
import { Helmet } from 'react-helmet'
import './i18n'
import './index.css'
import DefaultLayout from './layouts/DefaultLayout'
import reportWebVitals from './reportWebVitals'
import GlobalStateProvider from './state/GlobalStateProvider'

// eslint-disable-next-line no-undef
console.log('REACT_APP_FOO', process.env.REACT_APP_FOO)

const root = ReactDOM.createRoot(document.getElementById('root'))
root.render(
  <React.Fragment>
    <Helmet>
      <title>cy17-react-app-starter</title>
      <meta name="image" property="og:image" content="/logo512.png" />
      <meta
        name="description"
        property="og:description"
        content="Contains the foundation of a react-app repository + commonly used libraries to be like a framework of some sort."
      />
    </Helmet>
    <GlobalStateProvider>
      <DefaultLayout />
    </GlobalStateProvider>
  </React.Fragment>
)

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals()
