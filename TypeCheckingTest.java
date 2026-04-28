import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

public class TypeCheckingTest {
    public static void main(String[] args) {
        Reader reader = null;
        try {
            if (args.length == 1) {
                File input = new File(args[0]);
                reader = new FileReader(input);
            } else {
                reader = new InputStreamReader(System.in);
            }
            Lexer lexer = new Lexer(reader);
            parser parser = new parser(lexer);
            Program program = (Program) parser.parse().value;
            program.typeCheck();
            System.err.print("All good!");
        } catch (SemanticException semanticException) {
            System.err.print(semanticException);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}