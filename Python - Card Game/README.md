# Python Guessing Card Game

A command-line card game written in Python with multiple difficulty levels and gameplay variants. This project was developed early in the degree program and focuses on core programming logic and game flow.

## Game Features

- Three difficulty levels: **Easy**, **Medium**, and **Hard**.
- Two gameplay modes: classic and figures-based variants.
- Local multiplayer turn flow with score tracking and winner resolution.
- Lightweight implementation using standard Python modules.

## How to Play

1. Run from source:
   - Open a terminal in this folder.
   - Execute: `python card_game.py`
2. Follow the prompts to choose:
   - Number of players
   - Difficulty level
   - Gameplay mode
3. Play rounds and match cards according to the selected mode.

Optional executable flow:
- If a packaged build exists, run `dist/card_game.exe`.
- To build the executable, use [PyInstaller](https://pyinstaller.org/en/stable/) with `card_game.spec`.

## Requirements

- [Python 3](https://www.python.org/downloads/) for source execution.
- Optional for packaging: [PyInstaller](https://pyinstaller.org/en/stable/).

Project files:
- Entry point: [`card_game.py`](card_game.py)
- Difficulty modules: [`easy.py`](easy.py), [`medium.py`](medium.py), [`hard.py`](hard.py)
- Variant modules: [`easy1.py`](easy1.py), [`medium1.py`](medium1.py), [`hard1.py`](hard1.py)
- Helpers: [`score.py`](score.py), [`max.py`](max.py)
