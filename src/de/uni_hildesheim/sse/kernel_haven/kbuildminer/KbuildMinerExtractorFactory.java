package de.uni_hildesheim.sse.kernel_haven.kbuildminer;

import de.uni_hildesheim.sse.kernel_haven.SetUpException;
import de.uni_hildesheim.sse.kernel_haven.build_model.IBuildExtractorFactory;
import de.uni_hildesheim.sse.kernel_haven.build_model.IBuildModelExtractor;
import de.uni_hildesheim.sse.kernel_haven.config.BuildExtractorConfiguration;

/**
 * Factory for KbuildMiner. 
 * 
 * @author Adam
 * @author Alice
 */
public class KbuildMinerExtractorFactory implements IBuildExtractorFactory {

    @Override
    public IBuildModelExtractor create(BuildExtractorConfiguration config) throws SetUpException {
        return new KbuildMinerExtractor(config);
    }

}
