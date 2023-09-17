import {useState} from "react";
import {useNavigate} from "react-router-dom";
import {fs, invoke} from "@tauri-apps/api";

const HomeScreen = () => {
    const [minecraftFolder, setMinecraftFolder] = useState<string | undefined>(localStorage.mcf);

    const navigate = useNavigate();

    const load = async () => {
        const path: string = await invoke("getpath");
        console.log(path);
        let r = await fs.readDir(path + "\\nativelog");
        console.log(r);
        navigate("/log/" + btoa(JSON.stringify({base: path, files: r.filter(x => x.name?.endsWith(".json")).map(x => x.name)})));
    }

    return <div className="w-full min-h-[90vh] flex justify-center items-center">
        <div className="py-4 px-10 bg-white rounded-md shadow-md">
            <h1>SmartLog</h1>
            {minecraftFolder ? <>
                <p>Existing Minecraft folder: {minecraftFolder}</p>
                <button onClick={() => {setMinecraftFolder(undefined)}}>Change Minecraft folder</button>
            </> : <>
                <button className="bg-orange-500 py-1.5 px-2.5 text-white font-semibold flex justify-center items-center border-none ring-0 cursor-pointer hover:bg-orange-400 rounded-md shadow-md active:scale-95 transition-transform">Select Minecraft folder</button>
            </>}

            <div className="w-full flex justify-end mt-10">
                <button onClick={async () => {await load()}} className="bg-orange-500 py-1.5 px-3 text-lg text-white font-semibold flex justify-center items-center border-none ring-0 cursor-pointer hover:bg-orange-400 rounded-md shadow-md active:scale-95 transition-transform">Continue</button>
            </div>
        </div>
    </div>
}

export default HomeScreen;
