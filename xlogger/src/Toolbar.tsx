import {Icon} from "@iconify/react";
import squareMultiple20Regular from '@iconify/icons-fluent/square-multiple-20-regular';
import subtract20Filled from '@iconify/icons-fluent/subtract-20-filled';
import closeIcon from '@iconify/icons-mi/close';
import {appWindow} from "@tauri-apps/api/window";

const Toolbar = () => {
    return <div data-tauri-drag-region={true} className="flex justify-between select-none left-0 right-0 top-0 fixed p-1.5 bg-gray-200 shadow-sm">
        <div className="flex items-center h-fit" id="toolbar-left"/>
        <div className="flex items-center h-fit">
            <div onClick={async () => {await appWindow.minimize()}} className="inline-flex justify-center items-center text-xl p-2 hover:bg-gray-400 rounded-sm hover:text-gray-200 h-full">
                <Icon icon={subtract20Filled}/>
            </div>
            <div onClick={async () => {await appWindow.toggleMaximize()}} className="inline-flex justify-center items-center text-xl p-2 hover:bg-gray-400 rounded-sm hover:text-gray-200 h-full">
                <Icon icon={squareMultiple20Regular}/>
            </div>
            <div onClick={async () => {await appWindow.close()}} className="inline-flex justify-center items-center text-xl p-2 hover:bg-red-500 rounded-sm hover:text-gray-200 h-full">
                <Icon icon={closeIcon}/>
            </div>
        </div>
    </div>
}

export default Toolbar;
