"""
LED Runner - A customizable scrolling LED-style marquee for your laptop screen.

Run with:  python led_runner.py

Requires: Python 3 with tkinter (included in standard Python installs on
Windows/macOS; on Linux install with e.g. `sudo apt install python3-tk`).
"""

import tkinter as tk
from tkinter import ttk, colorchooser, font as tkfont


class LedRunnerApp:
    def __init__(self, root):
        self.root = root
        self.root.title("LED Runner - Settings")
        self.root.geometry("420x420")
        self.root.resizable(False, False)

        # ---- state ----
        self.text_color = "#00FF00"     # default LED green
        self.bg_color = "#000000"       # LED-style black background
        self.display_win = None
        self.canvas = None
        self.text_id = None
        self.x_pos = 0
        self.animating = False
        self.after_id = None

        self._build_settings_ui()

    # ------------------------------------------------------------------
    # Settings window
    # ------------------------------------------------------------------
    def _build_settings_ui(self):
        pad = {"padx": 10, "pady": 6}

        frame = ttk.Frame(self.root)
        frame.pack(fill="both", expand=True)

        # --- Text ---
        ttk.Label(frame, text="Runner text:").grid(row=0, column=0, sticky="w", **pad)
        self.text_var = tk.StringVar(value="WELCOME TO LED RUNNER!")
        ttk.Entry(frame, textvariable=self.text_var, width=30).grid(
            row=0, column=1, columnspan=2, sticky="we", **pad
        )

        # --- Font family ---
        ttk.Label(frame, text="Font:").grid(row=1, column=0, sticky="w", **pad)
        available_fonts = sorted(tkfont.families())
        preferred = "Courier New" if "Courier New" in available_fonts else available_fonts[0]
        self.font_var = tk.StringVar(value=preferred)
        font_box = ttk.Combobox(
            frame, textvariable=self.font_var, values=available_fonts, state="readonly", width=25
        )
        font_box.grid(row=1, column=1, columnspan=2, sticky="we", **pad)

        # --- Font size ---
        ttk.Label(frame, text="Font size:").grid(row=2, column=0, sticky="w", **pad)
        self.size_var = tk.IntVar(value=72)
        ttk.Spinbox(frame, from_=10, to=400, textvariable=self.size_var, width=10).grid(
            row=2, column=1, sticky="w", **pad
        )

        # --- Bold toggle ---
        self.bold_var = tk.BooleanVar(value=True)
        ttk.Checkbutton(frame, text="Bold", variable=self.bold_var).grid(
            row=2, column=2, sticky="w", **pad
        )

        # --- Text color ---
        ttk.Label(frame, text="Text color:").grid(row=3, column=0, sticky="w", **pad)
        self.color_preview = tk.Label(
            frame, text="      ", bg=self.text_color, relief="sunken", width=8
        )
        self.color_preview.grid(row=3, column=1, sticky="w", **pad)
        ttk.Button(frame, text="Choose...", command=self.pick_text_color).grid(
            row=3, column=2, sticky="w", **pad
        )

        # --- Background color ---
        ttk.Label(frame, text="Background color:").grid(row=4, column=0, sticky="w", **pad)
        self.bg_preview = tk.Label(
            frame, text="      ", bg=self.bg_color, relief="sunken", width=8
        )
        self.bg_preview.grid(row=4, column=1, sticky="w", **pad)
        ttk.Button(frame, text="Choose...", command=self.pick_bg_color).grid(
            row=4, column=2, sticky="w", **pad
        )

        # --- Speed ---
        ttk.Label(frame, text="Speed:").grid(row=5, column=0, sticky="w", **pad)
        self.speed_var = tk.IntVar(value=10)  # pixels moved per tick
        speed_scale = ttk.Scale(
            frame, from_=1, to=40, orient="horizontal", variable=self.speed_var
        )
        speed_scale.grid(row=5, column=1, columnspan=2, sticky="we", **pad)
        ttk.Label(frame, text="(1 = slow, 40 = very fast)").grid(
            row=6, column=1, columnspan=2, sticky="w"
        )

        # --- Direction ---
        ttk.Label(frame, text="Direction:").grid(row=7, column=0, sticky="w", **pad)
        self.direction_var = tk.StringVar(value="Right to Left")
        ttk.Combobox(
            frame,
            textvariable=self.direction_var,
            values=["Right to Left", "Left to Right"],
            state="readonly",
            width=15,
        ).grid(row=7, column=1, sticky="w", **pad)

        # --- Buttons ---
        btn_frame = ttk.Frame(frame)
        btn_frame.grid(row=8, column=0, columnspan=3, pady=20)
        ttk.Button(btn_frame, text="Start Runner", command=self.start_runner).pack(
            side="left", padx=10
        )
        ttk.Button(btn_frame, text="Stop Runner", command=self.stop_runner).pack(
            side="left", padx=10
        )

        ttk.Label(
            frame,
            text="Tip: press ESC in the runner window to close it.",
            foreground="gray",
        ).grid(row=9, column=0, columnspan=3, pady=(10, 0))

        for col in range(3):
            frame.columnconfigure(col, weight=1)

    def pick_text_color(self):
        color = colorchooser.askcolor(title="Choose text color", initialcolor=self.text_color)
        if color[1]:
            self.text_color = color[1]
            self.color_preview.config(bg=self.text_color)

    def pick_bg_color(self):
        color = colorchooser.askcolor(title="Choose background color", initialcolor=self.bg_color)
        if color[1]:
            self.bg_color = color[1]
            self.bg_preview.config(bg=self.bg_color)

    # ------------------------------------------------------------------
    # Runner window / animation
    # ------------------------------------------------------------------
    def start_runner(self):
        # Stop any existing runner first
        self.stop_runner()

        text = self.text_var.get() or " "
        family = self.font_var.get()
        size = self.size_var.get()
        weight = "bold" if self.bold_var.get() else "normal"
        run_font = tkfont.Font(family=family, size=size, weight=weight)

        self.display_win = tk.Toplevel(self.root)
        self.display_win.title("LED Runner")
        self.display_win.configure(bg=self.bg_color)
        self.display_win.attributes("-fullscreen", True)
        self.display_win.bind("<Escape>", lambda e: self.stop_runner())
        self.display_win.protocol("WM_DELETE_WINDOW", self.stop_runner)

        screen_w = self.display_win.winfo_screenwidth()
        screen_h = self.display_win.winfo_screenheight()

        self.canvas = tk.Canvas(
            self.display_win,
            width=screen_w,
            height=screen_h,
            bg=self.bg_color,
            highlightthickness=0,
        )
        self.canvas.pack(fill="both", expand=True)

        text_width = run_font.measure(text)
        y_pos = screen_h // 2

        direction = self.direction_var.get()
        if direction == "Right to Left":
            self.x_pos = screen_w
            self.dx = -abs(self.speed_var.get())
        else:
            self.x_pos = -text_width
            self.dx = abs(self.speed_var.get())

        self.text_id = self.canvas.create_text(
            self.x_pos,
            y_pos,
            text=text,
            fill=self.text_color,
            font=run_font,
            anchor="w",
        )

        self.screen_w = screen_w
        self.text_width = text_width
        self.animating = True
        self._animate()

    def _animate(self):
        if not self.animating or self.canvas is None:
            return

        # The canvas/window may have been destroyed (e.g. user closed it,
        # pressed Esc, or clicked Stop) right before this callback fires.
        try:
            if not self.canvas.winfo_exists():
                self.animating = False
                return

            self.x_pos += self.dx
            self.canvas.coords(self.text_id, self.x_pos, self.canvas.winfo_height() // 2)

            # Wrap around when text fully leaves the screen
            if self.dx < 0 and self.x_pos + self.text_width < 0:
                self.x_pos = self.screen_w
            elif self.dx > 0 and self.x_pos > self.screen_w:
                self.x_pos = -self.text_width
        except tk.TclError:
            # Widget was destroyed mid-callback; stop cleanly.
            self.animating = False
            return

        if self.animating:
            self.after_id = self.root.after(20, self._animate)

    def stop_runner(self):
        self.animating = False
        if self.after_id is not None:
            try:
                self.root.after_cancel(self.after_id)
            except Exception:
                pass
            self.after_id = None
        if self.display_win is not None:
            try:
                self.display_win.destroy()
            except Exception:
                pass
            self.display_win = None
            self.canvas = None
            self.text_id = None


if __name__ == "__main__":
    root = tk.Tk()
    app = LedRunnerApp(root)
    root.mainloop()
