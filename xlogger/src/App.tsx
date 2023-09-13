import {Dispatch, SetStateAction, useEffect, useState} from "react";
import "@fontsource/inter/latin.css";
import "@fontsource/jetbrains-mono/latin.css";
import 'react-tooltip/dist/react-tooltip.css'
import {Tooltip} from "react-tooltip";
import {invoke} from "@tauri-apps/api";

type LogData = {
    categories: string[];
    logs: { [key: string]: Log[] };
}

type Log = {
    level: number;
    content: string;
    source: string;
    time: string;
    children: Log[];
}

const PLACEHOLDER_DATA: LogData = {
    categories: ["general", "pathfinding", "llm"],
    logs: {
        "general": [
            {level: 0, content: "Hi word!", source: "main.rs", time: "12:04:32", children: []},
        ],
        "pathfinding": [
            {level: 1, content: "Hi 1 wupeh0[g9!", source: "main.rs:3", time: "12:04:32", children: []},
            {
                level: 2, content: "Hi 2 3ct298v3y5v023!", source: "main.rs:4", time: "12:04:32", children: [
                    {level: 3, content: "Hi 1 wupeh0[g9!", source: "main.rs:3", time: "12:04:32", children: []},
                    {level: 4, content: "Hi 2 wupeh0[g9!", source: "main.rs:3", time: "12:04:32", children: []}
                ]
            },
            {level: 5, content: "Hi 3 3wt30vw95 [1-2 [!", source: "main.rs:7", time: "12:04:32", children: []},
        ],
        "llm": [],
    }
}

const LEVELS = ["TRACE", "DEBUG", "INFO", "WARN", "ERROR", "CRITICAL"];
const COLORS = ["#8b5cf6", "#3b82f6", "#14b8a6", "#eab308", "#ef4444", "#450a0a"];
const LV = [0, 1, 2, 3, 4, 5];

const LogUX = ({data, level, levels, setLevels}: { data: Log, level: number, levels: number[], setLevels: Dispatch<SetStateAction<number[]>> }) => {
    const [showChildren, setShowChildren] = useState(true);
    let color = COLORS[data.level];

    const levelClick = () => {
        setLevels(levels.filter(x => x != data.level));
    }

    return <div className={level > 0 ? "ml-8" : ""}>
        <p data-tooltip-id="source" data-tooltip-content={data.source} className="w-fit">{data.children.length > 0 && <span className="mr-1 text-sm cursor-pointer text-gray-500 hover:text-gray-400" onClick={() => {setShowChildren(!showChildren)}}>&#9947;</span>}<span onClick={levelClick} style={{color}} className="font-code font-bold cursor-pointer">{LEVELS[data.level]}</span> <span className="font-roboto font-semibold text-gray-800 mr-2">{`[${data.time}]`}</span> <span style={{color}} className="font-code">{data.content}</span></p>
        <Tooltip id="source"/>
        {showChildren && data.children.filter(child => levels.includes(child.level)).map((child, index) => <LogUX data={child} level={level + 1} levels={levels} setLevels={setLevels} key={index}/>)}
    </div>
}

const logSearchMatch = (log: Log, search: string): boolean => {
    if (log.content.toLowerCase().includes(search.toLowerCase())) {
        return true;
    }
    log.children.forEach(child => {
        if (logSearchMatch(child, search)) {
            return true;
        }
    })
    return false;
}

const App = () => {
    const [logData, _] = useState<LogData | undefined>(PLACEHOLDER_DATA);
    const [category, setCategory] = useState<string | undefined>("pathfinding");
    const [query, setQuery] = useState("");
    const [filteredData, setFilteredData] = useState(logData?.logs);
    const [levels, setLevels] = useState(LV);

    const data = filteredData ?  filteredData[category!].filter(log => levels.includes(log.level)) : undefined;

    const search = () => {
        if (logData) {
            if (query.length === 0) {
                setFilteredData(logData.logs);
            } else {
                let fld = {...logData.logs};
                fld[category!] = [];
                logData.logs[category!].forEach(log => {
                    if (logSearchMatch(log, query)) {
                        fld[category!].push(log);
                    }
                });
                setFilteredData(fld);
            }
        }
    }

    useEffect(() => {
        search();
    }, [query]);

    useEffect(() => {
        setLevels(LV);
        setQuery("");
        setFilteredData(logData?.logs);
        // window.dispatchEvent(new CustomEvent("cool", {detail: {}}));
    }, [category]);

    useEffect(() => {
        setFilteredData(logData?.logs);
    }, [logData]);

    /*useEffect(() => {
        const setCategories = (event: Event) => {
            setLogData({...logData!, categories: (event as CustomEvent).detail});
        }
        window.addEventListener("addcategory", setCategories);

        return () => {
            window.removeEventListener("addcategory", setCategories);
        }
    }, []);*/

    const load = async () => {
        const r = await invoke("loaddata");
        console.log(r);
    }

    return logData && filteredData && category && <div className="flex gap-4">
        <div className="w-[10rem] md:w-[14rem] lg:w-[20rem] h-fit rounded-lg shadow-md px-4 mt-1 bg-white pb-4">
            <h2 className="font-bold text-2xl">Categories</h2>
            {logData.categories.map(c => <p onClick={() => {setCategory(c)}} className={`w-full font-semibold ${category === c ? "text-orange-400" : "text-gray-500"} cursor-pointer ${category === c ? "hover:text-orange-300" : "hover:text-gray-300"}`}>{c.toLowerCase().split(' ').map((s) => s.charAt(0).toUpperCase() + s.substring(1)).join(' ')}</p>)}
            <button onClick={load} className="mt-10 bg-orange-500 py-1.5 px-3 text-lg text-white font-semibold flex justify-center items-center border-none ring-0 cursor-pointer hover:bg-orange-400 rounded-md shadow-md active:scale-95 transition-transform">Load Data</button>
        </div>
        <div className="flex-1">
            <div className="flex shadow-md rounded-md w-fit mt-1 w-full mb-2">
                <input value={query} onChange={e => {setQuery(e.target.value)}} type="text" placeholder="Search" className="flex-1 rounded-md border-none ring-1 ring-orange-400 outline-none p-2.5 text-base focus:ring-2 placeholder-gray-400 focus:placeholder-gray-300 focus:ring-orange-500"/>
            </div>

            <div className="flex gap-2 flex-wrap mb-2">
                {LV.filter(level => !levels.includes(level)).map(level => <span onClick={() => {setLevels([...levels, level])}} style={{color: COLORS[level]}} className="p-2 bg-gray-200 hover:bg-gray-100 rounded-lg cursor-pointer shadow-sm flex justify-center items-center font-bold">{LEVELS[level]}</span>)}
            </div>

            {data!.length === 0 ? <p className="text-gray-600 font-semibold text-xl mt-5">No results!</p> : data!.map((log, index) => <LogUX key={index} data={log} levels={levels} setLevels={setLevels} level={0}/>)}
        </div>
    </div>
}

export default App;
