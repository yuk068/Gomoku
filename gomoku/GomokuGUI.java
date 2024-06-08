package gomoku;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;

public class GomokuGUI extends JFrame {

    private GomokuGame game;
    private GomokuBot bot;
    private final GrayOverlayPanel overlayPanel;
    private final JPanel panel;

    private String title;
    private String botName;
    public static int boardSize;
    
    boolean botVsBot = false;
    boolean botStart = false;
    boolean botGame = true;
    boolean dynamicBot = false;
    boolean botIsThinking = false;
    boolean gameConcluded = false;

    boolean showIndex = false;
    boolean showEval = false;
    boolean showMoveLog = false;

    ArrayList<Integer> moveLog;
    public static Color GO_BOARD = new Color(255, 187, 150).darker();
    public static final Color DEV_TEXT = new Color(200, 39, 143);
    public static Border DEV_BORDER = null;

    public GomokuGUI(int size) {
        super("Gomoku - Press G for Guide");
        title = "Gomoku";
        boardSize = size;

        moveLog = new ArrayList<>();
        game = new GomokuGame(size, 5);
        promptUserSettings();
        panel = new JPanel();
        panel.setLayout(new GridLayout(game.height, game.size));

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        layeredPane.setPreferredSize(new Dimension(800, 800));

        panel.setBounds(0, 0, 800, 800);
        layeredPane.add(panel, JLayeredPane.DEFAULT_LAYER);

        overlayPanel = new GrayOverlayPanel();
        overlayPanel.setBounds(0, 0, 800, 800);
        layeredPane.add(overlayPanel, JLayeredPane.PALETTE_LAYER);
        overlayPanel.setVisible(false);

        initializePanel();
        addHotKeys();
        add(layeredPane);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setSize(816, 839);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
        update();
    }

