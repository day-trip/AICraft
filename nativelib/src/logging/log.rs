use std::collections::HashMap;
use std::time::Duration;
use crossterm::event;
use crossterm::event::{Event, KeyCode, KeyEventKind};
use tui::backend::CrosstermBackend;
use tui::layout::{Constraint, Direction, Layout};
use tui::style::{Color, Style};
use tui::Terminal;
use tui::text::{Spans};
use tui::widgets::{Block, Borders, Paragraph};

#[derive(Eq, PartialEq, Debug, Copy, Clone)]
enum ViewType {
    ViewLogs,
    SelectCategory
}

#[derive(Eq, PartialEq, Debug, Copy, Clone)]
struct Log {
    pub cool: i8,
}

impl ToString for Log {
    fn to_string(&self) -> String {
        String::from(format!("COOL: {}", self.cool))
    }
}

pub fn init_ui() {
    let mut terminal = Terminal::new(CrosstermBackend::new(std::io::stdout())).expect("No terminal!");

    let mut view = ViewType::SelectCategory;
    let mut category = 0u8;

    let categories = vec!["cool", "uwu", "jai", "mama", "dawwio", "winnie", "doi", "wei"];

    let mut logs: HashMap<u8, Vec<Log>> = HashMap::default();

    for (i, _) in categories.iter().enumerate() {
        let mut v = vec![];
        for x in 0..500 {
            v.push(Log {cool: x as i8});
        }
        logs.insert(i as u8, v);
    }

    loop {
        let l = if view == ViewType::ViewLogs { Some(logs.get(&category).unwrap()) } else { None };

        terminal.draw(|f| {
            let chunks = Layout::default()
                .direction(Direction::Vertical)
                .margin(1)
                .constraints(
                    [
                        Constraint::Length(if view == ViewType::SelectCategory { (categories.len() + 3) as u16 } else { l.unwrap().len() as u16 }),
                        Constraint::Min(0),
                    ]
                        .as_ref(),
                )
                .split(f.size());

            let style = Style::default().fg(Color::White).bg(Color::Black);

            match view {
                ViewType::SelectCategory => {
                    let mut v = vec![];
                    for cat in categories.iter().enumerate() {
                        v.push(Spans::from(String::from(*cat.1) + (if category as usize == cat.0 { " (selected)" } else { "" })));
                    }
                    v.push(Spans::from("Press ENTER to confirm."));
                    let paragraph = Paragraph::new(v).style(style)
                        .block(Block::default().borders(Borders::ALL).title("Select a category"));
                    f.render_widget(paragraph, chunks[0]);
                }
                ViewType::ViewLogs => {
                    let mut v = vec![];
                    v.push(Spans::from(format!("Viewing {} logs. Press C to switch category.", l.unwrap().len())));
                    for log in l.unwrap() {
                        v.push(Spans::from(log.to_string()))
                    }
                    let paragraph = Paragraph::new(v)
                        .style(style)
                        .block(Block::default().borders(Borders::ALL).title("Log view"));
                    f.render_widget(paragraph, chunks[0]);
                }
            }
        }).unwrap();
        if event::poll(Duration::from_millis(200)).expect("poll works") {
            if let Event::Key(key) = event::read().expect("can read events") {
                if key.kind == KeyEventKind::Press {
                    match key.code {
                        KeyCode::Enter => {
                            view = ViewType::ViewLogs;
                        }
                        KeyCode::Char('c') => {
                            view = ViewType::SelectCategory;
                        }
                        KeyCode::Up => {
                            category = (category - 1) % categories.len() as u8
                        }
                        KeyCode::Down => {
                            category = (category + 1) % categories.len() as u8
                        }
                        _ => {}
                    }
                }
            }
        }
    }
}
