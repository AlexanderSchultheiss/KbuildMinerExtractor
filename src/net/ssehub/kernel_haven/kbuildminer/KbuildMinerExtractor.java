package net.ssehub.kernel_haven.kbuildminer;

import java.io.File;
import java.io.IOException;

import net.ssehub.kernel_haven.PipelineConfigurator;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.build_model.BuildModel;
import net.ssehub.kernel_haven.build_model.BuildModelProvider;
import net.ssehub.kernel_haven.build_model.IBuildModelExtractor;
import net.ssehub.kernel_haven.config.BuildExtractorConfiguration;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.Logger;

/**
 * Wrapper to run KbuildMiner.
 * 
 * @author Adam
 * @author Johannes
 */
public class KbuildMinerExtractor implements IBuildModelExtractor, Runnable {

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
     * The provider to notify about results.
     */
    private BuildModelProvider provider;
    
    /**
     * The directory where this extractor can store its resources. Not null.
     */
    private File resourceDir;
    
    private boolean stopRequested;

    /**
     * Creates a new KbuildMiner extractor.
     * 
     * @param config The user configuration file.
     * 
     * @throws SetUpException If the user configuration is not valid.
     */
    public KbuildMinerExtractor(BuildExtractorConfiguration config) throws SetUpException {
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
    public void start() {
        Thread th = new Thread(this);
        th.setName("KbuildMinerExtractor");
        th.start();
    }
    
    @Override
    public void stop() {
        synchronized (this) {
            stopRequested = true;
        }
    }
    
    /**
     * Checks if the provider requested that we stop our extraction.
     * 
     * @return Whether stop is requested.
     */
    private synchronized boolean isStopRequested() {
        return stopRequested;
    }

    @Override
    public void setProvider(BuildModelProvider provider) {
        this.provider = provider;
    }

    @Override
    public void run() {
        LOGGER.logDebug("Starting extraction");
        
        try {
            KbuildMinerWrapper wrapper = new KbuildMinerWrapper(resourceDir);
            
            File output = wrapper.runKbuildMiner(sourceTree, topFolders);
            
            if (output != null && !isStopRequested()) {
                Converter c = new Converter(PipelineConfigurator.instance().getVmProvider().getResult());
                BuildModel bm = c.convert(output);
                
                if (!isStopRequested()) {
                    provider.setResult(bm);
                }
                
            } else if (!isStopRequested()) {
                provider.setException(new ExtractorException("KconfigReader execution not successful"));
            }
            
        } catch (IOException | ExtractorException e) {
            if (!isStopRequested()) {
                provider.setException(new ExtractorException(e));
            }
        }
    }
    
}
