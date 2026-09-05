<?php
/**
 * LED Runner - a customizable scrolling LED-style marquee, built in PHP.
 *
 * PHP renders the settings form, then renders the actual marquee page
 * using CSS animation (so the scrolling itself is smooth, GPU-accelerated
 * CSS - PHP's job is just to read the user's settings and generate the
 * right HTML/CSS from them).
 *
 * Run:
 *   php -S localhost:8000
 * then open http://localhost:8000/index.php in a browser.
 * (Works on any standard PHP host too - Apache, Nginx+PHP-FPM, etc.)
 */

// ---------------------------------------------------------------------
// 1. Read & sanitize settings from the query string (GET), with defaults.
//    Using GET means a configured runner can be bookmarked/shared as a URL.
// ---------------------------------------------------------------------

function clamp_int($value, $min, $max, $default) {
    if (!is_numeric($value)) {
        return $default;
    }
    $n = (int) $value;
    if ($n < $min) return $min;
    if ($n > $max) return $max;
    return $n;
}

function clean_hex_color($value, $default) {
    if (is_string($value) && preg_match('/^#[0-9a-fA-F]{6}$/', $value)) {
        return $value;
    }
    return $default;
}

$allowed_fonts = [
    "'Courier New', monospace"        => 'Courier New',
    "'Share Tech Mono', monospace"    => 'Share Tech Mono (LED style)',
    "'VT323', monospace"              => 'VT323 (pixel style)',
    "Arial, sans-serif"               => 'Arial',
    "Georgia, serif"                  => 'Georgia',
    "'Times New Roman', serif"        => 'Times New Roman',
    "Impact, sans-serif"              => 'Impact',
];

$started = isset($_GET['start']);

$text      = isset($_GET['text']) ? trim((string) $_GET['text']) : 'WELCOME TO LED RUNNER!';
if ($text === '') {
    $text = ' ';
}
// Trim to a sane max length to keep the page well-behaved.
if (function_exists('mb_substr')) {
    $text = mb_substr($text, 0, 200);
} else {
    $text = substr($text, 0, 200);
}

$font_key  = isset($_GET['font']) ? (string) $_GET['font'] : "'Share Tech Mono', monospace";
if (!array_key_exists($font_key, $allowed_fonts)) {
    $font_key = "'Share Tech Mono', monospace";
}

$size      = clamp_int($_GET['size']  ?? null, 10, 400, 120);
$speed     = clamp_int($_GET['speed'] ?? null, 1, 40, 10);   // seconds per pass (lower = faster)
$text_color = clean_hex_color($_GET['text_color'] ?? null, '#ffb000');
$bg_color   = clean_hex_color($_GET['bg_color']   ?? null, '#000000');
$direction  = (isset($_GET['direction']) && $_GET['direction'] === 'ltr') ? 'ltr' : 'rtl';
$glow       = isset($_GET['glow']) && $_GET['glow'] === '1';

// Duration of one full crossing, in seconds. The speed slider is 1 (slow)
// to 40 (fast); invert it so higher slider values = shorter (faster) pass.
$duration = round(21 - ($speed / 2), 2); // ranges roughly 20.5s down to 1s
if ($duration < 1) {
    $duration = 1;
}

