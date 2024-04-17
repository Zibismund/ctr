import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ClipboardLogger extends Frame implements ClipboardOwner {
    private List textList;  // AWT List to display copied texts, now within a ScrollPane
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
            }
        });

        textList = new List();
        ScrollPane scrollPane = new ScrollPane();  // Creating a ScrollPane
        scrollPane.add(textList);                 // Adding the List to the ScrollPane
        add(scrollPane, BorderLayout.CENTER);     // Adding the ScrollPane to the Frame

        Button endButton = new Button("End Program");
        endButton.addActionListener(e -> saveDataToFile());
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

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        try {
            Thread.sleep(200); // Delay to allow clipboard to update
            Transferable newContents = clipboard.getContents(null);
            processContents(newContents);
            takeOwnership(clipboard, newContents);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processContents(Transferable contents) {
        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String text = (String) contents.getTransferData(DataFlavor.stringFlavor);
                String entry = copyCounter++ + ") " + text; // Changing the format to number followed by text
                textList.add(entry);
                dataStorage.add(entry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveDataToFile() {
        String baseDir = "C:\\Users\\Zbiga\\Downloads\\new\\";
        String fileName = "new";
        String fileExtension = ".txt";

        // Generate a unique file name
        File file;
        int fileNumber = 1;
        do {
            file = new File(baseDir + fileName + "(" + fileNumber++ + ")" + fileExtension);
        } while (file.exists());

        // Write data to the new unique file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String data : dataStorage) {
                writer.write(data + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        new ClipboardLogger();
    }
}
