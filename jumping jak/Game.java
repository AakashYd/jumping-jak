package SubwaySurfer;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.File;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.LinearGradientPaint;

public class Game extends JPanel implements ActionListener, KeyListener {
    private int score = 0;
    private boolean isPlaying = false;
    private boolean showIntro = true;
    private Player player;
    private ArrayList<Obstacle> obstacles;
    private Timer timer;
    private Random random;
    private int groundY;
    private Image backgroundImage;
    private Font customFont;
    private ArrayList<PowerUp> powerUps;
    private boolean isInvincible = false;
    private int coinCount = 0;
    private Color skyColor = new Color(135, 206, 235);
    private ArrayList<Cloud> clouds;
    private int distance = 0;
    private boolean isNight = false;
    private long lastPowerUpTime = 0;
    private enum GameMode { CLASSIC, NIGHT, SPEED_RUN, COIN_RUSH }
    private GameMode currentMode = GameMode.CLASSIC;
    private Clip backgroundMusic;
    private Clip jumpSound;
    private Clip coinSound;
    private Character currentCharacter;
    private ArrayList<Particle> particles;
    private ArrayList<Star> backgroundStars;
    private float rainbowOffset = 0;
    private BufferedImage particleImage;
    private Color[] rainbowColors = {
        new Color(255, 0, 0),
        new Color(255, 127, 0),
        new Color(255, 255, 0),
        new Color(0, 255, 0),
        new Color(0, 0, 255),
        new Color(75, 0, 130),
        new Color(148, 0, 211)
    };
    
    public Game() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        // Initialize game objects
        player = new Player();
        obstacles = new ArrayList<>();
        random = new Random();
        groundY = 500;
        
        // Load resources
        loadResources();
        
        // Setup game timer
        timer = new Timer(16, this); // ~60 FPS
        
