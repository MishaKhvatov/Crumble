package tool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

/**
 * Utility to generate Abstract Syntax Tree (AST) classes for a language.
 * This script generates an abstract base class and its subclasses for different
 * types of syntax tree nodes, along with a visitor interface for traversing them.
 */
public class GenerateAST {

    /**
     * Entry point of the utility.
     *
     * @param args The output directory where the generated files should be written.
     * @throws IOException If there is an issue writing to the file.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64); // Exit code for command-line usage error
        }

        String outputDir = args[0];

        // Define AST structure for the 'Expr' base class.
        defineAST(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right"
        ));
    }

    /**
     * Generates the base abstract class and its subclasses for an AST.
     *
     * @param outputDir The directory where the file will be generated.
     * @param baseName  The name of the base abstract class (e.g., Expr).
     * @param types     A list of type definitions in the format "ClassName: fields".
     * @throws FileNotFoundException         If the file cannot be created.
     * @throws UnsupportedEncodingException If UTF-8 encoding is not supported.
     */
    private static void defineAST(String outputDir, String baseName, List<String> types)
            throws FileNotFoundException, UnsupportedEncodingException {
        String path = outputDir + "/" + baseName + ".java";
        try (PrintWriter writer = new PrintWriter(path, "UTF-8")) {

            // Package declaration and imports
            writer.println("package crumble;");
            writer.println();
            writer.println("import java.util.List;");
            writer.println();

            // Add the autogenerated docstring for the base class
            writer.println("/**");
            writer.println(" * This is an autogenerated class representing an abstract base class for the AST nodes.");
            writer.println(" * The `Expr` class serves as the base for various node types in the abstract syntax tree.");
            writer.println(" * Each subclass represents a different kind of node in the AST, such as binary expressions,");
            writer.println(" * groupings, literals, or unary expressions.");
            writer.println(" * ");
            writer.println(" * This class is automatically generated by the GenerateAST utility and should not be modified.");
            writer.println(" */");
            writer.println("abstract class " + baseName + " {");

            // Define the visitor interface
            defineVisitor(writer, baseName, types);

            // Define each subclass
            for (String type : types) {
                String className = type.split(":")[0].trim();
                String fields = type.split(":")[1].trim();
                defineType(writer, baseName, className, fields);
            }

            // Define the abstract accept method in the base class
            writer.println();
            writer.println("  abstract <R> R accept(Visitor<R> visitor);");

            writer.println("}");
        }
    }

    /**
     * Defines the Visitor interface inside the base class.
     *
     * @param writer   The PrintWriter for writing to the file.
     * @param baseName The name of the base class (e.g., Expr).
     * @param types    The list of types for which visit methods are generated.
     */
    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("  interface Visitor<R> {");

        // Generate a visit method for each subclass
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
    }

    /**
     * Defines a concrete subclass of the base class.
     *
     * @param writer    The PrintWriter for writing to the file.
     * @param baseName  The name of the base class (e.g., Expr).
     * @param className The name of the subclass.
     * @param fieldList The list of fields in the format "Type name".
     */
    private static void defineType(
            PrintWriter writer, String baseName,
            String className, String fieldList) {
        writer.println("  static class " + className + " extends " +
                baseName + " {");

        // Constructor
        writer.println("    " + className + "(" + fieldList + ") {");

        // Initialize fields from constructor parameters
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }
        writer.println("    }");

        // Implement the accept method for the visitor
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");

        // Define fields
        writer.println();
        for (String field : fields) {
            writer.println("    final " + field + ";");
        }

        writer.println("  }");
    }
}
