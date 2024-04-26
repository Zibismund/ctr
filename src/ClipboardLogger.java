import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ClipboardLogger extends JFrame implements ClipboardOwner {
    private JTextArea textArea;  // JTextArea to display copied texts
    private int copyCounter = 1;  // To track copied text entries
    private java.util.List<String> dataStorage = new ArrayList<>();  // Storage for text for file saving

    public ClipboardLogger() {
        super("Clipboard Logger");
        prepareGUI();
    }

    private void prepareGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLayout(new BorderLayout());

        textArea = new JTextArea(10, 40);
        textArea.setEditable(false);  // Make JTextArea read-only
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        JButton endButton = new JButton("End Program");
        endButton.addActionListener(e -> {
            saveDataToFile();
            System.exit(0);
        });
        add(endButton, BorderLayout.SOUTH);

        setVisible(true);
        listenToClipboard();
    }

    private void listenToClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        takeOwnership(clipboard, contents);
    }

    private void takeOwnership(Clipboard clipboard, Transferable contents) {
        final int MAX_RETRIES = 5;
        int retries = 0;
        boolean success = false;
        while (!success && retries < MAX_RETRIES) {
            try {
                clipboard.setContents(contents, this);
                success = true;
            } catch (IllegalStateException ex) {
                retries++;
                handleException("Clipboard access error. Retry " + retries, ex);
            }
        }
        if (!success) {
            showContinueDialog("Unable to access the clipboard after several attempts.");
        }
    }

    private void handleException(String message, Exception ex) {
        JOptionPane.showMessageDialog(this, message + "\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showContinueDialog(String message) {
        int result = JOptionPane.showConfirmDialog(this, message + "\nDo you want to continue?", "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            listenToClipboard();  // Try to resume listening to clipboard
        } else {
            System.exit(1);  // Exit if the user chooses not to continue
        }
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        try {
            Thread.sleep(200);
            Transferable newContents = getClipboardContents(clipboard);
            processContents(newContents);
            takeOwnership(clipboard, newContents);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            handleException("Error while handling clipboard data", e);
        }
    }

    private Transferable getClipboardContents(Clipboard clipboard) {
        try {
            return clipboard.getContents(null);
        } catch (IllegalStateException e) {
            showContinueDialog("Failed to access clipboard: " + e.getMessage());
            return null;
        }
    }

    private void processContents(Transferable contents) {
        try {
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String text = (String) contents.getTransferData(DataFlavor.stringFlavor);
                String entry = copyCounter++ + ") " + text;
                textArea.append(entry + "\n");
                dataStorage.add(entry);
            } else {
                System.out.println("Clipboard contains unsupported data types.");
            }
        } catch (Exception e) {
            handleException("Error processing clipboard contents", e);
        }
    }

    private void saveDataToFile() {
        String baseDir = "C:\\Users\\Zbiga\\Downloads\\new\\";
        String fileName = "new";
        String fileExtension = ".txt";

        File file;
        int fileNumber = 1;
        do {
            file = new File(baseDir + fileName + "(" + fileNumber++ + ")" + fileExtension);
        } while (file.exists());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String data : dataStorage) {
                writer.write(data + "\n");
            }
        } catch (IOException e) {
            handleException("Failed to save data to file", e);
        }
    }

    public static void main(String[] args) {
        new ClipboardLogger();
    }
}