    private void addHotKeys() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_N:
                        game = new GomokuGame(boardSize, 5);
                        moveLog.clear();
                        promptUserSettings();
                        gameConcluded = false;
                        botIsThinking = false;
                        update();
                        break;
//                    case KeyEvent.VK_E:
//                        showEval = !showEval;
//                        if (showEval) {
//                            GO_BOARD = Color.DARK_GRAY;
//                            DEV_BORDER = BorderFactory.createLineBorder(Color.WHITE, 1);
//                        } else {
//                            GO_BOARD = new Color(255, 187, 150).darker();
//                            DEV_BORDER = null;
//                            showIndex = false;
//                        }
//                        update();
//                        break;
//                    case KeyEvent.VK_I:
//                        showIndex = !showIndex;
//                        if (showIndex) {
//                            GO_BOARD = Color.DARK_GRAY;
//                            DEV_BORDER = BorderFactory.createLineBorder(Color.WHITE, 1);
//                        } else {
//                            GO_BOARD = new Color(255, 187, 150).darker();
//                            DEV_BORDER = null;
//                            showEval = false;
//                        }
//                        update();
//                        break;
                    case KeyEvent.VK_L:
                        if (!game.replayMode) {
                            showMoveLog = !showMoveLog;
                            update();
                        }
                        break;
                    case KeyEvent.VK_R:
                        if (game.enterReplayMode() && gameConcluded) {
                            JOptionPane.showMessageDialog(GomokuGUI.this, "Replay mode: J to wind back and K to wind forward", "Notice", JOptionPane.INFORMATION_MESSAGE);
                            showMoveLog = false;
                            update();
                        } else {
                            JOptionPane.showMessageDialog(GomokuGUI.this, "Game has not ended yet", "Notice", JOptionPane.INFORMATION_MESSAGE);
                        }
                        break;
                    case KeyEvent.VK_J:
                        if (game.replayMode) {
                            game.windBack();
                            update();
                        }
                        break;
                    case KeyEvent.VK_K:
                        if (game.replayMode) {
                            game.windForward();
                            update();
                        }
                        break;
                    case KeyEvent.VK_G:
                        showNote();
                        break;
                }
            }
        });
    }

    private void initializePanel() {
        panel.removeAll();
        panel.setLayout(new GridLayout(game.size, game.size));
        for (int i = 0; i < game.height; i++) {
            for (int j = 0; j < game.size; j++) {
                JPanel square = getSquare();
                panel.add(square);
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    private JPanel getSquare() {
        JPanel square = new JPanel();
        square.setBackground(GO_BOARD);
        square.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!game.isOver() && !gameConcluded) {
                    int index = panel.getComponentZOrder(square);
                    int x = index / game.size;
                    int y = index % game.size;
                    if (game.withinBoard(x, y) && !botIsThinking) {
                        boolean moveSuccess = game.makeMove(x, y);
                        if (moveSuccess) {
                            moveLog.add(GomokuUtility.coordinatesToIndex(x, y));
                            game.replayTape.add(GomokuUtility.copyBoard(game.getBoard()));
                            System.out.println("Eval by player: " + GomokuEvaluatorV1.evaluateGomokuGame(game) + "\n");
                            SFX.playSound("move.wav", 6);
                            GomokuEvaluatorV1.debugAllPattern(game);
                            System.out.println();
                            update();
                            if (botGame && !game.isOver()) {
                                botIsThinking = true;
                                overlayPanel.setVisible(true);
                                setTitle("Gomoku - Bot is thinking");
                                Timer timer = getBotTimer();
                                timer.start();
                            }
                        }
                    }
                }
            }

            private Timer getBotTimer() {
                Timer timer = new Timer(0, event -> {
                    bot.makeMove();
                    moveLog.add(bot.moveMadeIndex);
                    game.replayTape.add(GomokuUtility.copyBoard(game.getBoard()));
                    SFX.playSound("move.wav", 6);
                    GomokuEvaluatorV1.debugAllPattern(game);
                    System.out.println();
                    update();
                    if (game.isOver()) {
                        ((Timer) event.getSource()).stop();
                    }
                });
                timer.setRepeats(botVsBot);
                return timer;
            }
        });
        return square;
    }

    public void update() {
        for (int i = 0; i < game.height; i++) {
            for (int j = 0; j < game.size; j++) {
                JPanel square = (JPanel) panel.getComponent(i * game.size + j);
                Gomoku entity = game.getSquare(i, j);

                square.setBorder(DEV_BORDER);
                square.removeAll();
                square.setLayout(new BorderLayout());

                JLayeredPane layeredPane = new JLayeredPane();
                layeredPane.setLayout(null);

                DrawingPanel drawingPanel = new DrawingPanel(entity, i, j);
                drawingPanel.setBounds(0, 0, square.getWidth(), square.getHeight());
                layeredPane.add(drawingPanel, JLayeredPane.DEFAULT_LAYER);

                if (showIndex) {
                    JLabel indexLabel = new JLabel(String.valueOf(i * game.size + j));
                    indexLabel.setBounds(0, 0, square.getWidth(), square.getHeight());
                    indexLabel.setForeground(DEV_TEXT);
                    indexLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                    indexLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    indexLabel.setVerticalAlignment(SwingConstants.BOTTOM);
                    indexLabel.setOpaque(false);
                    layeredPane.add(indexLabel, JLayeredPane.PALETTE_LAYER);
                }

                if (showEval) {
                    GomokuGame dummyGame = game.makeMoveToNewGame(i, j);
                    dummyGame.turn = !dummyGame.turn;
                    JLabel evalLabel = new JLabel(String.valueOf(GomokuEvaluatorV1.evaluateGomokuGame(dummyGame)));
                    evalLabel.setBounds(0, 0, square.getWidth(), square.getHeight());
                    evalLabel.setForeground(DEV_TEXT.brighter());
                    evalLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                    evalLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    evalLabel.setVerticalAlignment(SwingConstants.TOP);
                    evalLabel.setOpaque(false);
                    layeredPane.add(evalLabel, JLayeredPane.PALETTE_LAYER);
                }

                if (showMoveLog) {
                    if (getMoveLogLabel(i, j, square) != null) {
                        JLabel indexLabel = getMoveLogLabel(i, j, square);
                        layeredPane.add(indexLabel, JLayeredPane.PALETTE_LAYER);
                    }
                }

                square.add(layeredPane, BorderLayout.CENTER);
                square.revalidate();
                square.repaint();
            }
        }
        if (botIsThinking) {
            botIsThinking = false;
            overlayPanel.setVisible(false);
            setTitle(title + botName);
        }
        if (game.isOver()) {
            if (!gameConcluded) {
                SFX.playSound("gameover.wav", 6);
                String msg = ", N for new game, G for guide, R to enter replay mode";
                String whoWon = getWinnerString();
                String gameTerminationState = "";
                switch (game.checkGameState()) {
                    case BLACK_WON:
                        gameTerminationState = whoWon + "Black won!";
                        setTitle(gameTerminationState + msg);
                        break;
                    case WHITE_WON:
                        gameTerminationState = whoWon + "White won!";
                        setTitle(gameTerminationState + msg);
                        break;
                    case DRAW:
                        gameTerminationState = whoWon + "Draw!";
                        setTitle(gameTerminationState + msg);
                        break;
                }
                printBotConclusion(gameTerminationState + "; Bot first: " + botStart);
                gameConcluded = true;
            }
        }
    }

    private void printBotConclusion(String gameTerminationState) {
        System.out.println(gameTerminationState);
        System.out.println("Bot: depth: " + (!dynamicBot ? bot.depth : "- ") + "; sight: " + (!dynamicBot ? bot.sight : "- ") + "; dynamic: " + dynamicBot);
        System.out.println("Total game moves: " + game.moveMade);
        System.out.println(String.format("Total bot thinking time: %.4fs", bot.totalThinkingTime / 1000.0));
        System.out.println(String.format("Avg. Thinking time: %.4fs", bot.totalThinkingTime / 1000.0 / game.moveMade));
        System.out.println(String.format("Shortest thinking time: %.4fs", bot.shortestThinkingTime / 1000.0));
        System.out.println(String.format("Longest thinking time: %.4fs", bot.longestThinkingTime / 1000.0));
        System.out.println(("Min reach: " + bot.minReach));
        System.out.println(("Max reach: " + bot.maxReach) + "\n");
    }

    private String getWinnerString() {
        String whoWon;
        if (game.moveMade % 2 == 0) {
            if (!botStart) {
                whoWon = botName + " - ";
            } else {
                whoWon = botName + " - You - ";
            }
        } else {
            if (botStart) {
                whoWon = botName + " - ";
            } else {
                whoWon = botName + " - You - ";
            }
        }
        if (game.checkGameState() == Gomoku.DRAW) whoWon = "";
        return whoWon;
    }

    private JLabel getMoveLogLabel(int i, int j, JPanel square) {
        JLabel indexLabel = null;
        int index = i * game.size + j;
        if (moveLog.contains(index)) {
            indexLabel = new JLabel(String.valueOf(moveLog.indexOf(index) + 1));
            indexLabel.setBounds(0, 0, square.getWidth(), square.getHeight());
            if (moveLog.indexOf(index) % 2 == 0) indexLabel.setForeground(Color.WHITE);
            else indexLabel.setForeground(Color.BLACK);
            indexLabel.setHorizontalAlignment(SwingConstants.CENTER);
            indexLabel.setVerticalAlignment(SwingConstants.CENTER);
            indexLabel.setOpaque(false);
        }
        return indexLabel;
    }

    private void promptUserSettings() {
        String[] bots = {"Bot - Bullet", "Bot - Blitz", "Bot - Standard", "Bot - Overthinking", "Bot - Dynamic"};
        String[] startOptions = {"Human Starts", "Bot Starts"};

        String selectedBot = null;
        while (selectedBot == null) {
            selectedBot = (String) JOptionPane.showInputDialog(
                    null,
                    "Select Bot",
                    "New game setup",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    bots,
                    bots[0]);

            if (selectedBot == null) {
                selectedBot = bots[0];
            }
        }

        switch (selectedBot) {
            case "Bot - Bullet":
                bot = new GomokuBot(game, false);
                bot.setDepth(0);
                bot.setSight(1);
                dynamicBot = false;
                botName = bots[0];
                break;
            case "Bot - Blitz":
                bot = new GomokuBot(game, false);
                bot.setDepth(1);
                bot.setSight(1);
                dynamicBot = false;
                botName = bots[1];
                break;
            case "Bot - Standard":
                bot = new GomokuBot(game, false);
                bot.setDepth(2);
                bot.setSight(1);
                botName = bots[2];
                dynamicBot = false;
                break;
            case "Bot - Overthinking":
                bot = new GomokuBot(game, false);
                bot.setDepth(3);
                bot.setSight(1);
                botName = bots[3];
                dynamicBot = false;
                break;
            case "Bot - Dynamic":
                bot = new GomokuBot(game, true);
                botName = bots[4];
                dynamicBot = true;
                break;
        }

        int selectedStartOption = JOptionPane.showOptionDialog(
                null,
                "Who starts first?",
                "New game setup",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                startOptions,
                startOptions[0]);

        if (selectedStartOption == JOptionPane.CLOSED_OPTION) {
            selectedStartOption = 0;
            title = "Gomoku - Playing as Black - ";
            setTitle(title + botName);
            bot.setColor(Gomoku.WHITE);
            botStart = false;
        }

        if (selectedStartOption == 1) {
            int centerMoveIndex;
            if (boardSize == 19) {
                centerMoveIndex = 180;
            } else {
                centerMoveIndex = 112;
            }

            game.makeMove(GomokuUtility.indexToCoordinates(centerMoveIndex)[0],
                    GomokuUtility.indexToCoordinates(centerMoveIndex)[1]);
            moveLog.add(centerMoveIndex);
            SFX.playSound("move.wav", 6);
            title = "Gomoku - Playing as White - ";
            setTitle(title + botName);
            bot.setColor(Gomoku.BLACK);
            botStart = true;
        } else {
            title = "Gomoku - Playing as Black - ";
            setTitle(title + botName);
            bot.setColor(Gomoku.WHITE);
            botStart = false;
        }
        System.out.println("New game: " + botName + "; Start: " + (selectedStartOption == 1 ? "Bot" : "Human") + "\n");
        showEval = false;
        showIndex = false;
        GO_BOARD = new Color(255, 187, 150).darker();
        DEV_BORDER = null;
    }

    static class DrawingPanel extends JPanel {
        private final Gomoku entity;
        private final int x;
        private final int y;

        public DrawingPanel(Gomoku entity, int x, int y) {
            this.x = x;
            this.y = y;
            this.entity = entity;
            setPreferredSize(new Dimension(50, 50));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int size = getWidth();
            int height = getHeight();

            g.setColor(GO_BOARD);
            g.fillRect(0, 0, size, height);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(Color.BLACK);
            int midX = size / 2;
            int midY = height / 2;

            if (x == 0 && y == 0) {
                g2d.drawLine(midX, midY, midX, height);
                g2d.drawLine(midX, midY, size, midY);
            } else if (x == 0 && y == GomokuGUI.boardSize - 1) {
                g2d.drawLine(0, midY, midX, midY);
                g2d.drawLine(midX, midX, midX, height);
            } else if (x == GomokuGUI.boardSize - 1 && y == 0) {
                g2d.drawLine(midX, midY, size, midY);
                g2d.drawLine(midX, 0, midX, midY);
            } else if (x == GomokuGUI.boardSize - 1 && y == GomokuGUI.boardSize - 1) {
                g2d.drawLine(midX, 0, midX, midY);
                g2d.drawLine(0, midY, midX, midY);
            } else if (x == 0) {
                g2d.drawLine(midX, midX, midX, height);
                g2d.drawLine(0, midY, size, midY);
            } else if (y == 0) {
                g2d.drawLine(midX, 0, midX, height);
                g2d.drawLine(midY, midY, size, midY);
            } else if (x == GomokuGUI.boardSize - 1) {
                g2d.drawLine(midX, 0, midX, midY);
                g2d.drawLine(0, midY, size, midY);
            } else if (y == GomokuGUI.boardSize - 1) {
                g2d.drawLine(midX, 0, midX, height);
                g2d.drawLine(0, midY, midY, midY);
            } else {
                g2d.drawLine(midX, 0, midX, height);
                g2d.drawLine(0, midY, size, midY);
            }

            if (GomokuGUI.boardSize == 19) {
                if ((x == 9 && y == 9) || (x == 3 && y == 3) || (x == 3 && y == 9) || (x == 3 && y == 15)
                        || (x == 9 && y == 3) || (x == 9 && y == 15) || (x == 15 && y == 3) || (x == 15 && y == 9) || (x == 15 && y == 15)) {
                    g2d.fillOval((size - 6) / 2, (height - 6) / 2, 6, 6);
                }
            } else {
                int indexNode = GomokuUtility.coordinatesToIndex(x, y);
                switch (indexNode) {
                    case 112:
                    case 168:
                    case 48:
                    case 56:
                    case 176:
                        g2d.fillOval((size - 6) / 2, (height - 6) / 2, 6, 6);
                    default:
                        break;
                }
            }

            int diameter = Math.min(size, height) - 10;
            int x = (size - diameter) / 2;
            int y = (height - diameter) / 2;

            if (entity == Gomoku.BLACK) {
                g.setColor(Color.DARK_GRAY);
                g2d.fillOval(x - 1, y - 1, diameter + 2, diameter + 2);
                g.setColor(Color.BLACK);
                g2d.fillOval(x, y, diameter, diameter);
            } else if (entity == Gomoku.WHITE) {
                g.setColor(Color.DARK_GRAY);
                g2d.fillOval(x - 1, y - 1, diameter + 2, diameter + 2);
                g.setColor(Color.WHITE);
                g2d.fillOval(x, y, diameter, diameter);
            }
        }
    }

    static class GrayOverlayPanel extends JPanel {
        public GrayOverlayPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(new Color(0, 0, 0, 90));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public void showNote() {
        JOptionPane.showMessageDialog(this,
                """
                        NOTE:
                        [ N ] for New Game
                        [ L ] for Move Log
                        
                        <- Say hi to her btw
                        
                        All bot's activity are recorded in the terminal
                        After a game has concluded, you can press [ R ]
                        to enter replay mode: [ J ] and [ K ] to review
                        the match.
                        \n""", "Notice", JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(getImageURL("hare.png")));
    }

    public static URL getImageURL(String fileName) {
        String image = Main.class.getPackageName().replace('.', '/') +
                "/asset/" + fileName;
        return Main.class.getClassLoader().getResource(image);
    }

}