        powerUps = new ArrayList<>();
        clouds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            clouds.add(new Cloud(random.nextInt(800), random.nextInt(200)));
        }
        
        particles = new ArrayList<>();
        loadSounds();
        initializeCharacters();
        
        // Initialize new visual effect variables
        backgroundStars = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            backgroundStars.add(new Star());
        }
        
        createParticleImage();
    }
    
    private void loadResources() {
        // Load custom font
        try {
            customFont = new Font("Arial", Font.BOLD, 48);
        } catch (Exception e) {
            customFont = new Font("Arial", Font.BOLD, 48);
        }
        
        // Load background image
        backgroundImage = new ImageIcon("background.png").getImage();
    }
    
    private void createParticleImage() {
        particleImage = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = particleImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Create radial gradient for particle
        RadialGradientPaint gradient = new RadialGradientPaint(
            new Point2D.Float(10, 10),
            10,
            new float[]{0f, 1f},
            new Color[]{Color.WHITE, new Color(0, 0, 0, 0)}
        );
        g2.setPaint(gradient);
        g2.fillOval(0, 0, 20, 20);
        g2.dispose();
    }
    
    private void showIntroScreen(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        // Create animated background
        drawAnimatedBackground(g2d);
        
        // Draw title with 3D effect
        String title = "JUMPING JAP";
        g2d.setFont(new Font("Arial", Font.BOLD, 60));
        
        // Draw multiple layers for 3D effect
        for(int i = 20; i > 0; i--) {
            float alpha = i / 20f;
            g2d.setColor(new Color(1f, 0.5f, 0f, alpha * 0.1f));
            g2d.drawString(title, 150 - i, 150 - i);
        }
        
        // Main title with Japanese-style gradient
        GradientPaint titleGradient = new GradientPaint(
            150, 150, new Color(255, 0, 0),  // Red
            450, 150, new Color(255, 255, 255)  // White
        );
        g2d.setPaint(titleGradient);
        g2d.drawString(title, 150, 150);
        
        // Add Japanese subtitle
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        g2d.setColor(Color.WHITE);
        g2d.drawString("ジャンピング・ジャップ", 200, 200);
        
        // Draw creator name with rainbow effect
        drawRainbowText(g2d, "Created by: Aakash Yadav", 200, 300);
        
        // Draw animated "Press SPACE to Start"
        drawPulsatingText(g2d, "Press SPACE to Start", 300, 400);
    }
    
    private void drawAnimatedBackground(Graphics2D g) {
        // Create star field effect
        for(Star star : backgroundStars) {
            star.update();
            star.draw(g);
        }
        
        // Add nebula effect
        GradientPaint nebula = new GradientPaint(
            0, 0, new Color(0, 0, 50, 100),
            getWidth(), getHeight(), new Color(50, 0, 50, 100)
        );
        g.setPaint(nebula);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
    
    private void drawRainbowText(Graphics2D g, String text, int x, int y) {
        g.setFont(customFont);
        for(int i = 0; i < rainbowColors.length; i++) {
            float offset = (rainbowOffset + i) % rainbowColors.length;
            g.setColor(rainbowColors[(int)offset]);
            g.drawString(text, x + i, y + i);
        }
        rainbowOffset += 0.05f;
    }
    
    private void drawPulsatingText(Graphics2D g, String text, int x, int y) {
        float pulse = (float)Math.abs(Math.sin(System.currentTimeMillis() * 0.002));
        float scale = 1 + pulse * 0.2f;
        
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.scale(scale, scale);
        g.setColor(new Color(1f, 1f, 1f, 0.8f + pulse * 0.2f));
        g.drawString(text, -g.getFontMetrics().stringWidth(text)/2, 0);
        g.setTransform(old);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw sky with day/night cycle
        Color currentSky = isNight ? new Color(20, 24, 82) : skyColor;
        g2d.setColor(currentSky);
        g2d.fillRect(0, 0, getWidth(), groundY);
        
        // Draw clouds
        for (Cloud cloud : clouds) {
            cloud.draw(g2d);
        }
        
        // Draw ground with perspective
        GradientPaint groundGradient = new GradientPaint(
            0, groundY, new Color(34, 139, 34),
            0, getHeight(), new Color(28, 115, 28)
        );
        g2d.setPaint(groundGradient);
        g2d.fillRect(0, groundY, getWidth(), getHeight() - groundY);
        
        // Draw player with effects
        if (isInvincible) {
            float alpha = (float) Math.abs(Math.sin(System.currentTimeMillis() * 0.01));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f + alpha * 0.5f));
        }
        player.draw(g2d);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        
        // Draw game elements
        for (Obstacle obstacle : obstacles) {
            obstacle.draw(g2d);
        }
        
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(g2d);
        }
        
        // Draw HUD
        drawHUD(g2d);
    }
    
    private void drawHUD(Graphics2D g) {
        // Score panel
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(10, 10, 200, 70, 10, 10);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 20, 35);
        g.drawString("Coins: " + coinCount, 20, 65);
        
        // Distance
        String distanceStr = String.format("Distance: %dm", distance);
        g.drawString(distanceStr, getWidth() - 150, 30);
        
        // Power-up indicator
        if (isInvincible) {
            g.setColor(Color.YELLOW);
            g.drawString("INVINCIBLE!", getWidth()/2 - 50, 30);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isPlaying || showIntro) return;
        
        // Update player
        player.update();
        
        // Update obstacles
        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle obstacle = obstacles.get(i);
            obstacle.update();
            
            // Check collision
            if (player.getBounds().intersects(obstacle.getBounds())) {
                gameOver();
            }
            
            // Remove off-screen obstacles
            if (obstacle.getX() < -50) {
                obstacles.remove(i);
                score++;
            }
        }
        
        // Add new obstacles
        if (random.nextInt(50) == 0) {
            obstacles.add(new Obstacle(getWidth(), groundY));
        }
        
        repaint();
    }
    
    private void gameOver() {
        isPlaying = false;
        timer.stop();
        JOptionPane.showMessageDialog(this, "Game Over! Score: " + score);
        resetGame();
    }
    
    private void resetGame() {
        score = 0;
        player.reset();
        obstacles.clear();
        showIntro = true;
        repaint();
    }
    
    private void startGame() {
        showIntro = false;
        isPlaying = true;
        timer.start();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (showIntro) {
                startGame();
            } else {
                player.jump();
            }
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    private void loadSounds() {
        try {
            // Load Japanese-style background music
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(
                new File("sounds/japanese_theme.wav")
            );
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioIn);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            
            // Load Japanese-style sound effects
            String[] soundEffects = {
                "jump_ninja.wav",
                "coin_bell.wav",
                "katana_swing.wav",
                "powerup_flute.wav"
            };
            
            for(String sound : soundEffects) {
                audioIn = AudioSystem.getAudioInputStream(new File("sounds/" + sound));
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                soundEffects.put(sound, clip);
            }
        } catch (Exception e) {
            System.out.println("Error loading sounds: " + e.getMessage());
        }
    }
    
    private void initializeCharacters() {
        currentCharacter = new NinjaCharacter(
            "Ninja", 
            new Color(50, 50, 50),
            1.2f,  // Speed multiplier
            1.5f   // Jump multiplier
        );
    }
    
    private void addParticleEffect(int x, int y, Color color) {
        for (int i = 0; i < 10; i++) {
            particles.add(new Particle(x, y, color));
        }
    }
    
    private void updateParticles() {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle particle = particles.get(i);
            particle.update();
            if (particle.isDead()) {
                particles.remove(i);
            }
        }
    }
}

