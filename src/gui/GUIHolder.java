package gui;


import loader.LoaderFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;

/**
 * Created by Андрей on 03.04.2015.
 */
public class GUIHolder implements ActionListener {
    private static final String FRAME_HEADER = "TEST";
    private static final String CHOOSE_ACTION = "Обзор...";
    private static final String PDF_MODE = "PDF";
    private static final String IMAGES_MODE = "Картиночки";
    private static final String DOWNLOAD = "Скачать";

    private Worker worker;
    private JFrame mainFrame;
    private JTextField urlField;
    private JTextField pathField;
    private JTextField loginField;
    private JPasswordField passwordField;
    private JPanel loginPanel;
    private JDialog progressDialog;
    private JProgressBar progressBar;
    public GUIHolder(Worker worker) {
        this.worker = worker;
    }

    /**
     * Creating and showing main frame.
     * Я верстаю как дебил.
     */
    public void createAndShowGui() {
        JFrame frame = new JFrame(FRAME_HEADER);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container pane = frame.getContentPane();

        JPanel urlHolder = new JPanel(new FlowLayout(FlowLayout.LEFT));
        urlField = new JTextField();
        urlField.setPreferredSize(new Dimension(500, 26));
        JLabel urlLabel = new JLabel("Адрес страницы: ");
        urlLabel.setPreferredSize(new Dimension(150, 26));
        urlHolder.add(urlLabel);
        urlHolder.add(urlField);

        JPanel pathToSave = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pathField = new JTextField();
        pathField.setPreferredSize(new Dimension(400, 26));
        pathField.setText(worker.getSavePath());

        JLabel pathLabel = new JLabel("Сохранить в:");
        pathLabel.setPreferredSize(new Dimension(150, 26));

        JButton fileChooseBtn = new JButton(CHOOSE_ACTION);
        fileChooseBtn.addActionListener(this);
        pathToSave.add(pathLabel);
        pathToSave.add(pathField);
        pathToSave.add(fileChooseBtn);

        JPanel wayToSave = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel wayLabel = new JLabel("Сохранить как:");
        wayLabel.setPreferredSize(new Dimension(150, 26));

        JRadioButton pdfRadio = new JRadioButton(PDF_MODE, null, true);
        pdfRadio.addActionListener(this);

        JRadioButton imagesRadio = new JRadioButton(IMAGES_MODE);
        imagesRadio.addActionListener(this);
        ButtonGroup waysGroup = new ButtonGroup();
        waysGroup.add(pdfRadio);
        waysGroup.add(imagesRadio);
        wayToSave.add(wayLabel);
        wayToSave.add(pdfRadio);
        wayToSave.add(imagesRadio);

        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton button = new JButton(DOWNLOAD);
        button.addActionListener(this);
        buttonPane.add(button);

        progressDialog = new JDialog(mainFrame, "Загрузка", true);
        progressDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                worker.interruptWork();
                hideProgressBar();
            }
        });
        progressDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        progressBar = new JProgressBar(0, 500);
        progressDialog.add(progressBar);
        progressDialog.setSize(300, 50);
        progressDialog.setLocationRelativeTo(mainFrame);

        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        frame.add(urlHolder);
        frame.add(pathToSave);
        frame.add(wayToSave);
        frame.add(buttonPane);
        frame.pack();
        frame.setVisible(true);

        mainFrame = frame;
    }

    /**
     * Select folder for save data
     * @return folder
     */
    public File getSavePathFromChooser() {

        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Папка для сохранения");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }

    public String getUrl () {
        return urlField.getText();
    }

    public String getSavedPath () {
        return pathField.getText();
    }

    /**
     * Show simple login dialog
     * @return
     */
    public String[] getLoginData() {
        if (loginPanel == null) {
            loginPanel = new JPanel();
            loginField = new JTextField(5);
            JLabel loginLabel = new JLabel("Логин:");
            passwordField = new JPasswordField(5);
            JLabel passwordLabel = new JLabel("Пароль:");
            loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
            loginPanel.add(loginLabel);
            loginPanel.add(loginField);
            loginPanel.add(passwordLabel);
            loginPanel.add(passwordField);
        }

        int result = JOptionPane.showConfirmDialog(null, loginPanel,
                "Логин", JOptionPane.OK_CANCEL_OPTION);

        String login = loginField.getText();
        String password = passwordField.getText();

        if (result == JOptionPane.OK_OPTION) {
            return new String[]{login, password};
        }
        return new String[]{};

    }

    protected void showProgressBar() {
        progressDialog.setVisible(true);
        progressDialog.setModal(true);
    }

    protected void hideProgressBar() {
        progressDialog.dispose();
    }

    protected void showDialog(int type, String title, String message) {
        JOptionPane.showMessageDialog(mainFrame,
               message,
               title,
               type);
    }

    protected void postProgress (boolean indeterminate, int current, int total) {
        progressBar.setIndeterminate(indeterminate);
        progressBar.setValue(current);
        if (!indeterminate) {
            progressBar.setMaximum(total);
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String command = actionEvent.getActionCommand();
        switch (command) {
            case PDF_MODE :
                worker.setSaveMode(LoaderFactory.PDF_LOADER);
                break;
            case IMAGES_MODE :
                worker.setSaveMode(LoaderFactory.IMAGE_LOADER);
                break;
            case CHOOSE_ACTION :
                File directory = getSavePathFromChooser();
                String path = directory.getAbsolutePath();
                pathField.setText(path);
                break;
            case DOWNLOAD :
                worker.startDownloading();
                break;
        }
    }
}
