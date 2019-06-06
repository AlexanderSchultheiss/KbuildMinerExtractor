# KbuildMinerExtractor

![Build Status](https://jenkins-2.sse.uni-hildesheim.de/buildStatus/icon?job=KH_KbuildMinerExtractor)

A build-model extractor for [KernelHaven](https://github.com/KernelHaven/KernelHaven).

This extractor uses [KbuildMiner](https://github.com/ckaestne/KBuildMiner) to analyze the Linux Kernel.

## Capabilities

This extractor finds conditional compilation settings in the Kbuild files (`Kbuild*`, `Makefile*`) of the Linux Kernel.

## Usage

Place [`KbuildMinerExtractor.jar`](https://jenkins-2.sse.uni-hildesheim.de/job/KH_KbuildMinerExtractor/lastSuccessfulBuild/artifact/build/jar/KbuildMinerExtractor.jar) in the plugins folder of KernelHaven.

To use this extractor, set `build.extractor.class` to `net.ssehub.kernel_haven.kbuildminer.KbuildMinerExtractor` in the KernelHaven properties.

## Dependencies

This plugin has no additional dependencies other than KernelHaven.

## License

This plugin is licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.html).

Another license would be possible with the following restriction:
* The plugin contains `kbuildminer.jar` which is under GPLv3. We do not link against kbuildminer, so technically we are not infected by GPL. However a release under a license other than GPLv3 would require the removal of the contained KbuildMiner.

## Used Tools

The following tools are used (and bundled in `res/`) by this plugin:

| Tool | Version | License |
|------|---------|---------|
| [KBuildMiner](https://github.com/ckaestne/KBuildMiner) | [2016-07-01 (00c5b00)](https://github.com/ckaestne/KBuildMiner/commit/00c5b007f70094b5989ed219bc33ac2c55e01e41) | [GPLv3](https://www.gnu.org/licenses/gpl.html) |