class Player {
    private int x = 100;
    private int y = 400;
    private int yVelocity = 0;
    private static final int GRAVITY = 1;
    private static final int JUMP_FORCE = -20;
    private boolean isJumping = false;
    
    public void update() {
        if (isJumping) {
            yVelocity += GRAVITY;
            y += yVelocity;
            
            if (y >= 400) {
                y = 400;
                isJumping = false;
                yVelocity = 0;
            }
        }
    }
    
    public void jump() {
        if (!isJumping) {
            yVelocity = JUMP_FORCE;
            isJumping = true;
        }
    }
    
    public void draw(Graphics2D g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, 40, 60);
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, 40, 60);
    }
    
    public void reset() {
        x = 100;
        y = 400;
        yVelocity = 0;
        isJumping = false;
    }
}

class Obstacle {
    private int x;
    private int y;
    private static final int SPEED = 5;
    
    public Obstacle(int startX, int groundY) {
        this.x = startX;
        this.y = groundY - 40;
    }
    
    public void update() {
        x -= SPEED;
    }
    
    public void draw(Graphics2D g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, 30, 40);
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, 30, 40);
    }
    
    public int getX() {
        return x;
    }
}

class Cloud {
    private double x, y;
    private int width;
    private double speed;
    
    public Cloud(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = 30 + new Random().nextInt(50);
        this.speed = 0.5 + new Random().nextDouble();
    }
    
    public void update() {
        x -= speed;
        if (x + width < 0) {
            x = 800;
            y = new Random().nextInt(200);
        }
    }
    
    public void draw(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillOval((int)x, (int)y, width, width/2);
    }
}

class PowerUp {
    private int x, y;
    private int type; // 0: coin, 1: invincibility
    private static final int SIZE = 20;
    
    public PowerUp(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
    
    public void draw(Graphics2D g) {
        switch(type) {
            case 0:
                drawKatana(g);
                break;
            case 1:
                drawShuriken(g);
                break;
            case 2:
                drawSakura(g);
                break;
            case 3:
                drawKanji(g);
                break;
            case 4:
                drawManeki(g);
                break;
        }
    }
    
    private void drawKatana(Graphics2D g) {
        // Draw katana with glow effect
        Color glow = new Color(200, 200, 255, 100);
        g.setColor(glow);
        g.fillRoundRect(x-2, y-2, SIZE+4, SIZE+4, 5, 5);
        g.setColor(Color.LIGHT_GRAY);
        g.fillRoundRect(x, y, SIZE, SIZE/4, 2, 2);
    }
    
    private void drawShuriken(Graphics2D g) {
        // Rotating shuriken
        g.rotate(rotation, x + SIZE/2, y + SIZE/2);
        int[] xPoints = new int[4];
        int[] yPoints = new int[4];
        for(int i = 0; i < 4; i++) {
            double angle = i * Math.PI / 2 + rotation;
            xPoints[i] = x + SIZE/2 + (int)(SIZE/2 * Math.cos(angle));
            yPoints[i] = y + SIZE/2 + (int)(SIZE/2 * Math.sin(angle));
        }
        g.setColor(Color.GRAY);
        g.fillPolygon(xPoints, yPoints, 4);
    }
    
    private void drawSakura(Graphics2D g) {
        // Animated sakura petal
        g.setColor(new Color(255, 192, 203));
        for(int i = 0; i < 5; i++) {
            double angle = i * Math.PI * 2 / 5 + rotation;
            int px = x + SIZE/2 + (int)(SIZE/3 * Math.cos(angle));
            int py = y + SIZE/2 + (int)(SIZE/3 * Math.sin(angle));
            g.fillOval(px-2, py-2, 4, 4);
        }
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }
}

class NinjaCharacter extends Character {
    private float rotation = 0;
    private ArrayList<TrailEffect> trails;
    
