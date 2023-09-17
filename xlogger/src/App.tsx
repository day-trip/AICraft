import {createMemoryRouter, RouterProvider} from "react-router-dom";
import HomeScreen from "./HomeScreen.tsx";
import LogScreen from "./LogScreen.tsx";

const App = () => {
    const router = createMemoryRouter([
        {
            path: "/",
            element: <HomeScreen/>,
        },
        {
            path: "/log/:path",
            element: <LogScreen/>,
        },
    ]);

    return <RouterProvider router={router}/>
}

export default App;
