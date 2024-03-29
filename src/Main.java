import java.io.*;
import java.math.BigDecimal;
import java.net.ConnectException;
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

class Node {
    String name;
    Node prior;
    String text;
    int depth;
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
    static int notClosedTags = 0;
    static String URL_ERROR = "URL connection error";
    static String MALFORMED_HTML = "malformed HTML";
    private static boolean CanBeIgnored(String line) {
        return line.startsWith("<!") ||
                line.endsWith("/>") ||
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

    static public void AddIfHasText(String line, Node currentNode) {
        int index = 0;
        if (IsOpenTag(line)) {
            String tag = ExtractValueFromLine(line, 1);
            index = (tag.length() - 1) + 2;
        }
        StringBuilder result = new StringBuilder();
        for (; index < line.length(); index++) {
            char currentChar = line.charAt(index);

            if (currentChar == '>') continue;
            if (currentChar == '<') break;

            result.append(currentChar);
        }

        if (!result.toString().trim().isEmpty()) {
            boolean IsFirst = currentNode.text == null;
            if (IsFirst) currentNode.text = "";
            currentNode.text = currentNode.text.concat(IsFirst ? "" : "\n").concat(result.toString());
        }
    }

    static public int Test(Node current) {
        return Test(current, 0);
    }

    static public int Test(Node current, int count) {
        count += 1;

        Node prior = current.getPrior();
        if (prior == null) {
            return count;
        }

        return Test(prior, count);
    }


    static public void main(String[] args) throws IOException {
//        if (args.length == 0) {
//            System.out.println("Please inform the URL");
//            return;
//        }

        URL url = null;
        try {
            url = new URL("http://127.0.0.1:5500/index.html");
            url.toURI();
        } catch (Exception e) {
            System.out.println(URL_ERROR);
            return;
        }

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        Node biggestNode = new Node("result");
        Node current = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (CanBeIgnored(line)) {
                    continue;
                }

                if (IsOpenTag(line)) {
                    notClosedTags += 1;
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

                AddIfHasText(line, current);

                if (IsCloseTag(line)) {
                    notClosedTags -= 1;
                    String tag = ExtractValueFromLine(line, line.indexOf("</") + 2);

                    assert current != null;
                    if (current.getName().equals(tag)) {

                        current.depth = Test(current);
                        if (biggestNode == null) {
                            biggestNode = current;
                        } else if (biggestNode.depth < current.depth) {
                            String text = current.text;
                            if (current.text != null && !current.text.isEmpty()) {
                                biggestNode.text = current.text;
                                biggestNode.depth = current.depth;
                            }
                        }

                        if (current.getPrior() == null) break;
                        current = current.getPrior();
                    }
                }
            }

            if (notClosedTags != 0) {
                System.out.println(MALFORMED_HTML);
                return;
            }

            System.out.println(biggestNode.text);
        } catch (FileNotFoundException e) {
            System.out.println(MALFORMED_HTML);
        } catch (ConnectException e) {
            System.out.println(URL_ERROR);
        }
    }
}
