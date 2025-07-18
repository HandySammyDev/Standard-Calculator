import Calculations.Basic_Calculations;
import Record.MixedData;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class TopPanel extends JPanel {
    private final int WIDTH = 500;
    private final int HEIGHT = 70;
    private int sizeableHEIGHT = HEIGHT;
    private final int TEXT_FIELD_WIDTH = WIDTH;
    private final int LABEL_WIDTH = 180;
    private int sizeableLabelWIDTH = LABEL_WIDTH;
    private JPanel panelBorderSouth = new JPanel();
    private JTextField activeField = null;
    private int textFieldIndex;
    private int activeCaret = 0;
    private LinkedList<MixedData> mixedDataLinkedList = new LinkedList<>();
    private String ans = null;

    public JTextField getActiveTextField(){
        return activeField;
    }
    public String getTextInTextField(){
        return activeField.getText();
    }
    public void setTextField(String command){
        activeField.setText(activeField.getText() + command);
    }
    public String getAns(){
        return ans;
    }

    public void createPanels(){
        if(activeCaret==0 && !mixedDataLinkedList.isEmpty()){
            return;
        }

        JPanel panel = new JPanel();
        JTextField textField = new JTextField();
        JLabel label = new JLabel();

        textField.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, HEIGHT));
        textField.setFont(new Font(null, Font.PLAIN, 25));
        textField.setBackground(Color.decode("#f0edec"));
        textField.setBorder(null);

        label.setPreferredSize(new Dimension(LABEL_WIDTH, HEIGHT));
        label.setFont(new Font(null, Font.PLAIN, 25));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setBackground(Color.decode("#f0edec"));

        panel.setBorder(new LineBorder(Color.decode("#1d2226")));
        panel.setLayout(new BorderLayout());
        panel.add(textField, BorderLayout.CENTER);
        panel.add(label, BorderLayout.EAST);

        if(mixedDataLinkedList.isEmpty()){
            panelBorderSouth.add(panel);
            mixedDataLinkedList.add(new MixedData(panel, textField, label));
            activeField = mixedDataLinkedList.getFirst().getTextField();
            highlightActiveTextField(activeField);
        }
        else{
            expandBorder();
            mixedDataLinkedList = modifiedPanelList(mixedDataLinkedList, activeField, panel, textField, label);
            activeField = mixedDataLinkedList.get(indexOfNextTextField +1).getTextField();
            clearHighlightActiveTextField();
            highlightActiveTextField(activeField);
        }

        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                activeField = textField;
                clearHighlightActiveTextField();
                highlightActiveTextField(activeField);
            }
        });

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==10){
                    createPanels();
                    repaint();
                    revalidate();
                }
                if(e.getKeyCode()==KeyEvent.VK_BACK_SPACE && activeCaret==0 && mixedDataLinkedList.size()>1){
                    removeTextField();
                }
            }
        });

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changeLabel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changeLabel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changeLabel();
            }
        });

        textField.addCaretListener(e -> activeCaret = e.getDot());

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                activeField = (JTextField) e.getSource();
            }
        });

        changeTextFields();
        activeCaret = 0;
    }

    public void changeTextFields(){
        for(int i = 0; i< mixedDataLinkedList.size(); i++){
            panelBorderSouth.add(mixedDataLinkedList.get(i).getPanel());
        }
        repaint();
        revalidate();
    }

    private int indexOfNextTextField;
    public LinkedList<MixedData> modifiedPanelList
            (LinkedList<MixedData> originalList,
             JTextField activeTextField,
             JPanel newPanel,
             JTextField newTextField,
             JLabel newLabel){

        LinkedList<MixedData> newList = new LinkedList<>();

        for(int i = 0; i<originalList.size(); i++){
            newList.add(originalList.get(i));

            if(activeTextField == mixedDataLinkedList.get(i).getTextField()){
                newList.add(new MixedData(newPanel, newTextField, newLabel));
                indexOfNextTextField = i;
            }
        }
        return newList;
    }
    public void removeTextField(){
        shrinkBorder();
        for(int i = 0; i< mixedDataLinkedList.size(); i++){
            if(activeField == mixedDataLinkedList.get(i).getTextField()){
                mixedDataLinkedList.remove(i);
                activeField = mixedDataLinkedList.get(i-1).getTextField();
                highlightActiveTextField(activeField);
            }
        }
        changeTextFields();
    }

    public void changeLabel(){
        for(int i=0; i<mixedDataLinkedList.size(); i++){
            if(activeField == mixedDataLinkedList.get(i).getTextField()){
                Basic_Calculations calculations = new Basic_Calculations(getTextInTextField());

                errorIndex = i;
                ans = calculations.getCalculations();

                clearError();

                if(ans.equals("Error")){
                    if(getTextInTextField().isEmpty()){
                        mixedDataLinkedList.get(i).getLabel().setText(null);
                        return;
                    }
                    errorImageDelay();
                }
                else{
                    mixedDataLinkedList.get(i).getLabel().setText(ans);
                }
            }
        }
        repaint();
        revalidate();
    }

    private Timer errorTimer;
    private int errorIndex;
    public void errorImageDelay() {
        if(errorTimer!=null){
            errorTimer.cancel(); //cancel previous timer if still running
        }

        errorTimer = new Timer();
        TimerTask task = new TimerTask() {
            int count = 1;
            @Override
            public void run() {
                count--;
                if(count<0){
                    errorTimer.cancel();
                    mixedDataLinkedList.get(errorIndex).getLabel().setIcon(errorImage());
                }
            }
        };

        errorTimer.scheduleAtFixedRate(task, 0, 750);
    }
    public void clearError(){
        if(errorTimer!=null){
            errorTimer.cancel();
        }
        mixedDataLinkedList.get(errorIndex).getLabel().setIcon(null);
        mixedDataLinkedList.get(errorIndex).getLabel().setText(null);
    }
    public ImageIcon errorImage(){
        ImageIcon originalIcon = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/images/error1.png")));
        Image scaledImage = originalIcon.getImage().getScaledInstance(50,50,Image.SCALE_AREA_AVERAGING);

        return new ImageIcon(scaledImage);
    }

    public void clearTextField(){
        getActiveTextField().setText("");
    }
    public void clearAllTextFields(){
        panelBorderSouth.removeAll();
        mixedDataLinkedList.clear();
        sizeableHEIGHT = HEIGHT;
        panelBorderSouth.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        repaint();
        revalidate();
    }
    public boolean textFieldIsEmpty(){
        //CE:
        //  if activeTextField is empty

        //Clear:
        //  if activeField is NOT empty
//        if(activeField.getText().isEmpty()){
//            return true;
//        }
//        else if(!activeField.getText().isEmpty()){
//            return false;
//        }
        return !activeField.getText().isEmpty() || !mixedDataLinkedList.getFirst().getTextField().getText().isEmpty();
        //return true;
    }

    private int mixedDataTemp;
    public void highlightActiveTextField(JTextField activeTextField){
        for(int i=0; i<mixedDataLinkedList.size(); i++){
            if(mixedDataLinkedList.get(i).getTextField()==activeTextField){
                mixedDataLinkedList.get(i).getPanel().setBorder(new LineBorder(Color.decode("#1e8d8f"), 4));
                mixedDataTemp = i;
            }
        }
    }
    public void clearHighlightActiveTextField(){
        try{
            mixedDataLinkedList.get(mixedDataTemp).getPanel().setBorder(new LineBorder(Color.decode("#1d2226")));
        }
        catch (IndexOutOfBoundsException e){
            System.out.println("Outta bounds");
        }
    }

    public void expandBorder(){
        sizeableHEIGHT += HEIGHT;
        panelBorderSouth.setPreferredSize(new Dimension(WIDTH, sizeableHEIGHT));
    }
    public void shrinkBorder(){
        panelBorderSouth.removeAll();
        repaint();
        revalidate();
        sizeableHEIGHT -= HEIGHT;
        panelBorderSouth.setPreferredSize(new Dimension(WIDTH, sizeableHEIGHT));
    }

    public TopPanel(){
        this.setBackground(Color.decode("#31383d"));
        this.setOpaque(true);
        this.setLayout(new BorderLayout());
        this.add(panelBorderSouth, BorderLayout.SOUTH);

        panelBorderSouth.setLayout(new GridLayout(0,1));
        panelBorderSouth.setPreferredSize(new Dimension(WIDTH,HEIGHT));

        createPanels();
    }
}
