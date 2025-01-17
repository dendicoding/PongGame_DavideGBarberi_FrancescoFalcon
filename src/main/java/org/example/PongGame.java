package org.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;

public class PongGame extends JPanel implements ActionListener, KeyListener {
    public int ballX = getWidth()/2, ballY = getHeight()/2;
    public int ballVelX, ballVelY;
    public int paddle1Y = 200, paddle2Y = 200;
    private final int paddleWidth = 15, paddleHeight = 130;
    private final Timer timer;


    private long lastCollisionTime = 0;


    public int scorePlayer1 = 0;
    public int scorePlayer2 = 0;
    private String difficulty;
    public PowerUp currentPowerUp = null;
    private int powerUpDuration = 0;
    public int currentPaddleHeight = paddleHeight;
    public String effectMessage = "";
    private int effectMessageDuration = 0;
    private List<Point> ballTrail = new ArrayList<>();
    private Image backgroundImage;

    private String player1Name;
    private String player2Name;

    private String scoreMessage = "";
    private int scoreMessageDuration = 0;

    public PongGame(String difficulty, String player1Name, String player2Name) {
        this.difficulty = difficulty;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.setFocusable(true);
        this.addKeyListener(this);
        resetBall(); // Inizializza la posizione e velocità della pallina
        setBallSpeedBasedOnDifficulty(difficulty); // Imposta la velocità in base alla difficoltà

        backgroundImage = new ImageIcon(getClass().getResource("/bg.png")).getImage();
        timer = new Timer(5, this);
        timer.start();
    }

