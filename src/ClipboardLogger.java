import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ClipboardLogger extends Frame implements ClipboardOwner {
    private TextArea textArea;  // TextArea to display copied texts
    private int copyCounter = 1;  // To track copied text entries
    private java.util.List<String> dataStorage = new ArrayList<>();  // Storage for text for file saving

    public ClipboardLogger() {
        super("Clipboard Logger");
        prepareGUI();
    }

    private void prepareGUI() {
        setSize(500, 300);
        setLayout(new BorderLayout());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveDataToFile();
                System.exit(0);
            }
        });

        textArea = new TextArea(10, 40);
        textArea.setEditable(false);  // Make TextArea read-only
        add(textArea, BorderLayout.CENTER);

        Button endButton = new Button("End Program");
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
        clipboard.setContents(contents, this);
    }

    private Transferable getClipboardContents(Clipboard clipboard) {
        final int MAX_ATTEMPTS = 10;
        int attempt = 0;
        while (attempt < MAX_ATTEMPTS) {
            try {
                return clipboard.getContents(null);
            } catch (IllegalStateException e) {
                if (attempt == MAX_ATTEMPTS - 1) {
                    System.err.println("Failed to access clipboard after multiple attempts: " + e.getMessage());
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                attempt++;
            }
        }
        return null;
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        try {
            Thread.sleep(200);
            Transferable newContents = getClipboardContents(clipboard);
            processContents(newContents);
            takeOwnership(clipboard, newContents);
        } catch (Exception e) {
            e.printStackTrace();
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
            System.err.println("Error processing clipboard contents: " + e.getMessage());
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ClipboardLogger();
    }
}
