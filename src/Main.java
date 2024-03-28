import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author Gabriel Andreello
 * Java version: Amazon Correto 17.0.10
 *
 * andreello.dev.br
 */

public class Main {
    static String URL_NOT_FOUND = "URL connection error";

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

        URL url = null;
        try {
            url = new URL("http://hiring.axreng.com/internship/example1.html");
            url.toURI();
        } catch (Exception e) {
            System.out.println(URL_NOT_FOUND);
            return;
        }

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        Node current = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
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
        } catch (FileNotFoundException e) {
            System.out.println(URL_NOT_FOUND);
        }
    }
}

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
