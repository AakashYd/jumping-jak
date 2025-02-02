class PowerUp {
    private int x, y;
    private PowerUpType type;
    private static final int SIZE = 20;
    private float rotation = 0;
    private Color glowColor;
    
    public PowerUp(int x, int y, PowerUpType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.glowColor = getColorForType(type);
    }
    
    private Color getColorForType(PowerUpType type) {
        switch(type) {
            case COIN: return Color.YELLOW;
            case INVINCIBILITY: return Color.BLUE;
            case DOUBLE_SCORE: return Color.GREEN;
            case MAGNET: return Color.MAGENTA;
            case JETPACK: return Color.ORANGE;
            case SPEED_BOOST: return Color.RED;
            default: return Color.WHITE;
        }
    }
    
    public void draw(Graphics2D g) {
        // Draw glow effect
        int glowSize = SIZE + 10;
        int glowX = x - 5;
        int glowY = y - 5;
        
        AlphaComposite ac = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 
            0.5f + (float)Math.sin(rotation) * 0.2f
        );
        g.setComposite(ac);
        g.setColor(glowColor);
        g.fillOval(glowX, glowY, glowSize, glowSize);
        
        // Draw power-up
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(getColorForType(type));
        
        // Rotate power-up
        AffineTransform old = g.getTransform();
        g.rotate(rotation, x + SIZE/2, y + SIZE/2);
        
        switch(type) {
            case COIN:
                g.fillOval(x, y, SIZE, SIZE);
                break;
            case INVINCIBILITY:
                drawStar(g, x, y, SIZE);
                break;
            case DOUBLE_SCORE:
                drawMultiplier(g, x, y, SIZE);
                break;
            case MAGNET:
                drawMagnet(g, x, y, SIZE);
                break;
            case JETPACK:
                drawJetpack(g, x, y, SIZE);
                break;
            case SPEED_BOOST:
                drawLightning(g, x, y, SIZE);
                break;
        }
        
        g.setTransform(old);
        rotation += 0.05;
    }
    
    // Add methods to draw different power-up shapes...
} 