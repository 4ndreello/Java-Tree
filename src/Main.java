import java.io.*;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Gabriel Andreello
 * Java version: Amazon Correto 17.0.10
 *
 * andreello.dev.br
 */

class Node {
    String name;
    Node prior;
    ArrayList<Node> children;

    public Node(String name) {
        this.name = name;
        this.children = new ArrayList<Node>();
    }

    public String getName() {
        return this.name;
    }

    public Node getPrior() {
        return this.prior;
    }


    public void setPrior(Node prior) {
        this.prior = prior;
    }
}

public class Main {
    private static boolean CanBeIgnored(String line) {
        return line.startsWith("<!") ||
                line.isEmpty();
    }

    public static boolean IsOpenTag(String line) {
        Pattern hasBract = Pattern.compile("<[a-zA-Z]");
        return hasBract.matcher(line).find() && !line.endsWith("/>");
    }

    public static boolean IsCloseTag(String line) {
        return line.contains("</");
    }

    public static String ExtractValueFromLine(String line, int index) {
        String value = "";
        for (; index < line.length(); index++) {
            char currentChar = line.charAt(index);
            if (currentChar == ' ' || currentChar == '>') break;
            value = value.concat(Character.toString(currentChar));
        }
        return value;
    }

    static public void main(String[] args) throws IOException {
//        if (args.length == 0) {
//            System.out.println("Please inform the URL");
//            return;
//        }

        String filePath = "C:\\Users\\eu\\OneDrive\\Área de Trabalho\\HTML TEST\\index.html";//args[0];
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("File \"" + filePath + "\" informed not exists");
            return;
        }

        Node current = null;
        FileReader fileReader = new FileReader(filePath);
        try (BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (CanBeIgnored(line)) {
                    continue;
                }

                if (IsOpenTag(line)) {
                    String tag = ExtractValueFromLine(line, 1);

                    if (current == null) {
                        current = new Node(tag);
                        continue;
                    }

                    Node newNode = new Node(tag);
                    newNode.setPrior(current);
                    current.children.add(newNode);
                    current = newNode;
                }

                if (IsCloseTag(line)) {
                    String tag = ExtractValueFromLine(line, line.indexOf("</") + 2);

                    assert current != null;
                    if (current.getName().equals(tag)) {
                        if (current.getPrior() == null) break;
                        current = current.getPrior();
                    }
                }
            }

            System.out.println(true);

//            if (!VerifyHTMLIntegrity()) {
//                System.out.println("malformed HTML");
//            }
        } catch (IOException e) {
            System.out.println("Something gone wrong while reading the file");
        }
    }
}