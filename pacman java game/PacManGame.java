// Enhanced Pac-Man Game with levels, score saving, restart, ghost AI, etc.
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class PacManGame extends JPanel implements ActionListener, KeyListener {
    private final int TILE_SIZE = 24;
    private final int ROWS = 20;
    private final int COLS = 20;
    private final int[][] maze = new int[ROWS][COLS];
    private Timer timer;

    private int pacmanX = 1, pacmanY = 1;
    private int pacmanDX = 0, pacmanDY = 0;
    private int score = 0;
    private int lives = 3;
    private int level = 1;
    private int highScore = 0;

    private final ArrayList<Point> ghosts = new ArrayList<>();
    private final Color[] ghostColors = {Color.RED, Color.PINK, Color.CYAN, Color.MAGENTA, Color.ORANGE};
    private final Random rand = new Random();
    private boolean ghostsVulnerable = false;
    private int vulnerableCounter = 0;

    public PacManGame() {
        setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE + 30));
        setBackground(new Color(25, 25, 25));
        setFocusable(true);
        addKeyListener(this);
        loadHighScore();
        initMaze();

        ghosts.add(new Point(10, 10));
        ghosts.add(new Point(15, 15));
        ghosts.add(new Point(5, 12));
        ghosts.add(new Point(3, 17));
        ghosts.add(new Point(17, 3));

        timer = new Timer(150, this);
        timer.start();
    }

    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"))) {
            highScore = Integer.parseInt(reader.readLine());
        } catch (Exception e) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
        try (PrintWriter writer = new PrintWriter("highscore.txt")) {
            writer.println(Math.max(score, highScore));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMaze() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (row == 0 || col == 0 || row == ROWS - 1 || col == COLS - 1 || (row % 2 == 0 && col % 2 == 0)) {
                    maze[row][col] = 1;
                } else if ((row + col) % 7 == 0) {
                    maze[row][col] = 3;
                } else {
                    maze[row][col] = 2;
                }
            }
        }
        maze[pacmanY][pacmanX] = 0;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);
        drawMaze(g);
        drawPacMan(g);
        drawGhosts(g);
        drawHUD(g);
    }

    private void drawBackground(Graphics g) {
        g.setColor(new Color(35, 35, 35));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawMaze(Graphics g) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = col * TILE_SIZE;
                int y = row * TILE_SIZE + 30;

                if (maze[row][col] == 1) {
                    g.setColor(new Color(0, 102, 204));
                    g.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, 6, 6);
                } else if (maze[row][col] == 2) {
                    g.setColor(Color.WHITE);
                    g.fillOval(x + TILE_SIZE / 3, y + TILE_SIZE / 3, TILE_SIZE / 4, TILE_SIZE / 4);
                } else if (maze[row][col] == 3) {
                    g.setColor(Color.YELLOW);
                    g.fillOval(x + TILE_SIZE / 4, y + TILE_SIZE / 4, TILE_SIZE / 2, TILE_SIZE / 2);
                }
            }
        }
    }

    private void drawPacMan(Graphics g) {
        int x = pacmanX * TILE_SIZE;
        int y = pacmanY * TILE_SIZE + 30;
        g.setColor(Color.YELLOW);
        g.fillArc(x, y, TILE_SIZE, TILE_SIZE, 30, 300);
    }

    private void drawGhosts(Graphics g) {
        for (int i = 0; i < ghosts.size(); i++) {
            Point ghost = ghosts.get(i);
            g.setColor(ghostsVulnerable ? Color.BLUE : ghostColors[i % ghostColors.length]);
            int x = ghost.x * TILE_SIZE;
            int y = ghost.y * TILE_SIZE + 30;
            g.fillOval(x, y, TILE_SIZE, TILE_SIZE);
            g.setColor(Color.WHITE);
            g.fillOval(x + 6, y + 6, 5, 5);
            g.fillOval(x + 12, y + 6, 5, 5);
        }
    }

    private void drawHUD(Graphics g) {
        g.setColor(new Color(0, 255, 127));
        g.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g.drawString("Score: " + score + "  Lives: " + lives + "  Level: " + level + "  High Score: " + highScore, 10, 22);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        movePacMan();
        moveGhosts();
        checkCollision();
        repaint();

        if (ghostsVulnerable) {
            vulnerableCounter--;
            if (vulnerableCounter <= 0) ghostsVulnerable = false;
        }

        if (checkWinCondition()) {
            level++;
            initMaze();
            pacmanX = 1;
            pacmanY = 1;
            ghostsVulnerable = false;
            JOptionPane.showMessageDialog(this, "🎉 Level Up! Welcome to Level " + level);
        }
    }

    private boolean checkWinCondition() {
        for (int[] row : maze) {
            for (int cell : row) {
                if (cell == 2 || cell == 3) return false;
            }
        }
        return true;
    }

    private void movePacMan() {
        int newX = pacmanX + pacmanDX;
        int newY = pacmanY + pacmanDY;
        if (maze[newY][newX] != 1) {
            pacmanX = newX;
            pacmanY = newY;
            if (maze[pacmanY][pacmanX] == 2) {
                maze[pacmanY][pacmanX] = 0;
                score++;
                if (score % 100 == 0) lives++;
            } else if (maze[pacmanY][pacmanX] == 3) {
                maze[pacmanY][pacmanX] = 0;
                score += 5;
                ghostsVulnerable = true;
                vulnerableCounter = 30;
            }
        }
    }

    private void moveGhosts() {
        for (Point ghost : ghosts) {
            int dx = Integer.compare(pacmanX, ghost.x);
            int dy = Integer.compare(pacmanY, ghost.y);
            int newX = ghost.x + dx;
            int newY = ghost.y + dy;
            if (maze[newY][newX] != 1) {
                ghost.setLocation(newX, newY);
            }
        }
    }

    private void checkCollision() {
        for (int i = 0; i < ghosts.size(); i++) {
            Point ghost = ghosts.get(i);
            if (pacmanX == ghost.x && pacmanY == ghost.y) {
                if (ghostsVulnerable) {
                    ghosts.set(i, new Point(10, 10));
                    score += 10;
                } else {
                    lives--;
                    if (lives <= 0) {
                        saveHighScore();
                        timer.stop();
                        JOptionPane.showMessageDialog(this, "Game Over! Final Score: " + score);
                        System.exit(0);
                    } else {
                        pacmanX = 1;
                        pacmanY = 1;
                    }
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            pacmanDX = -1; pacmanDY = 0;
        } else if (key == KeyEvent.VK_RIGHT) {
            pacmanDX = 1; pacmanDY = 0;
        } else if (key == KeyEvent.VK_UP) {
            pacmanDX = 0; pacmanDY = -1;
        } else if (key == KeyEvent.VK_DOWN) {
            pacmanDX = 0; pacmanDY = 1;
        } else if (key == KeyEvent.VK_R) {
            restartGame();
        }
    }

    private void restartGame() {
        pacmanX = 1;
        pacmanY = 1;
        pacmanDX = 0;
        pacmanDY = 0;
        score = 0;
        lives = 3;
        level = 1;
        ghostsVulnerable = false;
        initMaze();
        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pac-Man Game");
        PacManGame game = new PacManGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
