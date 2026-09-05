import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LED Runner - a customizable scrolling LED-style marquee for your laptop
 * screen, built with Java Swing (no external libraries required).
 *
 * Compile:  javac LedRunner.java
 * Run:      java LedRunner
 */
public class LedRunner extends JFrame {

    // ---- Settings controls ----
    private final JTextField textField;
    private final JComboBox<String> fontBox;
    private final JSpinner sizeSpinner;
    private final JCheckBox boldCheck;
    private final JButton textColorBtn;
    private final JButton bgColorBtn;
    private final JSlider speedSlider;
    private final JComboBox<String> directionBox;
    private final JCheckBox glowCheck;

    private Color textColor = new Color(0, 255, 60);   // LED green
    private Color bgColor = Color.BLACK;

    private RunnerWindow runnerWindow;

    public LedRunner() {
        super("LED Runner - Settings");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 480);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        setContentPane(root);

        // ---- Text ----
        textField = new JTextField("WELCOME TO LED RUNNER!");
        root.add(labeled("Runner text:", textField));
        root.add(Box.createVerticalStrut(10));

        // ---- Font ----
        String[] fonts = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        fontBox = new JComboBox<>(fonts);
        selectPreferredFont(fontBox, fonts);
        root.add(labeled("Font:", fontBox));
        root.add(Box.createVerticalStrut(10));

        // ---- Size + Bold ----
        JPanel sizeRow = new JPanel(new BorderLayout(10, 0));
        sizeSpinner = new JSpinner(new SpinnerNumberModel(72, 10, 400, 2));
        boldCheck = new JCheckBox("Bold", true);
        sizeRow.add(labeled("Font size:", sizeSpinner), BorderLayout.CENTER);
        sizeRow.add(boldCheck, BorderLayout.EAST);
        sizeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        sizeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        root.add(sizeRow);
        root.add(Box.createVerticalStrut(10));

