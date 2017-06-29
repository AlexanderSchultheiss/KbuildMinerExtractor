package net.ssehub.kernel_haven.kbuildminer;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.build_model.IBuildExtractorFactory;
import net.ssehub.kernel_haven.build_model.IBuildModelExtractor;
import net.ssehub.kernel_haven.config.BuildExtractorConfiguration;

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
