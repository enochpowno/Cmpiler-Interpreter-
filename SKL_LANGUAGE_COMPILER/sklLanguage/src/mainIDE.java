import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;


import org.fife.ui.autocomplete.*;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.Stack;


public class mainIDE extends JFrame{

    private JTextPane outputArea;
    public static Stack<Scope> scopeList = new Stack<Scope>();
    private mainIDE main;
    RSyntaxTextArea reader;
    JPanel panel;
    JButton runBtn;
    RTextScrollPane inputScroll;
    boolean process = false;

    public mainIDE() {

        String message = "asd";

        panel = new JPanel(new BorderLayout());
        runBtn = new JButton("Run");
        reader = new RSyntaxTextArea(20, 60);

        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/sklLanguage", "TokenMaker");
        reader.setSyntaxEditingStyle("text/sklLanguage");
        reader.setCodeFoldingEnabled(true);
        reader.setAnimateBracketMatching(true);
        reader.setBracketMatchingEnabled(true);

        outputArea = new JTextPane();
        outputArea.setSize(new Dimension(600, 400));
        ScrollPane outputScroll = new ScrollPane();
        outputScroll.add(outputArea);

        inputScroll = new RTextScrollPane(reader);

        panel.add(runBtn, BorderLayout.NORTH);
        panel.add(inputScroll, BorderLayout.CENTER);
        panel.add(outputScroll, BorderLayout.SOUTH);


        setContentPane(panel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        System.out.println("cheeecking");

        // A CompletionProvider knows all possible completions and analyzes the contents of the text area to determine what completion choices should be presented.
        CompletionProvider provider = createCompletionProvider();

        // AutoCompletion is between a text component and a CompletionProvider. 
        // involves a popup trigger key, whether to display a documentation window along with completion choices
        AutoCompletion ac = new AutoCompletion(provider);
        ac.install(reader);

        main = this;

        reader.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
             //   printEventInfo("Key Typed", e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyCode() == KeyEvent.VK_SEMICOLON){
                    process = true;
                }
              //  printEventInfo("Key Pressed", e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if(process){
                    scopeList.clear();
                    outputArea.setText("");
                    CharStream cs = CharStreams.fromString(reader.getText()); 

                    sklLanguageLexer lexer = new  sklLanguageLexer(cs);
                    CommonTokenStream token = new CommonTokenStream(lexer);
                    token.fill();


                    sklLanguageParser parser = new  sklLanguageParser(token);

                    parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
                    lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

                    ParseTree tree = parser.compilationUnit();

                    Stack<Scope> scopes = new Stack<Scope>();
                    Visitor1 visitor = new Visitor1(token.getTokens(), lexer, scopes, scopeList, main);
                    visitor.visit(tree);
                    process = false;
                }
            //    printEventInfo("Key Released", e);
            }
        });

        runBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scopeList.clear();
                outputArea.setText("");
                CharStream cs = CharStreams.fromString(reader.getText());

                sklLanguageLexer lexer = new  sklLanguageLexer(cs);
                CommonTokenStream token = new CommonTokenStream(lexer);
                token.fill();


                sklLanguageParser parser = new  sklLanguageParser(token);

                parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
                lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

                ParseTree tree = parser.compilationUnit();
                
                Stack<Scope> scopes = new Stack<Scope>();
                Visitor2 visitor = new Visitor2(token.getTokens(), lexer, scopes, scopeList, main);
                visitor.visit(tree);
            }
        });
    }

    public void addColoredText(String text, Color color) {
        StyledDocument doc = outputArea.getStyledDocument();

        Style style = outputArea.addStyle("Color Style", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), text, style);
        }
        catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void printEventInfo(String str, KeyEvent e) {
        System.out.println(str);

        int code = e.getKeyCode();
        int mods = e.getModifiersEx();


    }

    public static void main(String[] args) {
        // Start all Swing applications on the EDT.
        SwingUtilities.invokeLater(() -> {
            new mainIDE().setVisible(true);
        });
    }

    public void popupMessage(String message){
        JOptionPane.showMessageDialog(null, message);
    }

    //IDE WITH FONT COLORING
    public void append(String msg){
        addColoredText(msg, Color.BLACK);
    }
    public void appendLine(String msg){

        addColoredText(msg + "\n", Color.BLACK);
    }
    public void appendError(String msg){
        addColoredText(msg + "\n", Color.RED);
    }

    public void appendSemanticError(String msg){
        addColoredText(msg + "\n", Color.BLUE);
    }

    public String askInput(String message){
        String reply = JOptionPane.showInputDialog(message);
        return reply;
    }

    //FOR IDE WITH AUTOCOMPLETE
    private CompletionProvider createCompletionProvider() {

        // A DefaultCompletionProvider simply checks the text entered up to the
        // caret position for a match against known completions.
        DefaultCompletionProvider provider = new DefaultCompletionProvider();

        // BasicCompletion is a straightforward word completion.

        provider.addCompletion(new BasicCompletion(provider, "stop"));
        provider.addCompletion(new BasicCompletion(provider, "if"));
        provider.addCompletion(new BasicCompletion(provider, "else"));
        provider.addCompletion(new BasicCompletion(provider, "while"));
        provider.addCompletion(new BasicCompletion(provider, "for"));
        provider.addCompletion(new BasicCompletion(provider, "output"));
        provider.addCompletion(new BasicCompletion(provider, "try"));
        provider.addCompletion(new BasicCompletion(provider, "catch"));
        provider.addCompletion(new BasicCompletion(provider, "throws"));
        provider.addCompletion(new BasicCompletion(provider, "constant"));
        provider.addCompletion(new BasicCompletion(provider, "lastly"));
        provider.addCompletion(new BasicCompletion(provider, "do"));
        provider.addCompletion(new BasicCompletion(provider, "create"));
        provider.addCompletion(new BasicCompletion(provider, "void"));
        provider.addCompletion(new BasicCompletion(provider, "true"));
        provider.addCompletion(new BasicCompletion(provider, "false"));
        provider.addCompletion(new BasicCompletion(provider, "char"));
        provider.addCompletion(new BasicCompletion(provider, "flag"));
        provider.addCompletion(new BasicCompletion(provider, "String"));
        provider.addCompletion(new BasicCompletion(provider, "int"));
        provider.addCompletion(new BasicCompletion(provider, "float"));
        provider.addCompletion(new BasicCompletion(provider, "printme"));
        provider.addCompletion(new BasicCompletion(provider, "input"));
        provider.addCompletion(new BasicCompletion(provider, "inputInt"));
        provider.addCompletion(new BasicCompletion(provider, "inputString"));
        provider.addCompletion(new BasicCompletion(provider, "inputChar"));
        provider.addCompletion(new BasicCompletion(provider, "inputFloat"));
        provider.addCompletion(new BasicCompletion(provider, "return"));
        provider.addCompletion(new BasicCompletion(provider, ""));
        provider.addCompletion(new BasicCompletion(provider, ""));

        // shorthands require the input text to be the same thing as the replacement text.
        provider.addCompletion(new ShorthandCompletion(provider, "out",
                "output ()", "output"));
        provider.addCompletion(new ShorthandCompletion(provider, "outln",
                "outputln ()", "outputLine"));
        provider.addCompletion(new ShorthandCompletion(provider, "if",
                "if ( ) {\n\n} else { \n\n }", "if(){ }"));
        provider.addCompletion(new ShorthandCompletion(provider, "wh",
                "while ( ) { }", "while loop"));
        provider.addCompletion(new ShorthandCompletion(provider, "fo",
                "for ( ; ; ) {\n\n}", "for loop"));
        provider.addCompletion(new ShorthandCompletion(provider, "pr",
                "printme( );", "printme( );"));
        provider.addCompletion(new ShorthandCompletion(provider, "tr",
                "try { \n\n } catch ( ) { \n }", "try { }"));
        provider.addCompletion(new ShorthandCompletion(provider, "ch",
                "char", "char"));
        provider.addCompletion(new ShorthandCompletion(provider, "in",
        		"input", "input"));
        provider.addCompletion(new ShorthandCompletion(provider, "inI",
        		"inputInt", "inputInt"));
        provider.addCompletion(new ShorthandCompletion(provider, "inS",
        		"inputString", "inputString"));
        provider.addCompletion(new ShorthandCompletion(provider, "inC",
        		"inputChar", "inputChar"));
        provider.addCompletion(new ShorthandCompletion(provider, "inF",
        		"inputFloat", "inputFloat"));
        
        return provider;
    }
}
