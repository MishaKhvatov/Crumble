package crumble;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * The {@code Crumble} class serves as the entry point for running the Crumble interpreter.
 * It can read source code from a file or from the standard input and then scan it into tokens.
 */
public class Crumble {
    static boolean hadError = false; // Tracks if an error occurred during execution

    /**
     * Main method to execute the interpreter. It reads from a file if an argument is provided,
     * or starts an interactive prompt otherwise.
     *
     * @param args command-line arguments; expects 0 or 1 argument for source file
     * @throws IOException if an I/O error occurs during file or prompt reading
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: crumble [source]");
            System.exit(64); // Exit with an error code for incorrect usage
        }
        if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }

        if (hadError) System.exit(65); // Exit with an error code if an error occurred
    }

    /**
     * Logs an error message with line information and sets the {@code hadError} flag.
     *
     * @param line    the line number where the error occurred
     * @param message the error message
     */
    public static void error(int line, String message) {
        System.err.println("Error at line " + line + ": " + message);
        hadError = true;
    }

    /**
     * Starts an interactive prompt where users can enter Crumble code line by line.
     * Scans and processes each line immediately.
     *
     * @throws IOException if an I/O error occurs during reading
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break; // Exit on EOF (Ctrl+D)

            hadError = false; // Reset error flag for each new line
            run(line);
        }
    }

    /**
     * Reads the entire content of a file and runs it as Crumble source code.
     *
     * @param fileName the path of the source file to run
     * @throws IOException if an I/O error occurs while reading the file
     */
    private static void runFile(String fileName) throws IOException {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(fileName));
            run(new String(bytes, Charset.defaultCharset()));
        } catch (IOException e) {
            System.err.println("Error reading file: " + fileName);
            System.exit(66); // Exit with an error code for file read failure
        }
    }

    /**
     * Runs the provided Crumble source code by scanning it into tokens.
     * Prints each token to the standard output.
     *
     * @param source the source code to interpret
     */
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        if (!hadError) {
            for (Token token : tokens) {
                System.out.println(token);
            }
        }
    }
}