function h($value) {
    return htmlspecialchars((string) $value, ENT_QUOTES, 'UTF-8');
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>LED Runner (PHP)</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=VT323&family=Share+Tech+Mono&display=swap" rel="stylesheet">
<style>
  :root{
    --bg: #0b0d10;
    --panel: #14171b;
    --panel-line: #23272d;
    --text-dim: #8a929c;
    --text: #e7ebee;
    --accent: #ffb000;
    --radius: 3px;
  }
  * { box-sizing: border-box; }
  html, body {
    margin: 0;
    min-height: 100%;
    background: var(--bg);
    color: var(--text);
    font-family: "Share Tech Mono", ui-monospace, "Courier New", monospace;
  }

<?php if (!$started): ?>

  body { display: flex; justify-content: center; padding: 40px 16px; }
  form {
    width: 100%;
    max-width: 440px;
    background: var(--panel);
    border: 1px solid var(--panel-line);
    border-radius: 6px;
    padding: 26px 24px;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
  h1 {
    font-size: 20px;
    letter-spacing: 1px;
    margin: 0 0 2px;
    color: var(--accent);
    text-shadow: 0 0 8px rgba(255,176,0,0.5);
  }
  p.sub { color: var(--text-dim); font-size: 12px; margin: 0; }
  label { font-size: 11px; color: var(--text-dim); display: block; margin-bottom: 6px; }
  input[type="text"], select {
    width: 100%;
    background: #0d0f12;
    border: 1px solid var(--panel-line);
    color: var(--text);
    padding: 9px 10px;
    font-family: inherit;
    font-size: 13px;
    border-radius: var(--radius);
  }
  input[type="range"] { width: 100%; }
  .row2 { display: flex; gap: 12px; }
  .row2 > div { flex: 1; }
  .color-row { display: flex; gap: 12px; }
  .color-field {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 8px;
    border: 1px solid var(--panel-line);
    border-radius: var(--radius);
    padding: 6px 8px;
    background: #0d0f12;
  }
  .color-field input[type="color"] {
    width: 28px; height: 28px; border: none; background: none; padding: 0; cursor: pointer;
  }
  .radio-row, .check-row { display: flex; gap: 18px; font-size: 12px; color: var(--text-dim); }
  .radio-row label, .check-row label { display: flex; align-items: center; gap: 6px; margin: 0; }
  button {
    margin-top: 6px;
    padding: 12px;
    background: var(--accent);
    color: #17130a;
    font-weight: bold;
    border: none;
    border-radius: var(--radius);
    font-family: inherit;
    font-size: 14px;
    cursor: pointer;
  }

<?php else: ?>

  html, body { height: 100%; overflow: hidden; background: <?= h($bg_color) ?>; }
  #stage {
    position: relative;
    width: 100vw;
    height: 100vh;
    overflow: hidden;
    background: <?= h($bg_color) ?>;
  }
  #runner {
    position: absolute;
    top: 50%;
    left: 0;
    transform: translateY(-50%) translateX(<?= $direction === 'rtl' ? '100vw' : '-100%' ?>);
    white-space: nowrap;
    font-family: <?= $font_key /* already validated against allow-list */ ?>;
    font-size: <?= (int) $size ?>px;
    font-weight: bold;
    color: <?= h($text_color) ?>;
    <?php if ($glow): ?>
    text-shadow:
      0 0 <?= (int) ($size * 0.12) ?>px <?= h($text_color) ?>,
      0 0 <?= (int) ($size * 0.30) ?>px <?= h($text_color) ?>,
      0 0 <?= (int) ($size * 0.55) ?>px <?= h($text_color) ?>;
    <?php endif; ?>
    animation: run <?= $duration ?>s linear infinite;
  }
  @keyframes run {
    from { transform: translateY(-50%) translateX(<?= $direction === 'rtl' ? '100vw' : '-100%' ?>); }
    to   { transform: translateY(-50%) translateX(<?= $direction === 'rtl' ? '-100%' : '100vw' ?>); }
  }
  #backBtn {
    position: absolute;
    top: 16px;
    right: 16px;
    z-index: 5;
    background: rgba(20,23,27,0.75);
    border: 1px solid var(--panel-line);
    color: var(--text-dim);
    font-family: inherit;
    font-size: 11px;
    padding: 8px 14px;
    border-radius: var(--radius);
    text-decoration: none;
  }
  #backBtn:hover { color: var(--text); }

<?php endif; ?>
</style>
</head>
<body>

<?php if (!$started): ?>

  <form method="get" action="">
    <div>
      <h1>LED RUNNER</h1>
      <p class="sub">Configure your scrolling sign, then start it.</p>
    </div>

    <div>
      <label for="text">Message</label>
      <input type="text" id="text" name="text" value="<?= h($text) ?>" maxlength="200">
    </div>

    <div>
      <label for="font">Font</label>
      <select id="font" name="font">
        <?php foreach ($allowed_fonts as $key => $label): ?>
          <option value="<?= h($key) ?>" <?= $key === $font_key ? 'selected' : '' ?>><?= h($label) ?></option>
        <?php endforeach; ?>
      </select>
    </div>

    <div class="row2">
      <div>
        <label for="size">Size (px)</label>
        <input type="range" id="size" name="size" min="20" max="300" value="<?= (int) $size ?>"
               oninput="sizeOut.value=size.value">
        <output id="sizeOut"><?= (int) $size ?></output>
      </div>
      <div>
        <label for="speed">Speed</label>
        <input type="range" id="speed" name="speed" min="1" max="40" value="<?= (int) $speed ?>"
               oninput="speedOut.value=speed.value">
        <output id="speedOut"><?= (int) $speed ?></output>
      </div>
    </div>

    <div class="color-row">
      <div class="color-field">
        <input type="color" name="text_color" value="<?= h($text_color) ?>">
        <span>Text color</span>
      </div>
      <div class="color-field">
        <input type="color" name="bg_color" value="<?= h($bg_color) ?>">
        <span>Background</span>
      </div>
    </div>

    <div class="radio-row">
      <label><input type="radio" name="direction" value="rtl" <?= $direction === 'rtl' ? 'checked' : '' ?>> Right &rarr; Left</label>
      <label><input type="radio" name="direction" value="ltr" <?= $direction === 'ltr' ? 'checked' : '' ?>> Left &rarr; Right</label>
    </div>

    <div class="check-row">
      <label><input type="checkbox" name="glow" value="1" <?= $glow ? 'checked' : '' ?>> Glow effect</label>
    </div>

    <input type="hidden" name="start" value="1">
    <button type="submit">Start Runner</button>
  </form>

<?php else: ?>

  <div id="stage">
    <a id="backBtn" href="?">&larr; Back to settings</a>
    <div id="runner"><?= h($text) ?></div>
  </div>

<?php endif; ?>

</body>
</html>
