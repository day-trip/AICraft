import "virtual:uno.css";
import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import "./styles.css";
import {createPortal} from "react-dom";
import Toolbar from "./Toolbar.tsx";

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
    <React.StrictMode>
        <App/>
        {createPortal(<Toolbar/>, document.body)}
    </React.StrictMode>,
);
