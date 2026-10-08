package waka.spike;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

import weka.core.Instances;
import weka.core.Version;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Stage S00 side check: weka-stable links, loads an ARFF file and reports its shape, with no UI
 * of any kind. Deliberately a task and not an exit test, because the stage's three bets are about
 * the user interface and this one is about a build file.
 *
 * <p>Run it with {@code java.awt.headless=true}, which the pom sets. FACTS warns that some Weka
 * entry points boot Swing inside our process; with headless on, such an attempt throws here
 * instead of quietly succeeding and failing in S02, where the options editor already leans on
 * Weka's OptionHandler.
 *
 * <p>The second half is about commons-compress. weka-stable marks it optional, so it is not
 * inherited, and a missing optional dependency is invisible until a user opens a compressed file.
 */
public final class WekaLinkProbe {

    private static final Path DEFAULT_DATASET =
            Path.of("C:\\Program Files\\Weka-3-9-6\\data\\iris.arff");

    private WekaLinkProbe() {
    }

    public static void main(String[] arguments) throws Exception {
        Path dataset = arguments.length > 0 ? Path.of(arguments[0]) : DEFAULT_DATASET;

        System.out.println("weka | version=" + Version.VERSION
                + " headless=" + System.getProperty("java.awt.headless"));

        if (!Files.exists(dataset)) {
            throw new IllegalStateException("no dataset at " + dataset);
        }

        Instances plainFile;
        try (BufferedReader reader = Files.newBufferedReader(dataset, StandardCharsets.UTF_8)) {
            plainFile = new Instances(reader);
        }
        System.out.printf("%s: %d instances, %d attributes%n",
                dataset.getFileName(), plainFile.numInstances(), plainFile.numAttributes());

        reportCompressionSupport(dataset, plainFile);
    }

    private static void reportCompressionSupport(Path dataset, Instances plainFile) throws Exception {
        // Resolution first: the class either reached the runtime classpath or it did not.
        Class<?> bzip2Stream = Class.forName(
                "org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream");
        System.out.println("commons-compress | on the runtime classpath: " + bzip2Stream.getName());

        // Then the path a user actually takes: open a compressed dataset and get the same shape.
        Path workingDirectory = Files.createTempDirectory("waka-spike-");
        Path compressedFile = workingDirectory.resolve(dataset.getFileName() + ".gz");
        try {
            try (InputStream source = Files.newInputStream(dataset);
                 OutputStream target = new GZIPOutputStream(Files.newOutputStream(compressedFile))) {
                source.transferTo(target);
            }

            Instances fromCompressed = new DataSource(compressedFile.toString()).getDataSet();
            boolean sameShape = fromCompressed.numInstances() == plainFile.numInstances()
                    && fromCompressed.numAttributes() == plainFile.numAttributes();
            System.out.printf("%s: %d instances, %d attributes | same shape as the plain file: %b%n",
                    compressedFile.getFileName(), fromCompressed.numInstances(),
                    fromCompressed.numAttributes(), sameShape);
        } finally {
            // Weka's DataSource keeps the loader, and the loader keeps the file open, so the
            // delete loses a race with it on Windows. The temporary directory is the operating
            // system's to clean up; failing the probe over it would report a false negative.
            compressedFile.toFile().deleteOnExit();
            workingDirectory.toFile().deleteOnExit();
        }
    }
}
