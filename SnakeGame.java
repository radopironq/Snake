package snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class SnakeGame extends JFrame {

    private static final int BOARD_SIZE = 650;

    public SnakeGame() {
        setTitle("Snake Game");
        setSize(BOARD_SIZE, BOARD_SIZE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        add(new GamePanel(BOARD_SIZE));
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SnakeGame::new);
    }
}

class GamePanel extends JPanel implements ActionListener {

    private static final int TILE_SIZE = 25;
    private final int BOARD_SIZE;
    private Timer timer;
    private Snake snake;
    private Food food;
    private PowerUp powerUp;
    private int score = 0;
    private boolean running = false;
    private int gameSpeed = 150;
    private int applesEaten = 0;
    private long powerUpStartTime = 0;
    private static final int POWER_UP_DURATION = 5000;
    private Color backgroundColor = Color.BLACK;
    private boolean colorToggle = false;
    private Color effectColor = Color.BLACK;

    public GamePanel(int boardSize) {
        this.BOARD_SIZE = boardSize;
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
        setBackground(backgroundColor);
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                snake.changeDirection(e);
            }
        });
        initGame();
    }

    private void initGame() {
        JButton startButton = new JButton("Start");
        JButton pauseButton = new JButton("Pause");
        JButton finishButton = new JButton("Finish");

        startButton.addActionListener(e -> startGame());
        pauseButton.addActionListener(e -> pauseGame());
        finishButton.addActionListener(e -> finishGame());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(finishButton);

        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.SOUTH);

        timer = new Timer(gameSpeed, this);
        snake = new Snake(BOARD_SIZE);
        food = new Food(snake, BOARD_SIZE);
        powerUp = new PowerUp(snake, BOARD_SIZE);

        spawnFood();
        spawnPowerUp();
    }

    private void startGame() {
        running = true;
        score = 0;
        snake.reset();
        spawnFood();
        spawnPowerUp();
        timer.start();
        requestFocusInWindow();
        repaint();
    }

    private void pauseGame() {
        running = false;
        timer.stop();
    }

    private void finishGame() {
        running = false;
        timer.stop();
        JOptionPane.showMessageDialog(this, "Game Over. Your score: " + score, "Game Finished", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            snake.move();

            if (snake.checkFood(food)) {
                score++;
                snake.grow();
                spawnFood();
            }

            if (snake.checkPowerUp(powerUp)) {
                spawnPowerUp();
                powerUp.activateEffect(this);
                powerUpStartTime = System.currentTimeMillis();
            }

            if (System.currentTimeMillis() - powerUpStartTime > POWER_UP_DURATION) {
                resetGameSpeed();
                resetBackgroundColor();
            }

            if (snake.checkCollision()) {
                running = false;
                timer.stop();
            }

            if (effectColor.equals(new Color(128, 0, 128)) || effectColor.equals(new Color(0, 50, 0))) {
                colorToggle = !colorToggle;
                backgroundColor = colorToggle ? effectColor : Color.BLACK;
            }
        }
        repaint();
    }

    private void resetGameSpeed() {
        setGameSpeed(150);
    }

    private void resetBackgroundColor() {
        effectColor = Color.BLACK;
    }

    private void spawnFood() {
        food.spawn();
    }

    private void spawnPowerUp() {
        powerUp.spawn();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(backgroundColor);

        if (running) {
            food.draw(g);
            powerUp.draw(g);
            snake.draw(g);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Score: " + score, 10, 20);
        } else {
            gameOver(g);
        }
    }

    private void gameOver(Graphics g) {
        String msg = "Game Over";
        String scoreMsg = "Your score: " + score;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString(msg, (BOARD_SIZE - metrics.stringWidth(msg)) / 2, BOARD_SIZE / 2 - 20);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString(scoreMsg, (BOARD_SIZE - metrics.stringWidth(scoreMsg)) / 2, BOARD_SIZE / 2 + 20);
    }

    public void setGameSpeed(int speed) {
        this.gameSpeed = speed;
        timer.setDelay(gameSpeed);
    }

    public void setEffectColor(Color color) {
        this.effectColor = color;
    }
}

class Snake {
    private static final int TILE_SIZE = 25;
    private final int BOARD_SIZE;

    private int[] x;
    private int[] y;
    private int bodyParts = 3;
    private char direction = 'R';

