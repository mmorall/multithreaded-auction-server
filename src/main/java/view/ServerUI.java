package view;

import server.SocketThread;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;

import static server.CristoServer.*;

public class ServerUI implements Runnable {
    private final JFrame frame;
    private JPanel mainPane;
    private JTextArea logTextArea;
    private JScrollPane logPane;
    private JPanel cardPane;
    private JPanel setupPane;
    private JPanel clientsPane;
    private JButton setupButton;
    private JTextField portTextField;
    private JTable clientsTable;

    public ServerUI(String appTitle) {
        frame = new JFrame(appTitle);
        initializeComponents();
    }

    private void initializeComponents() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(mainPane);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        //init buttons
        setupButton.addActionListener(e -> runServer(portTextField.getText()));
    }

    private void updateCardPane(JPanel panel) {
        cardPane.removeAll();
        cardPane.add(panel);
        cardPane.repaint();
        cardPane.revalidate();
    }

    public void updateClientsTable() {
        ArrayList<Object[]> data = new ArrayList<>();
        for (SocketThread socket : sockets) {
            data.add(new Object[] {socket.getName(), (socket.user != null), (socket.user != null) ? socket.user.getUsername() : "-"});
        }
        String[] columnTitles = new String[]{"Socket", "Logged In", "User"};
        clientsTable.setModel(new DefaultTableModel(data.toArray(new Object[0][]), columnTitles) {
            @Override public boolean isCellEditable(int row, int column) {return false;}
        });
    }

    public void goToClientsPane() {
        updateClientsTable();
        updateCardPane(clientsPane);
    }

    public void consolePrint(String line) {
        logTextArea.append("\n\n" + line);
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    }

    @Override
    public void run() {
        System.out.println("loading ServerUI...");
    }
}
