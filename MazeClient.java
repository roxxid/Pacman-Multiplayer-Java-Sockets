/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maze;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author Daniel
 */
public class MazeClient extends JFrame {
    
    private static int PORT = 3000;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean ingame = false;
    
    private int regdx, regdy, viewgdx, viewgdy;
    
    private void setPositions(Socket socket) {
        this.socket = socket;
        try {
                in = new BufferedReader( new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                while(true) {
                    String command = in.readLine();
                    if (command.startsWith("MOVE")) {
                        String moveLocationx = command.substring(5);
                        String moveLocationy = command.substring(5);
                        String sRdx = moveLocationx.substring(0, moveLocationx.lastIndexOf("."));
                        String sRdy = moveLocationy.substring(moveLocationy.lastIndexOf(".") + 1, moveLocationy.length());
                        regdx = Integer.parseInt(sRdx);
                        regdy = Integer.parseInt(sRdy);
                    }
                }
                //System.out.println("Ingame= " + ingame);
                
        } catch (IOException e) {
            System.out.println("Player died: " + e);
        }
    }
    
    public MazeClient(String serverAddress) throws Exception {
        
        
        try {
            socket = new Socket(serverAddress, PORT);
            initUI();
            while (true){
                setPositions(socket);
                System.out.println("Still inside");
            }
        } finally {
            System.out.println("Going out");
            socket.close();
        }
    }
    
    private void initUI() {
        
        add(new Board());
        setTitle("Client-Maze");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(380, 420);
        setLocationRelativeTo(null);
        setVisible(true);        
    }

    
    public static void main(String[] args) throws Exception {
            MazeClient client = new MazeClient("172.20.10.6");
    }
    
    public class Board extends JPanel implements ActionListener {

    private Dimension d;
    private final Font smallfont = new Font("Helvetica", Font.BOLD, 14);

    private Image ii;
    private final Color dotcolor = new Color(192, 192, 0);
    private Color mazecolor;

    
    private boolean dying = false;

    private final int blocksize = 24;
    private final int nrofblocks = 15;
    private final int scrsize = nrofblocks * blocksize;
    private final int pacanimdelay = 2;
    private final int pacmananimcount = 0;
    private final int maxghosts = 12;
    private final int pacmanspeed = 6;

    private int pacanimcount = pacanimdelay;
    private int pacanimdir = 1;
    private int pacmananimpos = 0;
    private int nrofghosts = 6;
    private int score, scoreTwo;
    private int[] dx, dy;
    private int ghostx, ghosty, ghostdx, ghostdy;

    private Image ghost;
    private Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down;
    private Image pacman3up, pacman3down, pacman3left, pacman3right;
    private Image pacman4up, pacman4down, pacman4left, pacman4right;

    private int pacmanx, pacmany, pacmandx, pacmandy;
    private int reqdx, reqdy, viewdx, viewdy;

    private final short leveldata[] = {
        19, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        25,16, 26, 26, 26, 26, 26, 18, 26, 26, 26, 26, 26, 22, 0,
        0, 21, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 21, 0,
        0, 21, 0, 19, 18, 18, 18, 16, 18, 18, 18, 22, 0, 21, 0,
        0, 21, 0, 17, 16, 16, 16, 24, 16, 16, 16, 20, 0, 21, 0,
        0, 21, 0, 17, 16, 16, 20, 0, 17, 16, 16, 20, 0, 21, 0,
        0, 21, 0, 17, 16, 24, 28, 0, 25, 24, 16, 20, 0, 21, 0,
        0, 17, 26, 16, 20, 0, 0, 0, 0, 0, 17, 20, 0, 21, 0,
        0, 21, 0, 17, 16, 18, 18, 18, 18, 18, 16, 20, 0, 21, 0,
        0, 21, 0, 25, 24, 24, 24, 24, 24, 16, 16, 20, 0, 21, 0,
        0, 21, 0, 0, 0, 0, 0, 0, 0, 17, 16, 20, 0, 21, 0,
        0, 21, 0, 27, 26, 26, 26, 22, 0, 25, 24, 24, 26, 20, 0,
        0, 21, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 21, 0,
        0, 25, 26, 26, 26, 26, 26, 24, 26, 26, 26, 26, 26, 16, 22,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 28
    };

    private final int validspeeds[] = {1, 2, 3, 4, 6, 8};
    private final int maxspeed = 6;

    private int currentspeed = 3;
    private short[] screendata;
    private Timer timer;

    public Board() {        //Method being called

        loadImages();
        initVariables();
        
        addKeyListener(new TAdapter());

        setFocusable(true);

        setBackground(Color.black);
        setDoubleBuffered(true);
    }

    private void initVariables() {

        screendata = new short[nrofblocks * nrofblocks];
        mazecolor = new Color(5, 100, 5);
        d = new Dimension(400, 400);
        dx = new int[4];
        dy = new int[4];
        
        timer = new Timer(40, this);
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();

        initGame();
    }

    private void playGame(Graphics2D g2d) {
        movePacman();
        drawPacman(g2d);
        drawGhost(g2d);
        moveGhosts();
        checkMaze();
    }

    private void showIntroScreen(Graphics2D g2d) {

        g2d.setColor(new Color(0, 32, 48));
        g2d.fillRect(50, scrsize / 2 - 30, scrsize - 100, 50);
        g2d.setColor(Color.white);
        g2d.drawRect(50, scrsize / 2 - 30, scrsize - 100, 50);

        String s = "Press s to start.";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);

        g2d.setColor(Color.white);
        g2d.setFont(small);
        g2d.drawString(s, (scrsize - metr.stringWidth(s)) / 2, scrsize / 2);
    }

    private void drawScore(Graphics2D g) {

        int i;
        String s;

        g.setFont(smallfont);
        g.setColor(new Color(96, 128, 255));
        s = "Score: " + score;
        g.drawString(s, scrsize / 2, scrsize + 16);
    }
    
    private void drawScoreTwo(Graphics2D g) {

        int i;
        String s;

        g.setFont(smallfont);
        g.setColor(new Color(255,218,68));
        s = "Score: " + scoreTwo;
        g.drawString(s, scrsize / 2 + 96, scrsize + 16);
    }

    private void checkMaze() {

        short i = 0;
        boolean finished = true;

        while (i < nrofblocks * nrofblocks && finished) {

            if ((screendata[i] & 48) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {

            initLevel();
        }
    }

    private void moveGhosts() {
        //private int regdx, regdy, viewgdx, viewgdy;
        short ch;
        int pos;
        
        if (regdx == -ghostdx && regdy == -ghostdy) {
            ghostdx = regdx;
            ghostdy = regdy;
            viewgdx = ghostdx;
            viewgdy = ghostdy;
        }
        
        if (ghostx % blocksize == 0 && ghosty % blocksize == 0) {
            pos = ghostx / blocksize + nrofblocks * (int) (ghosty / blocksize);
            ch = screendata[pos];

            if ((ch & 16) != 0) {
                screendata[pos] = (short) (ch & 15);
                scoreTwo++;
            }

            if (regdx != 0 || regdy != 0) {
                if (!((regdx == -1 && regdy == 0 && (ch & 1) != 0)
                        || (regdx == 1 && regdy == 0 && (ch & 4) != 0)
                        || (regdx == 0 && regdy == -1 && (ch & 2) != 0)
                        || (regdx == 0 && regdy == 1 && (ch & 8) != 0))) {
                    ghostdx = regdx;
                    ghostdy = regdy;
                    viewgdx = ghostdx;
                    viewgdy = ghostdy;
                }
            }

            // Check for standstill
            if ((ghostdx == -1 && ghostdy == 0 && (ch & 1) != 0)
                    || (ghostdx == 1 && ghostdy == 0 && (ch & 4) != 0)
                    || (ghostdx == 0 && ghostdy == -1 && (ch & 2) != 0)
                    || (ghostdx == 0 && ghostdy == 1 && (ch & 8) != 0)) {
                ghostdx = 0;
                ghostdy = 0;
            }
        }
        ghostx = ghostx + pacmanspeed * ghostdx;
        ghosty = ghosty + pacmanspeed * ghostdy;
        
    }

    private void drawGhost(Graphics2D g2d) {

        if (viewgdx == -1) {
            drawPacnanLeft(g2d);
        } else if (viewgdx == 1) {
            drawGhostRight(g2d);
        } else if (viewgdy == -1) {
            drawGhostUp(g2d);
        } else {
            drawGhostDown(g2d);
        }
    }

    private void drawGhostUp(Graphics2D g2d) {
        g2d.drawImage(ghost, ghostx + 1, ghosty + 1, this);    
    }

    private void drawGhostDown(Graphics2D g2d) {
        g2d.drawImage(ghost, ghostx + 1, ghosty + 1, this);        
    }

    private void drawGhostLeft(Graphics2D g2d) {

        g2d.drawImage(ghost, ghostx + 1, ghosty + 1, this);
    }

    private void drawGhostRight(Graphics2D g2d) {

        g2d.drawImage(ghost, ghostx + 1, ghosty + 1, this);
    }

    private void movePacman() {

        int pos;
        short ch;

        if (reqdx == -pacmandx && reqdy == -pacmandy) {
            pacmandx = reqdx;
            pacmandy = reqdy;
            viewdx = pacmandx;
            viewdy = pacmandy;
        }

        if (pacmanx % blocksize == 0 && pacmany % blocksize == 0) {
            pos = pacmanx / blocksize + nrofblocks * (int) (pacmany / blocksize);
            ch = screendata[pos];

            if ((ch & 16) != 0) {
                screendata[pos] = (short) (ch & 15);
                score++;
            }

            if (reqdx != 0 || reqdy != 0) {
                if (!((reqdx == -1 && reqdy == 0 && (ch & 1) != 0)
                        || (reqdx == 1 && reqdy == 0 && (ch & 4) != 0)
                        || (reqdx == 0 && reqdy == -1 && (ch & 2) != 0)
                        || (reqdx == 0 && reqdy == 1 && (ch & 8) != 0))) {
                    pacmandx = reqdx;
                    pacmandy = reqdy;
                    viewdx = pacmandx;
                    viewdy = pacmandy;
                }
            }

            // Check for standstill
            if ((pacmandx == -1 && pacmandy == 0 && (ch & 1) != 0)
                    || (pacmandx == 1 && pacmandy == 0 && (ch & 4) != 0)
                    || (pacmandx == 0 && pacmandy == -1 && (ch & 2) != 0)
                    || (pacmandx == 0 && pacmandy == 1 && (ch & 8) != 0)) {
                pacmandx = 0;
                pacmandy = 0;
            }
        }
        pacmanx = pacmanx + pacmanspeed * pacmandx;
        pacmany = pacmany + pacmanspeed * pacmandy;
    }

    private void drawPacman(Graphics2D g2d) {

        if (viewdx == -1) {
            drawPacnanLeft(g2d);
        } else if (viewdx == 1) {
            drawPacmanRight(g2d);
        } else if (viewdy == -1) {
            drawPacmanUp(g2d);
        } else {
            drawPacmanDown(g2d);
        }
    }

    private void drawPacmanUp(Graphics2D g2d) {
        g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);    
    }

    private void drawPacmanDown(Graphics2D g2d) {
        g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);        
    }

    private void drawPacnanLeft(Graphics2D g2d) {

        g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
    }

    private void drawPacmanRight(Graphics2D g2d) {

        g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
    }

    private void drawMaze(Graphics2D g2d) {

        short i = 0;
        int x, y;

        for (y = 0; y < scrsize; y += blocksize) {
            for (x = 0; x < scrsize; x += blocksize) {

                g2d.setColor(mazecolor);
                g2d.setStroke(new BasicStroke(2));

                if ((screendata[i] & 1) != 0) { 
                    g2d.drawLine(x, y, x, y + blocksize - 1);
                }

                if ((screendata[i] & 2) != 0) { 
                    g2d.drawLine(x, y, x + blocksize - 1, y);
                }

                if ((screendata[i] & 4) != 0) { 
                    g2d.drawLine(x + blocksize - 1, y, x + blocksize - 1,
                            y + blocksize - 1);
                }

                if ((screendata[i] & 8) != 0) { 
                    g2d.drawLine(x, y + blocksize - 1, x + blocksize - 1,
                            y + blocksize - 1);
                }

                if ((screendata[i] & 16) != 0) { 
                    g2d.setColor(dotcolor);
                    g2d.fillRect(x + 11, y + 11, 2, 2);
                }

                i++;
            }
        }
    }

    private void initGame() {
        score = 0;
        scoreTwo = 0;
        initLevel();
        nrofghosts = 0;
        currentspeed = 1;
    }

    private void initLevel() {

        int i;
        for (i = 0; i < nrofblocks * nrofblocks; i++) {
            screendata[i] = leveldata[i];
        }

        continueLevel();
    }

    private void continueLevel() {

        short i;
        int dx = 1;
        int random;

        /*for (i = 0; i < nrofghosts; i++) {

            ghosty[i] = 4 * blocksize;
            ghostx[i] = 4 * blocksize;
            ghostdy[i] = 0;
            ghostdx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentspeed + 1));

            if (random > currentspeed) {
                random = currentspeed;
            }

            ghostspeed[i] = validspeeds[random];
        }*/

        pacmanx = 14 * blocksize;
        pacmany = 14 * blocksize;
        pacmandx = 0;
        pacmandy = 0;
        reqdx = 0;
        reqdy = 0;
        viewdx = -1;
        viewdy = 0;
        dying = false;
    }

    private void loadImages() {

        ghost = new ImageIcon("src/maze/people_yellow.png").getImage();
        pacman1 = new ImageIcon("src/maze/people.png").getImage();
        pacman2up = new ImageIcon("images/up1.png").getImage();
        pacman3up = new ImageIcon("images/up2.png").getImage();
        pacman4up = new ImageIcon("images/up3.png").getImage();
        pacman2down = new ImageIcon("images/down1.png").getImage();
        pacman3down = new ImageIcon("images/down2.png").getImage();
        pacman4down = new ImageIcon("images/down3.png").getImage();
        pacman2left = new ImageIcon("images/left1.png").getImage();
        pacman3left = new ImageIcon("images/left2.png").getImage();
        pacman4left = new ImageIcon("images/left3.png").getImage();
        pacman2right = new ImageIcon("images/right1.png").getImage();
        pacman3right = new ImageIcon("images/right2.png").getImage();
        pacman4right = new ImageIcon("images/right3.png").getImage();

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);
        drawScoreTwo(g2d);

        if (ingame) {
            playGame(g2d);
        } else {
            showIntroScreen(g2d);
        }

        g2d.drawImage(ii, 5, 5, this);
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }

    class TAdapter extends KeyAdapter  {
        
        @Override
        public void keyPressed(KeyEvent e) {
            
            int key = e.getKeyCode();
            System.out.println("This is key " + key);
            
            

            if (ingame) {
                if (key == KeyEvent.VK_LEFT) {
                    reqdx = -1;
                    reqdy = 0;
                    out.println("MOVE " + reqdx + "." + reqdy);
                    
                } else if (key == KeyEvent.VK_RIGHT) {
                    reqdx = 1;
                    reqdy = 0;
                    out.println("MOVE " + reqdx + "." + reqdy);
                } else if (key == KeyEvent.VK_UP) {
                    reqdx = 0;
                    reqdy = -1;
                    out.println("MOVE " + reqdx + "." + reqdy);
                } else if (key == KeyEvent.VK_DOWN) {
                    reqdx = 0;
                    reqdy = 1;
                    out.println("MOVE " + reqdx + "." + reqdy);
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    ingame = false;
                    out.println(ingame);
                } else if (key == KeyEvent.VK_PAUSE) {
                    if (timer.isRunning()) {
                        timer.stop();
                    } else {
                        timer.start();
                    }
                }
            } else {
                if (key == 's' || key == 'S') {
                    ingame = true;
                    out.println(ingame);
                    initGame();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == Event.LEFT || key == Event.RIGHT
                    || key == Event.UP || key == Event.DOWN) {
                reqdx = 0;
                reqdy = 0;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        repaint();
    }
}
}
