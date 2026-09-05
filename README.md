# LED Runner

A customizable scrolling LED-style marquee. Enter your own text, pick a font, size, color, and speed, and it scrolls across the screen like an electronic sign.

Available in four versions — pick whichever fits your setup:

| File | Language | Type |
|---|---|---|
| `led_runner.py` | Python (Tkinter) | Desktop app |
| `led_runner.html` | HTML/CSS/JS | Browser app |
| `LedRunner.java` | Java (Swing) | Desktop app |
| `led_runner.php` | PHP | Web app |

---

## Python (`led_runner.py`)

- Desktop GUI built with **Tkinter** (included with standard Python — no installs needed on Windows/macOS; `sudo apt install python3-tk` on Linux)
- Settings window lets you set: runner text, font family (auto-lists all installed fonts), font size, bold on/off, text color, background color, speed, and scroll direction
- **Start Runner** opens a real fullscreen window with the text scrolling and wrapping continuously
- Press **Esc**, click **Stop Runner**, or close the window to end it — all handled safely
- Run with: `python led_runner.py`

## HTML (`led_runner.html`)

- Single self-contained file — just open it in any browser, no installs, no server needed
- Live control panel on the left, live `<canvas>` preview on the right, updates instantly as you type or drag sliders
- Includes LED/pixel-style Google Fonts (VT323, Share Tech Mono) alongside standard fonts
- Extra features beyond the base request: **glow effect** toggle for an authentic LED-sign look, **Play/Pause**, **Reset**, and a **Fullscreen** button for using it as an actual laptop-screen sign
- Smooth animation via `requestAnimationFrame`, fully responsive to window resizing

## Java (`LedRunner.java`)

- Desktop GUI built with **Swing** (part of the standard JDK — no external libraries)
- Settings window lets you set: runner text, font (lists every font installed on your system), size, bold, text color and background color (via native color-picker dialogs), speed, direction, and glow on/off
- **Start Runner** opens a true fullscreen borderless window using Java's exclusive full-screen mode
- Press **Esc** or click **Stop Runner** to close it cleanly
- Compile and run with: `javac LedRunner.java` then `java LedRunner`

## PHP (`led_runner.php`)

- Works as a small **web app** rather than a desktop app, since PHP is server-side
- One file: a settings form (message, font, size, speed, colors, direction, glow) submits via `GET`, so a configured runner is a shareable, bookmarkable URL
- All user input is validated and sanitized server-side (hex colors checked, fonts limited to an allow-list, numbers clamped, text escaped) before being rendered
- The scroll itself runs as a smooth CSS `@keyframes` animation — PHP's job is just computing the animation duration and injecting your chosen values
- Run with: `php -S localhost:8000`, then open `http://localhost:8000/led_runner.php`

---

## Common features across all four

- Custom runner text
- Adjustable font size
- Font selection
- Custom text color
- Adjustable speed
- Full-screen / full-window display

## A note on testing

The Java and PHP versions couldn't be compiled/run inside this working environment (no `javac` or PHP interpreter available here due to sandbox/network restrictions), so those two were verified through careful manual code review and brace/syntax balance checks rather than an actual execution. The Python and HTML versions were checked more directly. If you hit any errors running Java or PHP, share the message and it can be fixed quickly.