    public void setBallSpeedBasedOnDifficulty(String difficulty) {
        switch (difficulty) {
            case "Facile": ballVelX = 4; ballVelY = 4; break;
            case "Media": ballVelX = 6; ballVelY = 6; break;
            case "Difficile": ballVelX = 8; ballVelY = 8; break;
            default: ballVelX = 6; ballVelY = 6; break; // Valore predefinito
        }
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);
        drawBallTrail(g);
        drawBall(g);
        drawPaddles(g);
        drawScoresAndNames(g);
        drawPowerUp(g);
        drawMessages(g);
    }

    private void drawBackground(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }

    private void drawBallTrail(Graphics g) {
        g.setColor(new Color(255, 255, 255, 50));
        for (Point p : ballTrail) {
            g.fillOval(p.x, p.y, 5, 5);
        }
    }

    private void drawBall(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillOval(ballX, ballY, 30, 30);
    }

    private void drawPaddles(Graphics g) {
        g.setColor(Color.CYAN);
        g.fillRect(20, paddle1Y, paddleWidth, currentPaddleHeight);
        g.fillRect(getWidth() - 30, paddle2Y, paddleWidth, currentPaddleHeight);
    }

    private void drawScoresAndNames(Graphics g) {
        Font nameFont = new Font("Arial", Font.BOLD, 20);
        Font scoreFont = new Font("Arial", Font.BOLD, 40);
        g.setFont(nameFont);

        FontMetrics nameMetrics = g.getFontMetrics(nameFont);
        FontMetrics scoreMetrics = g.getFontMetrics(scoreFont);

        int player1NameWidth = nameMetrics.stringWidth(player1Name);
        int player1ScoreWidth = scoreMetrics.stringWidth(String.valueOf(scorePlayer1));
        int player1X = 20;
        int player1ScoreX = player1X + player1NameWidth + 10;

        int player2NameWidth = nameMetrics.stringWidth(player2Name);
        int player2ScoreWidth = scoreMetrics.stringWidth(String.valueOf(scorePlayer2));
        int player2X = getWidth() - player2NameWidth - player2ScoreWidth - 30;
        int player2ScoreX = player2X + player2NameWidth + 10;

        g.setColor(Color.WHITE);
        g.drawString(player1Name, player1X, 30);
        g.drawString(player2Name, player2X, 30);
        g.setFont(scoreFont);
        g.drawString(String.valueOf(scorePlayer1), player1ScoreX, 35);
        g.drawString(String.valueOf(scorePlayer2), player2ScoreX, 35);
    }

    private void drawPowerUp(Graphics g) {
        if (currentPowerUp != null) {
            g.setColor(currentPowerUp.getColor());
            g.fillRect(currentPowerUp.getX(), currentPowerUp.getY(), 50, 50);
        }
    }

    private void drawMessages(Graphics g) {
        if (!effectMessage.isEmpty()) drawEffectMessage(g);
        if (!scoreMessage.isEmpty()) drawScoreMessage(g);
    }

    private void drawEffectMessage(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics metrics = g.getFontMetrics();
        int effectMessageWidth = metrics.stringWidth(effectMessage);
        int effectX = (getWidth() - effectMessageWidth) / 2;
        int effectY = getHeight() / 2 - 50;
        g.setColor(Color.WHITE);
        g.drawString(effectMessage, effectX, effectY);
    }

    private void drawScoreMessage(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics metrics = g.getFontMetrics();
        int scoreMessageWidth = metrics.stringWidth(scoreMessage);
        int scoreX = (getWidth() - scoreMessageWidth) / 2;
        int scoreY = getHeight() / 2;
        g.setColor(Color.RED);
        g.drawString(scoreMessage, scoreX, scoreY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGameLogic();
        repaint();
    }

    private void updateGameLogic() {
        updateBallTrail();
        updateBallPosition();
        checkCollisions();
        checkForPowerUp();
        updateMessages();
    }

    private void updateBallTrail() {
        ballTrail.add(new Point(ballX + 7, ballY + 7));
        if (ballTrail.size() > 50) ballTrail.remove(0);
    }

    private void updateBallPosition() {
        ballX += ballVelX;
        ballY += ballVelY;

        if (ballY <= 0 || ballY >= getHeight() - 15) ballVelY = -ballVelY;
    }

    private void checkCollisions() {
        if (checkPaddleCollision()) ballVelX = -ballVelX;
        checkScore();
    }

    private boolean checkPaddleCollision() {
        return (ballX <= 35 && ballY + 10 >= paddle1Y - 5 && ballY <= paddle1Y + currentPaddleHeight + 5) ||
                (ballX >= getWidth() - 50 && ballY + 10 >= paddle2Y - 5 && ballY <= paddle2Y + currentPaddleHeight + 5);
    }

    private void checkScore() {
        if (ballX <= 12) {
            scorePlayer2++;
            showScoreMessage();
            resetBall();
        } else if (ballX >= getWidth() - 12) {
            scorePlayer1++;
            showScoreMessage();
            resetBall();
        }
    }

    private void checkForPowerUp() {
        if (currentPowerUp == null && Math.random() < 0.01) generatePowerUp();

        if (currentPowerUp != null &&
                ballX + 15 >= currentPowerUp.getX() && ballX <= currentPowerUp.getX() + 50 &&
                ballY + 15 >= currentPowerUp.getY() && ballY <= currentPowerUp.getY() + 50) {
            activatePowerUp();
            currentPowerUp = null;
        }

        if (powerUpDuration > 0) {
            powerUpDuration--;
            if (powerUpDuration == 0) deactivatePowerUp();
        }
    }
    private void updateMessages() {
        if (effectMessageDuration > 0) {
            effectMessageDuration--;
            if (effectMessageDuration == 0) {
                effectMessage = "";
            }
        }

        if (scoreMessageDuration > 0) {
            scoreMessageDuration--;
            if (scoreMessageDuration == 0) {
                scoreMessage = "";
            }
        }
    }

    private void resetBall() {
        ballX = getWidth() / 2;
        ballY = getHeight() / 2;
        ballVelX = -ballVelX;
    }
    private void showScoreMessage() {
        scoreMessage = "Punto!";
        scoreMessageDuration = 100; // Durata del messaggio
    }


    public void generatePowerUp() {
        Random rand = new Random();
        int x = rand.nextInt(getWidth() - 60) + 40;
        int y = rand.nextInt(getHeight() - 60) + 40;
        String type = rand.nextInt(2) == 0 ? "Velocità" : "Racchetta";
        currentPowerUp = new PowerUp(x, y, type);
    }

    public void activatePowerUp() {
        if (currentPowerUp != null) {
            if (currentPowerUp.getType().equals("Velocità")) {
                ballVelX *= 1.5;
                ballVelY *= 1.5;
                effectMessage = "Velocità 2x! Durata: 5 secondi";
            } else if (currentPowerUp.getType().equals("Racchetta")) {
                currentPaddleHeight = 180;
                effectMessage = "Racchette più grandi! Durata: 5 secondi";
            }
            effectMessageDuration = 100;
            powerUpDuration = 500;
        }
    }

    private void deactivatePowerUp() {
        int speed = 0;
        switch (difficulty) {
            case "Facile": speed = 4; break;
            case "Media": speed = 6; break;
            case "Difficile": speed = 8; break;
        }
        ballVelX = ballVelX < 0 ? -speed : speed; // Mantiene la direzione originale
        ballVelY = ballVelY < 0 ? -speed : speed; // Mantiene la direzione originale
        currentPaddleHeight = paddleHeight; // Reimposta l'altezza della racchetta
    }


    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_W && paddle1Y > 0) paddle1Y -= 10;
        if (e.getKeyCode() == KeyEvent.VK_S && paddle1Y < getHeight() - currentPaddleHeight) paddle1Y += 10;
        if (e.getKeyCode() == KeyEvent.VK_UP && paddle2Y > 0) paddle2Y -= 10;
        if (e.getKeyCode() == KeyEvent.VK_DOWN && paddle2Y < getHeight() - currentPaddleHeight) paddle2Y += 10;
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        class StyledButton extends JButton {
            private Color hoverColor = new Color(60, 179, 113);
            private boolean isHovered = false;

            public StyledButton(String text) {
                super(text);
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.BOLD, 20));
                setPreferredSize(new Dimension(200, 50));

                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isHovered) {
                    g2.setColor(hoverColor);
                } else {
                    g2.setColor(new Color(31, 118, 175));
                }

                RoundRectangle2D.Float rect = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15);
                g2.fill(rect);

                super.paintComponent(g);
                g2.dispose();
            }
        }

        JFrame menuFrame = new JFrame("Pong Game Menu");
        menuFrame.setUndecorated(true);

        JPanel menuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(44, 62, 80),
                        0, getHeight(), new Color(52, 152, 219)
                );

                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel titleLabel = new JLabel("PONG");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);

        //titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Crea i JTextField per i nomi dei giocatori
        JTextField player1NameField = new JTextField("Davide Barberi");
        JTextField player2NameField = new JTextField("Francesco Falcon");

        player1NameField.setPreferredSize(new Dimension(200, 30));
        player2NameField.setPreferredSize(new Dimension(200, 30));

        StyledButton playButton = new StyledButton("Gioca");

        StyledButton exitButton = new StyledButton("Esci");



        JLabel player1Label = new JLabel("Giocatore 1:");
        player1Label.setFont(new Font("Arial", Font.ITALIC, 20));
        player1Label.setForeground(new Color(230, 230, 250)); // Colore Lavanda

        JLabel player2Label = new JLabel("Giocatore 2:");
        player2Label.setFont(new Font("Arial", Font.ITALIC, 20));
        player2Label.setForeground(new Color(230, 230, 250)); // Colore Lavanda

        menuPanel.add(titleLabel);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        menuPanel.add(player1Label);
        menuPanel.add(player1NameField);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        menuPanel.add(player2Label);
        menuPanel.add(player2NameField);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        menuPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        menuPanel.add(playButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        menuPanel.add(exitButton);

        //playButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        //exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        playButton.addActionListener(e -> {
            // Ottieni i nomi dei giocatori dai campi di testo
            String player1Name = player1NameField.getText();
            String player2Name = player2NameField.getText();

            String[] difficulties = {"Facile", "Media", "Difficile"};

            UIManager.put("OptionPane.background", new Color(44, 62, 80));
            UIManager.put("Panel.background", new Color(44, 62, 80));
            UIManager.put("OptionPane.messageForeground", Color.WHITE);

            String difficulty = (String) JOptionPane.showInputDialog(
                    null,
                    "Scegli la difficoltà:",
                    "Pong Game - Difficoltà",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    difficulties,
                    difficulties[1]
            );

            if (difficulty != null) {
                JFrame gameFrame = new JFrame("Pong Game");
                gameFrame.setUndecorated(true);

                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice gd = ge.getDefaultScreenDevice();

                gameFrame.setResizable(false);

                PongGame game = new PongGame(difficulty, player1Name, player2Name); // Passa i nomi dei giocatori
                gameFrame.add(game);

                game.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                            gd.setFullScreenWindow(null);
                            System.exit(0);
                        }
                    }
                });

                gd.setFullScreenWindow(gameFrame);
                menuFrame.dispose();
            }
        });



        exitButton.addActionListener(e -> System.exit(0));

        menuFrame.add(menuPanel);
        menuFrame.setSize(400, 500);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setVisible(true);
    }

    class PowerUp {
        private int x, y;
        private String type;

        public PowerUp(int x, int y, String type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public String getType() { return type; }

        public Color getColor() {
            if (type.equals("Speed")) return Color.RED;
            else if (type.equals("Paddle")) return Color.GREEN;
            return Color.YELLOW;
        }
    }
}
