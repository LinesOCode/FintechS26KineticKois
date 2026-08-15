import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

class SuperStoxUI {
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel cards;

    public SuperStoxUI() {
        frame = new JFrame("SuperStox");
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        cards.add(createHomePanel(), "home");
        cards.add(createPredictionsPanel(), "predictions");
        cards.add(createFakeMarketPanel(), "fake");

        frame.setContentPane(cards);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top area: title + dog image side by side
        JPanel top = new JPanel(new BorderLayout(10, 10));
        JLabel title = new JLabel("SuperStox");
        title.setFont(new Font("SansSerif", Font.BOLD, 48));
        title.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        // Example dog image URL; replace with local file if you prefer
        try {
            URL url = new URL("https://images.dog.ceo/breeds/husky/n02110185_1469.jpg");
            ImageIcon icon = new ImageIcon(url);
            Image scaled = icon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            imageLabel.setText("[Dog image]");
            imageLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        }

        top.add(title, BorderLayout.CENTER);
        top.add(imageLabel, BorderLayout.EAST);

        // Bottom area: two large buttons each taking half width
        JPanel bottom = new JPanel(new GridLayout(1, 2, 10, 0));
        JButton predictionsBtn = new JButton("Predictions");
        JButton learningBtn = new JButton("Learning");

        Font btnFont = new Font("SansSerif", Font.PLAIN, 24);
        predictionsBtn.setFont(btnFont);
        learningBtn.setFont(btnFont);

        // Make buttons visually large
        predictionsBtn.setPreferredSize(new Dimension(0, 100));
        learningBtn.setPreferredSize(new Dimension(0, 100));

        predictionsBtn.addActionListener(e -> cardLayout.show(cards, "predictions"));
        learningBtn.addActionListener(e -> cardLayout.show(cards, "fake")); // goes straight to fake stock market

        bottom.add(predictionsBtn);
        bottom.add(learningBtn);

        // Compose front page
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JPanel(), BorderLayout.CENTER); // spacer
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPredictionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("Predictions");
        header.setFont(new Font("SansSerif", Font.BOLD, 36));
        header.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(header, BorderLayout.NORTH);

        // center: three big buttons stacked vertically
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(10, 100, 10, 100));

        JButton newsBtn = new JButton("News");
        JButton reviewsBtn = new JButton("Reviews");
        JButton historyBtn = new JButton("Stock History");

        Font btnFont = new Font("SansSerif", Font.PLAIN, 20);
        Dimension btnSize = new Dimension(0, 60);

        for (JButton b : new JButton[]{newsBtn, reviewsBtn, historyBtn}) {
            b.setFont(btnFont);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnSize.height));
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            center.add(b);
            center.add(Box.createRigidArea(new Dimension(0, 12)));
        }

        // Button below all of those: Fake Stock Market
        JButton fakeBtn = new JButton("Fake Stock Market");
        fakeBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        fakeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        fakeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        fakeBtn.addActionListener(e -> cardLayout.show(cards, "fake"));

        center.add(Box.createRigidArea(new Dimension(0, 6)));
        center.add(fakeBtn);

        panel.add(center, BorderLayout.CENTER);

        // Optional back button to return to home
        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> cardLayout.show(cards, "home"));
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(backBtn);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFakeMarketPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("Fake Stock Market");
        header.setFont(new Font("SansSerif", Font.BOLD, 34));
        header.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(header, BorderLayout.NORTH);

        // A simple placeholder body
        JTextArea info = new JTextArea(
            "This is the Fake Stock Market screen.\n\n" +
            "You can implement the simulated trading UI here."
        );
        info.setEditable(false);
        info.setFont(new Font("Monospaced", Font.PLAIN, 14));
        info.setBackground(panel.getBackground());
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(info, BorderLayout.CENTER);

        JButton backHome = new JButton("Back to Home");
        backHome.addActionListener(e -> cardLayout.show(cards, "home"));
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(backHome);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    public static void main(String[] args) {
        // Ensure GUI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(SuperStoxUI::new);
    }
}
