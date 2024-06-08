package gomoku;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {

    public static void main(String[] args) {
        System.out.println("Welcome to Gomoku! Bot log will be displayed here");
        System.out.println("Thanks for trying out my program!" + "\n");
        GomokuGUI gomokuGUI = new GomokuGUI(19);
    }

    private static void promptSize() {
        while (true) {
            int boardSize = promptForBoardSize();
            if (boardSize == -1) {
                break;
            }

            GomokuGUI gameGUI = new GomokuGUI(boardSize);
            gameGUI.setVisible(true);

            waitForWindowClose(gameGUI);
        }
    }

    private static int promptForBoardSize() {
        String[] options = {"19", "15", "Quit"};
        int choice = JOptionPane.showOptionDialog(null,
                "Choose the board size:",
                "Gomoku Board Size",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            return 19;
        } else if (choice == 1) {
            return 15;
        } else {
            return -1;
        }
    }

    private static void waitForWindowClose(JFrame frame) {
        final boolean[] windowClosed = {false};
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                synchronized (windowClosed) {
                    windowClosed[0] = true;
                    windowClosed.notify();
                }
            }
        });

        synchronized (windowClosed) {
            while (!windowClosed[0]) {
                try {
                    windowClosed.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}