    public NinjaCharacter(String name, Color color, float speedMult, float jumpMult) {
        super(name, color, speedMult, jumpMult);
        trails = new ArrayList<>();
    }
    
    @Override
    public void draw(Graphics2D g, int x, int y) {
        // Draw ninja character
        g.setColor(color);
        
        // Draw trailing effects
        for(TrailEffect trail : trails) {
            trail.draw(g);
        }
        
        // Save current transform
        AffineTransform old = g.getTransform();
        
        // Rotate character during jump
        if(isJumping) {
            rotation += 0.2;
            g.rotate(rotation, x + 20, y + 30);
        }
        
        // Draw ninja body
        g.fillRoundRect(x, y, 40, 60, 10, 10);
        
        // Draw ninja details
        g.setColor(color.darker());
        // Headband
        g.fillRect(x + 5, y + 10, 30, 5);
        // Ninja mask
        g.fillRect(x + 10, y + 15, 20, 10);
        // Scarf effect
        drawScarf(g, x + 40, y + 15);
        
        // Reset transform
        g.setTransform(old);
        
        // Update trailing effects
        updateTrails();
    }
    
    private void drawScarf(Graphics2D g, int x, int y) {
        // Animated scarf
        float wave = (float)Math.sin(System.currentTimeMillis() * 0.01);
        int[] xPoints = {x, x + 10, x + 20, x + 15};
        int[] yPoints = {y, y + (int)(5 * wave), y + 10, y + 5};
        g.fillPolygon(xPoints, yPoints, 4);
    }
    
    private void updateTrails() {
        if(isJumping) {
            trails.add(new TrailEffect(x, y, color));
        }
        
        // Update and remove old trails
        for(int i = trails.size() - 1; i >= 0; i--) {
            TrailEffect trail = trails.get(i);
            trail.update();
            if(trail.isDead()) {
                trails.remove(i);
            }
        }
    }
}

class Particle {
    private double x, y;
    private double vx, vy;
    private int life;
    private Color color;
    
    public Particle(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
        double angle = Math.random() * Math.PI * 2;
        double speed = Math.random() * 5;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.life = 20 + (int)(Math.random() * 20);
    }
    
    public void update() {
        x += vx;
        y += vy;
        vy += 0.2; // Gravity
        life--;
    }
    
    public void draw(Graphics2D g) {
        int alpha = (life * 255) / 40;
        g.setColor(new Color(color.getRed(), color.getGreen(), 
                           color.getBlue(), Math.max(0, Math.min(255, alpha))));
        g.fillOval((int)x, (int)y, 4, 4);
    }
    
    public boolean isDead() {
        return life <= 0;
    }
}

class Star {
    private float x, y;
    private float speed;
    private float brightness;
    private float size;
    
    public Star() {
        reset();
        x = (float)(Math.random() * 800);
    }
    
    private void reset() {
        x = 800;
        y = (float)(Math.random() * 600);
        speed = 1 + (float)(Math.random() * 5);
        brightness = (float)Math.random();
        size = 1 + (float)(Math.random() * 3);
    }
    
    public void update() {
        x -= speed;
        if(x < 0) reset();
        brightness = (float)Math.abs(Math.sin(System.currentTimeMillis() * 0.001 * speed));
    }
    
    public void draw(Graphics2D g) {
        g.setColor(new Color(1f, 1f, 1f, brightness));
        g.fill(new Ellipse2D.Float(x, y, size, size));
    }
} 