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

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.ssehub.kernel_haven.PipelineConfigurator;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.build_model.AbstractBuildModelExtractor;
import net.ssehub.kernel_haven.build_model.BuildModel;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.config.Setting;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;

/**
 * Wrapper to run KbuildMiner.
 * 
 * @author Adam
 * @author Johannes
 */
public class KbuildMinerExtractor extends AbstractBuildModelExtractor {

    public static final @NonNull Setting<@Nullable String> TOP_FOLDERS
            = new Setting<>("build.extractor.top_folders", Setting.Type.STRING, false, null, "List of top-folders to "
                    + "analyze in the product line. If this is not specfied, it is automatically generated from the "
                    + "arch setting."); 

    private static final Logger LOGGER = Logger.get();

    /**
     * The path to the linux source tree.
     */
    private @NonNull File sourceTree = new File("will be initialized in init()");
    
    /**
     * The top folders to analyze in the source tree.
     */
    private @NonNull String topFolders = "will be initialized in init()";
    
    /**
     * The directory where this extractor can store its resources. Not null.
     */
    private @NonNull File resourceDir = new File("will be initialized in init()");
   
    @Override
    protected void init(@NonNull Configuration config) throws SetUpException {
        sourceTree = config.getValue(DefaultSettings.SOURCE_TREE);
        
        config.registerSetting(TOP_FOLDERS);
        String topFolders = config.getValue(TOP_FOLDERS);
        if (topFolders == null) {
            String arch = config.getValue(DefaultSettings.ARCH);
            if (arch == null) {
                throw new SetUpException("Config does not contain 'arch' setting");
            } else {
                // if no top_folders are specified, then we can use default values for Linux, based on arch 
                try {
                    topFolders = "arch/" + arch + determineTopFolders();
                } catch (IOException e) {
                    throw new SetUpException(e);
                }
            }
            
        }
        LOGGER.logInfo("Top folders: " + topFolders);
        this.topFolders = topFolders;
        
        resourceDir = Util.getExtractorResourceDir(config, getClass());
    }

    @Override
    protected @NonNull BuildModel runOnFile(@NonNull File target) throws ExtractorException {
        LOGGER.logDebug("Starting extraction");
        
        BuildModel result;
        
        File output = null;
        try {
            KbuildMinerWrapper wrapper = new KbuildMinerWrapper(resourceDir);
            
            output = wrapper.runKbuildMiner(sourceTree, topFolders);
            
            if (output == null) {
                throw new ExtractorException("KbuildMiner execution not successful");
            }
            
            if (output.length() == 0) {
                LOGGER.logWarning("Output of KbuildMiner is an empty file");
            }
            
            VariabilityModel varModel = notNull(PipelineConfigurator.instance().getVmProvider()).getResult();
            if (varModel == null) {
                throw new ExtractorException("Did not get a variability model");
            }
            Converter c = new Converter(varModel);
            result = c.convert(output);
            
        } catch (IOException e) {
            throw new ExtractorException(e);
            
        } finally {
            if (output != null && output.isFile()) {
                if (!output.delete()) {
                    LOGGER.logWarning("Can't delete kbuildminer output file " + output.getAbsolutePath());
                }
            }
        }
        
        return result;
    }

    @Override
    protected @NonNull String getName() {
        return "KbuildMinerExtractor";
    }

    private String determineTopFolders() throws IOException {
        LOGGER.logInfo("Determining top folders in " + sourceTree);
        try {
            final List<Path> makefiles = Files.find(sourceTree.toPath(),
                    Integer.MAX_VALUE,
                    (path, basicFileAttributes) -> path.toFile().isFile() && isMakefileName(path.toFile().getName()),
                    FileVisitOption.FOLLOW_LINKS).collect(Collectors.toList());
            StringBuilder topFolders = new StringBuilder();
            Set<String> folderNames = new HashSet<>();
            for (Path path : makefiles) {
                Path relativePath = sourceTree.toPath().relativize(path).getParent();
                if (relativePath != null) {
                    String topFolder = relativePath.toString();
                    if (!topFolder.equals("arch") && !topFolder.equals("Makefile")) {
                        folderNames.add(topFolder);
                    }
                }
            }
            folderNames.forEach(name -> topFolders.append(",").append(name));
            return topFolders.toString();
        } catch (IOException e) {
            LOGGER.logException("Was not able to retrieve top folders ", e);
            throw e;
        }
    }

    private boolean isMakefileName(String fileName) {
        return fileName.equals("Makefile") || fileName.equals("Kbuild") || fileName.equals("Kbuild.src");
    }
}
