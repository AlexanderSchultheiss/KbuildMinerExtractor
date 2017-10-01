package net.ssehub.kernel_haven.kbuildminer;

import java.io.File;
import java.io.IOException;

import net.ssehub.kernel_haven.PipelineConfigurator;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.build_model.AbstractBuildModelExtractor;
import net.ssehub.kernel_haven.build_model.BuildModel;
import net.ssehub.kernel_haven.config.BuildExtractorConfiguration;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.Logger;

/**
 * Wrapper to run KbuildMiner.
 * 
 * @author Adam
 * @author Johannes
 */
public class KbuildMinerExtractor extends AbstractBuildModelExtractor {

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
    protected void init(BuildExtractorConfiguration config) throws SetUpException {
        sourceTree = config.getSourceTree();
        if (sourceTree == null) {
            throw new SetUpException("Config does not contain source_tree setting");
        }
        
        
        topFolders = config.getProperty("build.extractor.top_folders");
        if (topFolders == null) {
            String arch = config.getArch();
            if (arch == null) {
                throw new SetUpException("Config does not contain top_folders setting");
            } else {
                // if no top_folders are specified, then we can use default values for Linux, based on arch 
                topFolders = "arch/" + arch + ",block,crypto,drivers,firmware,fs,init,"
                        + "ipc,kernel,lib,mm,net,security,sound";
            }
            
        }
        
        resourceDir = config.getExtractorResourceDir(getClass());
    }

    @Override
    protected BuildModel runOnFile(File target) throws ExtractorException {
        LOGGER.logDebug("Starting extraction");
        
        BuildModel result;
        
        try {
            KbuildMinerWrapper wrapper = new KbuildMinerWrapper(resourceDir);
            
            File output = wrapper.runKbuildMiner(sourceTree, topFolders);
            
            if (output == null) {
                throw new ExtractorException("KbuildMiner execution not successful");
            }
            
            Converter c = new Converter(PipelineConfigurator.instance().getVmProvider().getResult());
            result = c.convert(output);
            
            
        } catch (IOException e) {
            throw new ExtractorException(e);
        }
        
        return result;
    }

    @Override
    protected String getName() {
        return "KbuildMinerExtractor";
    }
    
}