    public Snake(int boardSize) {
        this.BOARD_SIZE = boardSize;
        this.x = new int[BOARD_SIZE / TILE_SIZE * BOARD_SIZE / TILE_SIZE];
        this.y = new int[BOARD_SIZE / TILE_SIZE * BOARD_SIZE / TILE_SIZE];
    }

    public void reset() {
        bodyParts = 3;
        direction = 'R';
        for (int i = 0; i < bodyParts; i++) {
            x[i] = 100 - i * TILE_SIZE;
            y[i] = 100;
        }
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U': y[0] -= TILE_SIZE; break;
            case 'D': y[0] += TILE_SIZE; break;
            case 'L': x[0] -= TILE_SIZE; break;
            case 'R': x[0] += TILE_SIZE; break;
        }
    }

    public void changeDirection(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: if (direction != 'D') direction = 'U'; break;
            case KeyEvent.VK_S: if (direction != 'U') direction = 'D'; break;
            case KeyEvent.VK_A: if (direction != 'R') direction = 'L'; break;
            case KeyEvent.VK_D: if (direction != 'L') direction = 'R'; break;
        }
    }

    public boolean checkFood(Food food) {
        return x[0] == food.getX() && y[0] == food.getY();
    }

    public boolean checkPowerUp(PowerUp powerUp) {
        return x[0] == powerUp.getX() && y[0] == powerUp.getY();
    }

    public boolean checkCollision() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                return true;
            }
        }

        if (x[0] < 0 || x[0] >= BOARD_SIZE || y[0] < 0 || y[0] >= BOARD_SIZE) {
            return true;
        }
        return false;
    }

    public void draw(Graphics g) {
        for (int i = 0; i < bodyParts; i++) {
            g.setColor(i == 0 ? Color.GREEN : Color.LIGHT_GRAY);
            g.fillRect(x[i], y[i], TILE_SIZE, TILE_SIZE);
        }
    }

    public int getX(int i) {
        return x[i];
    }

    public int getY(int i) {
        return y[i];
    }

    public int getBodyParts() {
        return bodyParts;
    }

    public void grow() {
        bodyParts++;
        x[bodyParts - 1] = x[bodyParts - 2];
        y[bodyParts - 1] = y[bodyParts - 2];
    }
}

class Food {
    private static final int TILE_SIZE = 25;
    private int x, y;
    private final Snake snake;
    private final int BOARD_SIZE;

    public Food(Snake snake, int boardSize) {
        this.snake = snake;
        this.BOARD_SIZE = boardSize;
    }

    public void spawn() {
        Random random = new Random();
        boolean foodOnSnake;
        int attempts = 0;
        int maxAttempts = 50;
        
        do {
            foodOnSnake = false;
            x = random.nextInt(550 / TILE_SIZE) * TILE_SIZE;
            y = random.nextInt(550 / TILE_SIZE) * TILE_SIZE;

            for (int i = 0; i < snake.getBodyParts(); i++) {
                if (snake.getX(i) == x && snake.getY(i) == y) {
                    foodOnSnake = true;
                    break;
                }
            }

            attempts++;
            if (attempts >= maxAttempts) {
                break;
            }
        } while (foodOnSnake);
    }
    
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(x, y, TILE_SIZE, TILE_SIZE);
    }
}

class PowerUp {
    private static final int TILE_SIZE = 25;
    private int x, y;
    private final Snake snake;
    private final int BOARD_SIZE;
    private static final Color PURPLE = new Color(128, 0, 128);

    public PowerUp(Snake snake, int boardSize) {
        this.snake = snake;
        this.BOARD_SIZE = boardSize;
    }

    public void spawn() {
        Random random = new Random();
        boolean powerUpOnSnake;
        do {
            powerUpOnSnake = false;
            x = random.nextInt(550 / TILE_SIZE) * TILE_SIZE;
            y = random.nextInt(550 / TILE_SIZE) * TILE_SIZE;

            for (int i = 0; i < snake.getBodyParts(); i++) {
                if (snake.getX(i) == x && snake.getY(i) == y) {
                    powerUpOnSnake = true;
                    break;
                }
            }
        } while (powerUpOnSnake);
    }

    public void activateEffect(GamePanel gamePanel) {
        Random random = new Random();
        int effect = random.nextInt(2);
        if (effect == 0) {
            gamePanel.setGameSpeed(100);
            gamePanel.setEffectColor(PURPLE);
        } else {
            gamePanel.setGameSpeed(200);
            gamePanel.setEffectColor(new Color(0, 50, 0));
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.CYAN);
        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
