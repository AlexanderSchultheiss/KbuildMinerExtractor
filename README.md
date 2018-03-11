# KbuildMinerExtractor

![Build Status](https://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=KernelHaven_KbuildMinerExtractor)

A build-model extractor for [KernelHaven](https://github.com/KernelHaven/KernelHaven).

This extractor uses [KbuildMiner](https://github.com/ckaestne/KBuildMiner) to analyze the Linux Kernel.

## Capabilities

This extractor finds conditional compilation settings in the Kbuild files (`Kbuild*`, `Makefile*`) of the Linux Kernel.

## Usage

Place [`KbuildminerExtractor.jar`](https://jenkins.sse.uni-hildesheim.de/view/KernelHaven/job/KernelHaven_KbuildMinerExtractor/lastSuccessfulBuild/artifact/build/jar/KbuildminerExtractor.jar) in the plugins folder of KernelHaven.

To use this extractor, set `build.extractor.class` to `net.ssehub.kernel_haven.kbuildminer.KbuildMinerExtractor` in the KernelHaven properties.

## Dependencies

This plugin has no additional dependencies other than KernelHaven.

## License

This plugin is licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.html).

Another license would be possible with the following restriction:
* The plugin contains `kbuildminer.jar` which is under GPLv3. We do not link against kbuildminer, so technically we are not infected by GPL. However a release under a license other than GPLv3 would require the removal of the contained KbuildMiner.