        // ---- Colors ----
        JPanel colorRow = new JPanel(new GridLayout(1, 2, 10, 0));
        colorRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        colorRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        textColorBtn = new JButton("Text Color");
        textColorBtn.setBackground(textColor);
        textColorBtn.setOpaque(true);
        textColorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose text color", textColor);
            if (c != null) {
                textColor = c;
                textColorBtn.setBackground(c);
            }
        });

        bgColorBtn = new JButton("Background Color");
        bgColorBtn.setBackground(bgColor);
        bgColorBtn.setOpaque(true);
        bgColorBtn.setForeground(Color.WHITE);
        bgColorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose background color", bgColor);
            if (c != null) {
                bgColor = c;
                bgColorBtn.setBackground(c);
            }
        });

        colorRow.add(textColorBtn);
        colorRow.add(bgColorBtn);
        root.add(colorRow);
        root.add(Box.createVerticalStrut(14));

        // ---- Speed ----
        speedSlider = new JSlider(1, 40, 10);
        speedSlider.setMajorTickSpacing(10);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        root.add(labeled("Speed (pixels per tick):", speedSlider));
        root.add(Box.createVerticalStrut(10));

        // ---- Direction ----
        directionBox = new JComboBox<>(new String[]{"Right to Left", "Left to Right"});
        root.add(labeled("Direction:", directionBox));
        root.add(Box.createVerticalStrut(10));

        // ---- Glow ----
        glowCheck = new JCheckBox("Glow effect", true);
        glowCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(glowCheck);
        root.add(Box.createVerticalStrut(16));

        // ---- Buttons ----
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        JButton startBtn = new JButton("Start Runner");
        JButton stopBtn = new JButton("Stop Runner");
        startBtn.addActionListener(e -> startRunner());
        stopBtn.addActionListener(e -> stopRunner());
        btnRow.add(startBtn);
        btnRow.add(stopBtn);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(btnRow);
        root.add(Box.createVerticalStrut(10));

        JLabel tip = new JLabel("Tip: press ESC in the runner window to close it.");
        tip.setForeground(Color.GRAY);
        tip.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(tip);
    }

    private void selectPreferredFont(JComboBox<String> box, String[] fonts) {
        String preferred = "Courier New";
        for (String f : fonts) {
            if (f.equalsIgnoreCase(preferred)) {
                box.setSelectedItem(f);
                return;
            }
        }
        // fall back to Monospaced if Courier New isn't available on this system
        for (String f : fonts) {
            if (f.equalsIgnoreCase("Monospaced")) {
                box.setSelectedItem(f);
                return;
            }
        }
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height + 30));
        JLabel l = new JLabel(label);
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void startRunner() {
        stopRunner(); // close any existing runner first

        String text = textField.getText();
        if (text == null || text.isEmpty()) {
            text = " ";
        }
        String fontName = (String) fontBox.getSelectedItem();
        int size = (Integer) sizeSpinner.getValue();
        int style = boldCheck.isSelected() ? Font.BOLD : Font.PLAIN;
        Font font = new Font(fontName, style, size);
        int speed = speedSlider.getValue();
        boolean rightToLeft = directionBox.getSelectedIndex() == 0;
        boolean glow = glowCheck.isSelected();

        runnerWindow = new RunnerWindow(text, font, textColor, bgColor, speed, rightToLeft, glow);
        runnerWindow.setVisible(true);
    }

    private void stopRunner() {
        if (runnerWindow != null) {
            runnerWindow.stopAndClose();
            runnerWindow = null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LedRunner().setVisible(true));
    }

    // ------------------------------------------------------------------
    // Fullscreen scrolling display window
    // ------------------------------------------------------------------
    private static class RunnerWindow extends JFrame {
        private final MarqueePanel panel;
        private Timer timer;

        RunnerWindow(String text, Font font, Color textColor, Color bgColor,
                     int speed, boolean rightToLeft, boolean glow) {
            super("LED Runner");
            setUndecorated(true);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            panel = new MarqueePanel(text, font, textColor, bgColor, speed, rightToLeft, glow);
            setContentPane(panel);

            // Fullscreen on the default display
            GraphicsDevice device = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice();
            if (device.isFullScreenSupported()) {
                device.setFullScreenWindow(this);
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                setVisible(true);
            }

            // Esc closes the runner window
            InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = panel.getActionMap();
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeRunner");
            am.put("closeRunner", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    stopAndClose();
                }
            });

            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    panel.resetPosition();
                }
            });

            timer = new Timer(20, e -> {
                panel.advance();
                panel.repaint();
            });
            timer.start();
        }

        void stopAndClose() {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
            GraphicsDevice device = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice();
            if (device.getFullScreenWindow() == this) {
                device.setFullScreenWindow(null);
            }
            dispose();
        }
    }

    // ------------------------------------------------------------------
    // The panel that actually draws and scrolls the text
    // ------------------------------------------------------------------
    private static class MarqueePanel extends JPanel {
        private final String text;
        private final Font font;
        private final Color textColor;
        private final boolean rightToLeft;
        private final int speed;
        private final boolean glow;

        private double x;
        private int textWidth = -1;

        MarqueePanel(String text, Font font, Color textColor, Color bgColor,
                     int speed, boolean rightToLeft, boolean glow) {
            this.text = text;
            this.font = font;
            this.textColor = textColor;
            this.rightToLeft = rightToLeft;
            this.speed = speed;
            this.glow = glow;
            setBackground(bgColor);
            setDoubleBuffered(true);
        }

        void resetPosition() {
            FontMetrics fm = getFontMetrics(font);
            textWidth = fm.stringWidth(text);
            if (rightToLeft) {
                x = getWidth();
            } else {
                x = -textWidth;
            }
        }

        void advance() {
            if (textWidth < 0) {
                resetPosition();
                return;
            }
            if (rightToLeft) {
                x -= speed;
                if (x + textWidth < 0) {
                    x = getWidth();
                }
            } else {
                x += speed;
                if (x > getWidth()) {
                    x = -textWidth;
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (textWidth < 0) {
                resetPosition();
            }

            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int y = getHeight() / 2 + fm.getAscent() / 2 - fm.getDescent() / 2;

            if (glow) {
                // simple layered-glow effect: draw the text several times
                // with a translucent, slightly larger stroke feel using
                // fading alpha, then draw a crisp copy on top.
                Composite original = g2.getComposite();
                for (int i = 6; i >= 1; i--) {
                    float alpha = 0.05f * i;
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    g2.setColor(textColor);
                    for (int dx = -i; dx <= i; dx += i) {
                        for (int dy = -i; dy <= i; dy += i) {
                            if (dx == 0 && dy == 0) continue;
                            g2.drawString(text, (int) x + dx, y + dy);
                        }
                    }
                }
                g2.setComposite(original);
            }

            g2.setColor(textColor);
            g2.drawString(text, (int) x, y);
        }
    }
}
