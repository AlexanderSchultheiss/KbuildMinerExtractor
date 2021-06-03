/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.ssehub.kernel_haven.kbuildminer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

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
    private @NonNull File resourceDir;
    
    /**
     * Initializes the KbuildMiner.
     * 
     * @param resourceDir The directory where this extractor can stores. Must its resource not be null.
     */
    public KbuildMinerWrapper(@NonNull File resourceDir) {
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
    public @Nullable File runKbuildMiner(@NonNull File sourceTree, @NonNull String topFolders) throws IOException {
        LOGGER.logDebug("runKBuildMiner() called");

        // extract jar to run kconfigreader
        File kbuildMinerJar = new File(resourceDir, "kbuildminer.jar");
        if (!kbuildMinerJar.isFile()) {
            Util.extractJarResourceToFile("net/ssehub/kernel_haven/kbuildminer/res/kbuildminer.jar", kbuildMinerJar);
        }
        
        // logback.xml is the configuration file for the logger of kbuildminer
        // it is necessary to not spam us with debug messages
        File logback = new File(resourceDir, "logback.xml");
        if (!logback.isFile()) {
            Util.extractJarResourceToFile("net/ssehub/kernel_haven/kbuildminer/res/logback.xml", logback);
        }

        File output = File.createTempFile("kbuildminer.pcs.txt", "");
        output.delete();

        ProcessBuilder processBuilder = new ProcessBuilder("java", "-Xmx2G", "-Xms128m", "-Xss50m",
                // also add resource dir to the class path, because logback.xml will be located there
                "-cp", resourceDir.getAbsolutePath() + File.pathSeparatorChar + kbuildMinerJar.getAbsolutePath(),
                "gsd.buildanalysis.linux.KBuildMinerMain",
                "--codebase", sourceTree.getAbsolutePath(),
                "--topFolders", topFolders,
                "--pcOutput", output.getAbsolutePath());

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        
        processBuilder.directory(resourceDir);

        boolean success = Util.executeProcess(processBuilder, "KbuildMiner", stdout, stderr, 0);

        logOutput(stdout, stderr);

        // delete output/ directory that is (annoyingly) always created by KbuildMiner in the working dir (resDir)
        File outputDir = new File(resourceDir, "output");
        if (outputDir.isDirectory()) {
            Util.deleteFolder(outputDir);
        }

        if (!success && output.isFile()) {
            if (!output.delete()) {
                LOGGER.logWarning("Can't delete kbuildminer output file " + output.getAbsolutePath());
            }
        }
        
        return success ? output : null;
    }

    private void logOutput(ByteArrayOutputStream stdout, ByteArrayOutputStream stderr) {
        String infoString;
        String errorString;
        infoString = stdout.toString();
        errorString = stderr.toString();
        if (infoString.length() > 0) {
            LOGGER.logInfo(stdout.toString());
        }
        if (errorString.length() > 0) {
            LOGGER.logError(stderr.toString());
        }
    }

}
