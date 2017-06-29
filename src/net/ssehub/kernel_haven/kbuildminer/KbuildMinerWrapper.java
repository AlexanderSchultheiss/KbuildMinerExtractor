package net.ssehub.kernel_haven.kbuildminer;

import java.io.File;
import java.io.IOException;

import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;

/**
 * This class is a Wrapper for calling the tool KbuildMiner and receiving the
 * results. This Wrapper could only run on the complete linux source tree. 
 * 
 * @author Johannes
 * @author Moritz
 * @author Adam
 */
public class KbuildMinerWrapper {
    
    private static final Logger LOGGER = Logger.get();

    /**
     * The directory where this extractor can store its resources. Not null.
     */
    private File resourceDir;
    
    /**
     * Initializes the KbuildMiner.
     * 
     * @param resourceDir The directory where this extractor can stores. Must its resource not be null.
     */
    public KbuildMinerWrapper(File resourceDir) {
        this.resourceDir = resourceDir;
    }

    /**
     * Runs KbuildMiner on the specified product line.
     * 
     * @param sourceTree The path to the source code tree to analyze. Must not be <code>null</code>.
     * @param topFolders A comma separated list of folders to look into relative to sourceTree
     *      must not be null.
     * 
     * @return The output file of KbuildMiner; a mapping of file: presence condition.
     *         <code>null</code> if not successful.
     * 
     * @throws IOException
     *             If executing KbuildMiner fails.
     */
    public File runKbuildMiner(File sourceTree, String topFolders) throws IOException {
        LOGGER.logDebug("runKBuildMiner() called");

        // extract jar to run kconfigreader
        File kbuildMinerJar = new File(resourceDir, "kbuildminer.jar");
        if (!kbuildMinerJar.isFile()) {
            Util.extractJarResourceToFile("net/ssehub/kernel_haven/kbuildminer/res/kbuildminer.jar",
                    kbuildMinerJar);
        }

        File output = File.createTempFile("kbuildminer.pcs.txt", "");
        output.delete();

        ProcessBuilder processBuilder = new ProcessBuilder("java", "-Xmx2G", "-Xms128m", "-Xss50m", "-cp",
                kbuildMinerJar.getAbsolutePath(), "gsd.buildanalysis.linux.KBuildMinerMain",
                "--codebase", sourceTree.getAbsolutePath(),
                "--topFolders", topFolders,
                "--pcOutput", output.getAbsolutePath());
        
        processBuilder.directory(resourceDir);

        boolean success = Util.executeProcess(processBuilder, "KbuildMiner");
        
        Util.deleteFolder(new File(resourceDir, "output"));

        return success ? output : null;
    }

}
