#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

use std::{fs};
use native_dialog::FileDialog;

#[tauri::command]
fn getpath() -> Option<String> {
    match FileDialog::new().show_open_single_dir().expect("Could not pick folder!") {
        None => { None }
        Some(p) => {
            let path = String::from(p.to_str().expect("Could not create file string!"));
            Some(path)
        }
    }
}

#[tauri::command]
fn refreshdata(path: String) -> Option<String> {
    let data = fs::read_to_string(path + "\\nativelog\\nativelog.json").expect("Unable to read data!");
    Some(data)
}

fn main() {
    tauri::Builder::default()
        .setup(|app| {
            /*let main_window = app.get_window("main").expect("Could not get window!");
            let mut stream = UnixStream::connect("/path/to/my/socket").expect("Could not create stream");
            tauri::async_runtime::spawn(async move {

            });*/
            Ok(())
        })
        .invoke_handler(tauri::generate_handler![getpath, refreshdata])
        .run(tauri::generate_context!())
        .expect("Error while running Tauri application.");
}
