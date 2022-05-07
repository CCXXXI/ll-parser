import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Java_LLParserAnalysis {
  public static void main(String[] args) throws IOException, LLParser.SyntaxError {
    final LLParser llParser = new LLParser(new LineNumberReader(new InputStreamReader(System.in)));
    System.out.println(llParser.parse());
  }

  @SuppressWarnings({"SpellCheckingInspection", "ArraysAsListWithZeroOrOneArgument"})
  static class LLParser {
    // region static fields
    private static final Set<String> nullableSymbols = new HashSet<>();
    private static final Map<String, Map<String, List<String>>> transitions = new HashMap<>();

    // The stupid platform only supports Java 8,
    // so we have to use this stupid way to init the set and the map.
    static {
      nullableSymbols.add("stmts");
      nullableSymbols.add("arithexprprime");
      nullableSymbols.add("multexprprime");
    }

    static {
      Map<String, List<String>> map = new HashMap<>();
      transitions.put("program", map);

      map.put("{", Arrays.asList("compoundstmt"));
    }

    static {
      Map<String, List<String>> map = new HashMap<>();
      transitions.put("stmt", map);

      map.put("{", Arrays.asList("compoundstmt"));
      map.put("if", Arrays.asList("ifstmt"));
      map.put("while", Arrays.asList("whilestmt"));
      map.put("ID", Arrays.asList("assgstmt"));
    }

    static {
      Map<String, List<String>> map = new HashMap<>();
      transitions.put("compoundstmt", map);

      map.put("{", Arrays.asList("{", "stmts", "}"));
    }

    static {
      Map<String, List<String>> map = new HashMap<>();
      transitions.put("stmts", map);

      map.put("{", Arrays.asList("stmt", "stmts"));
      map.put("}", Arrays.asList(""));
      map.put("if", Arrays.asList("stmt", "stmts"));
      map.put("while", Arrays.asList("stmt", "stmts"));
      map.put("ID", Arrays.asList("stmt", "stmts"));
    }

    static {
      Map<String, List<String>> map = new HashMap<>();
      transitions.put("ifstmt", map);

      map.put("if", Arrays.asList("if", "(", "boolexpr", ")", "then", "stmt", "else", "stmt"));
    }

    static {
      Map<String, List<String>> map = new HashMap<>();
      transitions.put("whilestmt", map);

      map.put("while", Arrays.asList("while", "(", "boolexpr", ")", "stmt"));
    }

    static {
      Map<String, List<String>> map = new HashMap<>();
      transitions.put("assgstmt", map);

      map.put("ID", Arrays.asList("ID", "=", "arithexpr", ";"));
    }

    static {
      Map<String, List<String>> map = new HashMap<>();
      transitions.put("boolexpr", map);

      map.put("(", Arrays.asList("arithexpr", "boolop", "arithexpr"));
      map.put("ID", Arrays.asList("arithexpr", "boolop", "arithexpr"));
      map.put("NUM", Arrays.asList("arithexpr", "boolop", "arithexpr"));
    }

    static {
      Map<String, List<String>> map = new HashMap<>();
      transitions.put("boolop", map);

      map.put("<", Arrays.asList("<"));
      map.put(">", Arrays.asList(">"));
      map.put("<=", Arrays.asList("<="));
      map.put(">=", Arrays.asList(">="));
      map.put("==", Arrays.asList("=="));
    }

    static {
      Map<String, List<String>> map = new HashMap<>();
      transitions.put("arithexpr", map);

      map.put("(", Arrays.asList("multexpr", "arithexprprime"));
      map.put("ID", Arrays.asList("multexpr", "arithexprprime"));
      map.put("NUM", Arrays.asList("multexpr", "arithexprprime"));
    }

    static {
      Map<String, List<String>> map = new HashMap<>();
      transitions.put("arithexprprime", map);

      map.put(")", Arrays.asList(""));
      map.put(";", Arrays.asList(""));
      map.put("<", Arrays.asList(""));
      map.put(">", Arrays.asList(""));
      map.put("<=", Arrays.asList(""));
      map.put(">=", Arrays.asList(""));
      map.put("==", Arrays.asList(""));
      map.put("+", Arrays.asList("+", "multexpr", "arithexprprime"));
      map.put("-", Arrays.asList("-", "multexpr", "arithexprprime"));
    }

    static {
      Map<String, List<String>> map = new HashMap<>();
      transitions.put("multexpr", map);

      map.put("(", Arrays.asList("simpleexpr", "multexprprime"));
      map.put("ID", Arrays.asList("simpleexpr", "multexprprime"));
      map.put("NUM", Arrays.asList("simpleexpr", "multexprprime"));
    }

    static {
      Map<String, List<String>> map = new HashMap<>();
      transitions.put("multexprprime", map);

      map.put(")", Arrays.asList(""));
      map.put(";", Arrays.asList(""));
      map.put("<", Arrays.asList(""));
      map.put(">", Arrays.asList(""));
      map.put("<=", Arrays.asList(""));
      map.put(">=", Arrays.asList(""));
      map.put("==", Arrays.asList(""));
      map.put("+", Arrays.asList(""));
      map.put("-", Arrays.asList(""));
      map.put("*", Arrays.asList("*", "simpleexpr", "multexprprime"));
      map.put("/", Arrays.asList("/", "simpleexpr", "multexprprime"));
    }

    static {
      Map<String, List<String>> map = new HashMap<>();
      transitions.put("simpleexpr", map);

      map.put("(", Arrays.asList("(", "arithexpr", ")"));
      map.put("ID", Arrays.asList("ID"));
      map.put("NUM", Arrays.asList("NUM"));
    }
    // endregion

    private final LineNumberReader reader;
    private Scanner scanner;
    private String currentSymbol;

    public LLParser(LineNumberReader reader) {
      this.reader = reader;
    }

    public SymbolNode parse() throws IOException, SyntaxError {
      nextSymbol();
      final SymbolNode result = new SymbolNode("program");
      result.build();
      return result;
    }

    /**
     * Reads the next symbol from the input.
     *
     * @see <a href="https://stackoverflow.com/a/1332316/13805358">Stack Overflow</a>
     */
    private void nextSymbol() throws IOException {
      if (scanner != null && scanner.hasNext()) {
        currentSymbol = scanner.next();
      } else {
        String line = reader.readLine();
        if (line != null) {
          scanner = new Scanner(line);
          nextSymbol();
        } else {
          currentSymbol = null;
        }
      }
    }

    static class SyntaxError extends Exception {
      public SyntaxError(String message) {
        super(message);
      }
    }

    public class SymbolNode {
      private final String symbol;
      private final List<SymbolNode> children;

      public SymbolNode(String symbol) {
        this.symbol = symbol;
        this.children = new ArrayList<>();
      }

      /** Add a symbol as child and build it. */
      private void addChild(String symbol) throws SyntaxError, IOException {
        if (symbol.isEmpty()) {
          children.add(new SymbolNode("E"));
        } else {
          SymbolNode child = new SymbolNode(symbol);
          children.add(child);
          child.build();
        }
      }

      public void build() throws SyntaxError, IOException {
        if (currentSymbol == null) {
          throw new SyntaxError("Unexpected end of file.");
        }
        if (isTerminal()) {
          if (symbol.equals(currentSymbol)) {
            nextSymbol();
          } else {
            throw new SyntaxError("Expected " + symbol + " but got " + currentSymbol);
          }
        } else { // non-terminal
          final List<String> children = transitions.get(symbol).get(currentSymbol);
          if (children == null) {
            throw new SyntaxError("Unexpected symbol " + currentSymbol);
          }
          for (String child : children) {
            addChild(child);
          }
        }
      }

      @Override
      public String toString() {
        return symbol
            + '\n'
            + children.stream()
                .map(c -> c.toString().replaceAll("(?m)^", "\t"))
                .collect(Collectors.joining());
      }

      private boolean isTerminal() {
        return !transitions.containsKey(symbol);
      }

      private boolean isNullable() {
        return nullableSymbols.contains(symbol);
      }
    }
  }
}
