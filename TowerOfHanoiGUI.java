import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class TowerOfHanoiGUI extends JFrame {

    private final List<Deque<Integer>> towers =
            new ArrayList<>();

    private int moves = 0;
    private int numberOfDisks = 3;

    private JLabel moveLabel;
    private JLabel messageLabel;
    private JSpinner diskSelector;

    private GamePanel gamePanel;

    public TowerOfHanoiGUI() {

        setTitle("Tower of Hanoi Game");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        moveLabel = new JLabel("Moves: 0");

        messageLabel = new JLabel(
                "Drag a disk from one tower to another!"
        );

        diskSelector = new JSpinner(
                new SpinnerNumberModel(3, 1, 8, 1)
        );

        JButton restartButton =
                new JButton("New Game / Restart");

        restartButton.addActionListener(e -> {
            numberOfDisks = (Integer)
                    diskSelector.getValue();
            startGame();
        });

        JPanel topPanel = new JPanel();

        topPanel.add(new JLabel("Number of disks:"));
        topPanel.add(diskSelector);
        topPanel.add(restartButton);

        JPanel bottomPanel = new JPanel();

        bottomPanel.add(moveLabel);
        bottomPanel.add(messageLabel);

        gamePanel = new GamePanel();

        add(topPanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        startGame();

        setVisible(true);
    }

    private void startGame() {

        towers.clear();

        for (int i = 0; i < 3; i++) {
            towers.add(new ArrayDeque<>());
        }

        for (int i = numberOfDisks; i >= 1; i--) {
            towers.get(0).push(i);
        }

        moves = 0;

        moveLabel.setText("Moves: 0");

        messageLabel.setText(
                "Move all disks from Tower A to Tower C!"
        );

        gamePanel.cancelDragging();
        gamePanel.repaint();
    }

    private void moveDisk(int from, int to) {

        if (from == to) {
            messageLabel.setText(
                    "Choose a different tower!"
            );
            return;
        }

        Deque<Integer> source = towers.get(from);
        Deque<Integer> destination = towers.get(to);

        if (source.isEmpty()) {
            messageLabel.setText("Tower is empty!");
            return;
        }

        int disk = source.peek();

        if (!destination.isEmpty()
                && disk > destination.peek()) {

            messageLabel.setText(
                    "Invalid move! A large disk cannot go on a small disk."
            );

            return;
        }

        source.pop();
        destination.push(disk);

        moves++;

        moveLabel.setText("Moves: " + moves);

        if (towers.get(2).size() == numberOfDisks) {

            messageLabel.setText(
                    "Congratulations! You WON!"
            );

            JOptionPane.showMessageDialog(
                    this,
                    "You completed Tower of Hanoi!\n"
                            + "Total moves: " + moves
                            + "\nMinimum moves: "
                            + ((1 << numberOfDisks) - 1),
                    "Congratulations!",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            messageLabel.setText("Great move! Keep going.");
        }
    }

    class GamePanel extends JPanel {

        private int draggingDisk = -1;
        private int sourceTower = -1;

        private int mouseX;
        private int mouseY;

        private int offsetX;
        private int offsetY;

        private final Color[] diskColors = {
                Color.RED,
                Color.ORANGE,
                Color.YELLOW,
                Color.GREEN,
                Color.CYAN,
                Color.BLUE,
                Color.MAGENTA,
                Color.PINK
        };

        GamePanel() {

            setBackground(new Color(245, 245, 245));

            MouseAdapter mouseHandler = new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {

                    if (!SwingUtilities.isLeftMouseButton(e)) {
                        return;
                    }

                    int tower = getTower(e.getX());

                    if (towers.get(tower).isEmpty()) {
                        messageLabel.setText("Tower is empty!");
                        return;
                    }

                    int disk = towers.get(tower).peek();

                    int centerX = getTowerCenter(tower);
                    int baseY = getHeight() - 55;

                    int topY = baseY -
                            towers.get(tower).size() * 30;

                    int width = getDiskWidth(disk);

                    Rectangle diskArea = new Rectangle(
                            centerX - width / 2,
                            topY,
                            width,
                            28
                    );

                    // Select the visible top disk
                    if (diskArea.contains(e.getPoint())) {

                        draggingDisk = disk;
                        sourceTower = tower;

                        offsetX = e.getX() - centerX;
                        offsetY = e.getY() - topY;

                        mouseX = e.getX();
                        mouseY = e.getY();

                        messageLabel.setText(
                                "Disk selected. Drag to another tower."
                        );

                        repaint();
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {

                    if (draggingDisk == -1) {
                        return;
                    }

                    mouseX = e.getX();
                    mouseY = e.getY();

                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {

                    if (draggingDisk == -1) {
                        return;
                    }

                    int destination = getTower(e.getX());
                    int originalTower = sourceTower;

                    // Reset selection before moving
                    draggingDisk = -1;
                    sourceTower = -1;

                    moveDisk(originalTower, destination);

                    repaint();
                }
            };

            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }

        private int getTower(int x) {

            if (x < getWidth() / 3) {
                return 0;
            }

            if (x < 2 * getWidth() / 3) {
                return 1;
            }

            return 2;
        }

        private int getTowerCenter(int tower) {

            return getWidth() * (tower * 2 + 1) / 6;
        }

        private int getDiskWidth(int disk) {

            return 70 + disk * 32;
        }

        void cancelDragging() {

            draggingDisk = -1;
            sourceTower = -1;
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int baseY = getHeight() - 55;

            // Draw the base
            g2.setColor(Color.DARK_GRAY);

            g2.fillRoundRect(
                    40, baseY,
                    getWidth() - 80, 12,
                    10, 10
            );

            // Draw three towers
            for (int t = 0; t < 3; t++) {

                int centerX = getTowerCenter(t);

                g2.setColor(new Color(120, 80, 40));

                g2.fillRoundRect(
                        centerX - 6,
                        50,
                        12,
                        baseY - 50,
                        10,
                        10
                );

                // Tower labels
                g2.setColor(Color.BLACK);

                g2.setFont(
                        new Font("Arial", Font.BOLD, 18)
                );

                g2.drawString(
                        "Tower " + (char) ('A' + t),
                        centerX - 35,
                        baseY + 35
                );

                // Draw disks from bottom to top
                Iterator<Integer> iterator =
                        towers.get(t).descendingIterator();

                int index = 0;

                while (iterator.hasNext()) {

                    int disk = iterator.next();

                    if (t == sourceTower
                            && disk == draggingDisk) {
                        continue;
                    }

                    int width = getDiskWidth(disk);

                    int x = centerX - width / 2;
                    int y = baseY - (index + 1) * 30;

                    drawDisk(g2, disk, x, y, width);

                    index++;
                }
            }

            // Draw disk while dragging
            if (draggingDisk != -1) {

                int width = getDiskWidth(draggingDisk);

                drawDisk(
                        g2,
                        draggingDisk,
                        mouseX - offsetX - width / 2,
                        mouseY - offsetY,
                        width
                );
            }

            g2.dispose();
        }

        private void drawDisk(
                Graphics2D g2,
                int disk,
                int x,
                int y,
                int width) {

            g2.setColor(diskColors[disk - 1]);

            g2.fillRoundRect(
                    x, y, width, 28,
                    15, 15
            );

            g2.setColor(Color.BLACK);

            g2.drawRoundRect(
                    x, y, width, 28,
                    15, 15
            );

            // Disk number
            g2.setFont(
                    new Font("Arial", Font.BOLD, 12)
            );

            g2.drawString(
                    String.valueOf(disk),
                    x + width / 2 - 4,
                    y + 19
            );
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() ->
                new TowerOfHanoiGUI()
        );
    }
}