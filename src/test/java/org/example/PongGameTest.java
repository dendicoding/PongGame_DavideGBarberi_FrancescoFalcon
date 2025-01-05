package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.*;

class PongGameTest {
    private PongGame pongGame;

    @BeforeEach
    void setUp() {
        pongGame = new PongGame("Media", "Player1", "Player2");
        JFrame frame = new JFrame(); // Necessario per l'inizializzazione del JPanel
        frame.add(pongGame);
        frame.setSize(800, 600); // Imposta dimensioni per evitare errori di getWidth/getHeight
        frame.setVisible(true);
    }

    @Test
    void testBallSpeedBasedOnDifficulty() {
        pongGame.setBallSpeedBasedOnDifficulty("Facile");
        assertEquals(4, pongGame.ballVelX, "La velocità X della pallina non è corretta per la difficoltà Facile");
        assertEquals(4, pongGame.ballVelY, "La velocità Y della pallina non è corretta per la difficoltà Facile");

        pongGame.setBallSpeedBasedOnDifficulty("Difficile");
        assertEquals(8, pongGame.ballVelX, "La velocità X della pallina non è corretta per la difficoltà Difficile");
        assertEquals(8, pongGame.ballVelY, "La velocità Y della pallina non è corretta per la difficoltà Difficile");
    }

    @Test
    void testScoreIncrementOnBallOutOfBounds() {
        int initialScorePlayer1 = pongGame.scorePlayer1;
        int initialScorePlayer2 = pongGame.scorePlayer2;

        // Simula pallina che esce dal lato sinistro
        pongGame.ballX = -10;
        pongGame.actionPerformed(null); // Aggiorna lo stato del gioco
        assertEquals(initialScorePlayer2 + 1, pongGame.scorePlayer2, "Il punteggio del Player2 non è incrementato correttamente");

        // Simula pallina che esce dal lato destro
        pongGame.ballX = pongGame.getWidth() + 10;
        pongGame.actionPerformed(null); // Aggiorna lo stato del gioco
        assertEquals(initialScorePlayer1 + 1, pongGame.scorePlayer1, "Il punteggio del Player1 non è incrementato correttamente");
    }

    @Test
    void testPaddleMovementUp() {
        int initialPaddle1Y = pongGame.paddle1Y;
        pongGame.keyPressed(new KeyEvent(pongGame, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_W, 'W'));
        assertTrue(pongGame.paddle1Y < initialPaddle1Y, "La racchetta del Player1 non si muove correttamente verso l'alto");
    }

    @Test
    void testPaddleMovementDown(){
        int initialPaddle1Y = pongGame.paddle1Y;
        pongGame.keyPressed(new KeyEvent(pongGame, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_S, 'S'));
        assertTrue(pongGame.paddle1Y > initialPaddle1Y, "La racchetta del Player1 non si muove correttamente verso il basso");
    }

    @Test
    void testPowerUpGeneration() {
        pongGame.generatePowerUp();
        assertNotNull(pongGame.currentPowerUp, "Il power-up non è stato generato correttamente");
    }

    @Test
    void testPowerUpActivation() {
        PongGame.PowerUp powerUp = pongGame.new PowerUp(100, 100, "Racchetta");
        pongGame.currentPowerUp = powerUp;
        pongGame.activatePowerUp();

        assertEquals(180, pongGame.currentPaddleHeight, "L'altezza della racchetta non è corretta dopo l'attivazione del power-up");
        assertEquals("Racchette più grandi! Durata: 5 secondi", pongGame.effectMessage, "Il messaggio di effetto non è corretto");
    }
}