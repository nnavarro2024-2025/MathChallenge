package com.mycompany.mathchallenge;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;

public class MathChallenge {

    // questions 
    static HashMap<Integer, String> questionMap = new HashMap<>(); 
    // valid the user responses
    static HashMap<Integer, Integer> answerMap = new HashMap<>(); 
    // store response times
    static List<AnsweredProblem> answeredProblems = new ArrayList<>();
    // manage question to be answered
    static Queue<String> questionQueue = new LinkedList<>(); 
    // store all questions 
    static LinkedList<String> questionHistory = new LinkedList<>(); 
    // track level 
    static Graph levelGraph = new Graph();

    static int score = 0; 
    static int lives = 3; 
    static int questionID = 0; 
    static final int TIME_LIMIT = 20;

    // swing
    private static JFrame frame;
    private static JLabel questionLabel;
    private static JLabel levelLabel;
    private static JLabel scoreLabel;
    private static JLabel livesLabel;
    private static JLabel timerLabel;
    private static JButton[] optionButtons;
    private static JTextArea historyTextArea;
    private static Timer timer;
    private static int timeLeft;

    //current question was shown
    private static long questionStartTime;

    public static void main(String[] args) {
        // swing UI
        SwingUtilities.invokeLater(() -> createAndShowGUI());
        
        // graph
        initializeGraph();
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Math Challenge Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // game info
        JPanel infoPanel = new JPanel(new GridLayout(1, 4));
        levelLabel = new JLabel("Level: 1", SwingConstants.CENTER);
        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        livesLabel = new JLabel("Lives: 3", SwingConstants.CENTER);
        timerLabel = new JLabel("Time: 20", SwingConstants.CENTER);
        
        infoPanel.add(levelLabel);
        infoPanel.add(scoreLabel);
        infoPanel.add(livesLabel);
        infoPanel.add(timerLabel);
        frame.add(infoPanel, BorderLayout.NORTH);

        // question and options
        JPanel centerPanel = new JPanel(new BorderLayout());
        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 20));
        centerPanel.add(questionLabel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(2, 2));
        optionButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JButton();
            optionButtons[i].setFont(new Font("Arial", Font.PLAIN, 16));
            final int index = i;
            optionButtons[i].addActionListener(e -> checkAnswer(index));
            optionsPanel.add(optionButtons[i]);
        }
        centerPanel.add(optionsPanel, BorderLayout.CENTER);
        frame.add(centerPanel, BorderLayout.CENTER);

        // panel for history
        historyTextArea = new JTextArea();
        historyTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(historyTextArea);
        scrollPane.setPreferredSize(new Dimension(frame.getWidth(), 150));
        frame.add(scrollPane, BorderLayout.SOUTH);

        frame.setVisible(true);

        // start game
        nextQuestion();
    }

    private static void nextQuestion() {
        questionID++;
        int level = (score / 5) + 1;
        String question = generateQuestion(level);
        Integer correctAnswer = answerMap.get(questionID);
        if (correctAnswer == null) {
            correctAnswer = 0;
        }

      
        levelLabel.setText("Level: " + level);
        scoreLabel.setText("Score: " + score);
        livesLabel.setText("Lives: " + lives);
        
        // multiple choice options
        List<String> options = generateOptions(correctAnswer);
        questionLabel.setText("Q" + questionID + ") " + question);
        
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(options.get(i));
            optionButtons[i].setEnabled(true);
        }

        // queue and history
        questionQueue.add(question);
        questionHistory.add(question);
        updateHistoryDisplay();

        // question start time
        questionStartTime = System.currentTimeMillis();

  
        startTimer();
    }

    private static List<String> generateOptions(int correctAnswer) {
        Random rand = new Random();
        List<String> options = new ArrayList<>();
        
        // position for correct answer (0-3)
        int correctPos = rand.nextInt(4);
        
        // 3 unique wrong answers
        Set<Integer> wrongAnswers = new HashSet<>();
        while (wrongAnswers.size() < 3) {
            int wrongAnswer = correctAnswer + rand.nextInt(10) - 5;
            if (wrongAnswer != correctAnswer && !wrongAnswers.contains(wrongAnswer) && wrongAnswer >= 0) {
                wrongAnswers.add(wrongAnswer);
            }
        }
        
        // options in order a, b, c, d
        char optionLetter = 'a';
        int wrongIndex = 0;
        for (int i = 0; i < 4; i++) {
            if (i == correctPos) {
                options.add(optionLetter + ") " + correctAnswer);
            } else {
                options.add(optionLetter + ") " + wrongAnswers.toArray()[wrongIndex++]);
            }
            optionLetter++;
        }
        
        return options;
    }

    private static void checkAnswer(int selectedIndex) {
        if (timer != null) {
            timer.stop();
        }

        long answerEndTime = System.currentTimeMillis();
        double timeTakenSeconds = (answerEndTime - questionStartTime) / 1000.0;

        String selectedText = optionButtons[selectedIndex].getText();
        Integer correctAnswer = answerMap.get(questionID);

        // ambot check
        if (correctAnswer == null) {
            correctAnswer = 0;
        }

        //  buttons para sa  feedback
        for (JButton button : optionButtons) {
            button.setEnabled(false);
        }

        boolean isCorrect = selectedText.endsWith(String.valueOf(correctAnswer));
        if (isCorrect) {
            JOptionPane.showMessageDialog(frame, "Correct!", "Result", JOptionPane.INFORMATION_MESSAGE);
            score++;
            // update question correctly answered
            int index = questionHistory.size() - 1;
            String prevQuestion = questionHistory.get(index);
            if (prevQuestion.endsWith("?")) {
                String updated = prevQuestion.substring(0, prevQuestion.length() - 1) + correctAnswer;
                questionHistory.set(index, updated);
            }
        } else {
            // correct answer text
            String correctAnswerText = "";
            for (JButton button : optionButtons) {
                if (button.getText().endsWith(String.valueOf(correctAnswer))) {
                    correctAnswerText = button.getText();
                    break;
                }
            }
            
            JOptionPane.showMessageDialog(frame, 
                "Wrong! Correct answer was \n" + correctAnswerText, 
                "Result", JOptionPane.ERROR_MESSAGE);
            lives--;
        }

      
        answeredProblems.add(new AnsweredProblem(questionID, questionMap.get(questionID), isCorrect, (int)Math.round(timeTakenSeconds)));

        questionQueue.poll();
        updateHistoryDisplay();

        if (lives <= 0) {
            gameOver();
        } else {
          
            Timer delayTimer = new Timer(1500, e -> {
                nextQuestion();
            });
            delayTimer.setRepeats(false);
            delayTimer.start();
        }
    }

    private static void startTimer() {
        timeLeft = TIME_LIMIT;
        timerLabel.setText("Time: " + timeLeft);
        
        if (timer != null) {
            timer.stop();
        }
        
        timer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("Time: " + timeLeft);
            
            if (timeLeft <= 0) {
                timer.stop();
                timeUp();
            }
        });
        timer.start();
    }

    private static void timeUp() {
        lives--;
        // irecord ang max time limit
        answeredProblems.add(new AnsweredProblem(questionID, questionMap.get(questionID), false, TIME_LIMIT));

        questionQueue.poll();
        updateHistoryDisplay();
        
        if (lives <= 0) {
            gameOver();
        } else {
            JOptionPane.showMessageDialog(frame, "Time's up!", "Result", JOptionPane.WARNING_MESSAGE);
            nextQuestion();
        }
    }

    private static void gameOver() {
        if (timer != null) {
            timer.stop();
        }

        // iend all game pag game over
        for (JButton button : optionButtons) {
            button.setEnabled(false);
        }
        
        // timeTakenSeconds
        List<AnsweredProblem> sortedByFastest = new ArrayList<>();
        // correct answers for fastest answered
        for (AnsweredProblem ap : answeredProblems) {
            if (ap.isCorrect) {
                sortedByFastest.add(ap);
            }
        }
        sortedByFastest.sort(Comparator.comparingInt(a -> a.timeTakenSeconds));

        //  message with score, fastest answered problems, and history
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Game Over!\n");
        messageBuilder.append("Your Score: ").append(score).append("\n\n");
        messageBuilder.append("Fastest Answered Problems (Top 3):\n");
        int count = 0;
        for (AnsweredProblem ap : sortedByFastest) {
            if (count >= 3) break;
            // integer seconds
            messageBuilder.append("Q").append(ap.questionID).append(": ").append(ap.question)
                          .append(" - Time: ").append(ap.timeTakenSeconds).append(" sec")
                          .append(" (Correct)").append("\n");
            count++;
        }
        if (count == 0) {
            messageBuilder.append("No correct answers.\n");
        }
        messageBuilder.append("\nHistory of all questions asked:\n");
        for (String h : questionHistory) {
            messageBuilder.append(h).append("\n");
        }
        
        JTextArea textArea = new JTextArea(messageBuilder.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        // panel with scrollPane and start button
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton startButton = new JButton("Start New Game");
        panel.add(startButton, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(frame, "Game Over Summary", true);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);

        // button to close dialog unya restart game
        startButton.addActionListener(e -> {
            dialog.dispose();
            restartGame();
        });

        dialog.setVisible(true);
    }

    private static void restartGame() {
        // reset game
        score = 0;
        lives = 3;
        questionID = 0;
        questionMap.clear();
        answerMap.clear();
        questionQueue.clear();
        questionHistory.clear();
        answeredProblems.clear();

        // UI labels
        levelLabel.setText("Level: 1");
        scoreLabel.setText("Score: 0");
        livesLabel.setText("Lives: 3");
        timerLabel.setText("Time: " + TIME_LIMIT);
        historyTextArea.setText("");

        // re initialize graph
        levelGraph = new Graph();
        initializeGraph();

        // enable buttons
        for (JButton button : optionButtons) {
            button.setEnabled(true);
        }

 
        nextQuestion();
    }

    private static void updateHistoryDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("Answered Problems:\n");

        int start = Math.max(0, answeredProblems.size() - 3);
        for (int i = start; i < answeredProblems.size(); i++) {
            AnsweredProblem ap = answeredProblems.get(i);
            sb.append("Q").append(ap.questionID).append(": ").append(ap.question)
              .append(" - Time: ").append(ap.timeTakenSeconds).append(" sec")
              .append(ap.isCorrect ? " (Correct)" : " (Wrong)").append("\n");
        }
        sb.append("\nQuestion History:\n");
        for (String h : questionHistory) {
            sb.append(h).append("\n");
        }
        historyTextArea.setText(sb.toString());
    }

    static String generateQuestion(int level) {
        Random rand = new Random();
        int a = rand.nextInt(20 * level) + 1; // first random number
        int b = rand.nextInt(20 * level) + 1; // second random number
        int c = rand.nextInt(3 * level) + 1; // random exponent
        int type = rand.nextInt(5); // select the type of question
        String question = "";
        int answer = 0;

        switch (type) {
            case 0:
                question = a + " + " + b + " = ?";
                answer = a + b;
                break;
            case 1:
                question = a + " - " + b + " = ?";
                answer = a - b;
                break;
            case 2:
                question = a + " × " + b + " = ?";
                answer = a * b;
                break;
            case 3:
                b = (b == 0) ? 1 : b;
                question = (a * b) + " ÷ " + b + " = ?";
                answer = a;
                break;
            case 4:
                question = "What is " + a + "^" + c + "?";
                answer = (int) Math.pow(a, c);
                break;
        }

        // generated question and answer 
        questionMap.put(questionID, question);
        answerMap.put(questionID, answer);
        return question;
    }

    private static void initializeGraph() {
        for (int i = 1; i <= 10; i++) {
            levelGraph.addNode(i); 
        }

        for (int i = 1; i < 10; i++) {
            levelGraph.addEdge(i, i + 1); 
        }
    }

    // graph class using adjacency list 
    static class Graph {
        private Map<Integer, List<Integer>> adjList; 

        public Graph() {
            adjList = new HashMap<>(); 
        }

        public void addNode(int node) {
            adjList.putIfAbsent(node, new ArrayList<>());
        }

        public void addEdge(int src, int dest) {
            if (!adjList.containsKey(src)) {
                addNode(src); 
            }
            if (!adjList.containsKey(dest)) {
                addNode(dest); 
            }
            adjList.get(src).add(dest); 
        }


    }

    // hold answered problem info with time taken
    static class AnsweredProblem {
        int questionID;
        String question;
        boolean isCorrect;
        int timeTakenSeconds; // int

        public AnsweredProblem(int questionID, String question, boolean isCorrect, double timeTakenSeconds) {
            this.questionID = questionID;
            this.question = question;
            this.isCorrect = isCorrect;
            this.timeTakenSeconds = (int) Math.round(timeTakenSeconds);
        }
    }
}
