package net.ssehub.kernel_haven.kbuildminer;

import java.io.File;
import java.io.IOException;

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

/**
 * Wrapper to run KbuildMiner.
 * 
 * @author Adam
 * @author Johannes
 */
public class KbuildMinerExtractor extends AbstractBuildModelExtractor {

    public static final Setting<String> TOP_FOLDERS
            = new Setting<>("build.extractor.top_folders", Setting.Type.STRING, false, null, "List of top-folders to "
                    + "analyze in the product line. If this is not specfied, it is automatically generated from the "
                    + "arch setting."); 

    private static final Logger LOGGER = Logger.get();

    /**
     * The path to the linux source tree.
     */
    private File sourceTree;
    
    /**
     * The top folders to analyze in the source tree.
     */
    private String topFolders;
    
    /**
     * The directory where this extractor can store its resources. Not null.
     */
    private File resourceDir;
   
    @Override
    protected void init(Configuration config) throws SetUpException {
        sourceTree = config.getValue(DefaultSettings.SOURCE_TREE);
        
        config.registerSetting(TOP_FOLDERS);
        topFolders = config.getValue(TOP_FOLDERS);
        if (topFolders == null) {
            String arch = config.getValue(DefaultSettings.ARCH);
            if (arch == null) {
                throw new SetUpException("Config does not contain top_folders setting");
            } else {
                // if no top_folders are specified, then we can use default values for Linux, based on arch 
                topFolders = "arch/" + arch + ",block,crypto,drivers,firmware,fs,init,"
                        + "ipc,kernel,lib,mm,net,security,sound";
            }
            
        }
        
        resourceDir = Util.getExtractorResourceDir(config, getClass());
    }

    @Override
    protected BuildModel runOnFile(File target) throws ExtractorException {
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
            
            Converter c = new Converter(PipelineConfigurator.instance().getVmProvider().getResult());
            result = c.convert(output);
            
        } catch (IOException e) {
            throw new ExtractorException(e);
            
        } finally {
            if (output != null && output.isFile()) {
                output.delete();
            }
        }
        
        return result;
    }

    @Override
    protected String getName() {
        return "KbuildMinerExtractor";
    }
    
}
