# KbuildMinerExtractor

A build-model extractor for [KernelHaven](https://github.com/KernelHaven/KernelHaven).
This extractor uses [KbuildMiner](https://github.com/ckaestne/KBuildMiner) to analyze the Linux Kernel.

## Capabilities

This extractor finds conditional compilation settings in the Kbuild files (`Kbuild*`, `Makefile*`) of the Linux Kernel.

## Usage

To use this extractor, set `build.extractor.class` to `net.ssehub.kernel_haven.kbuildminer.KbuildMinerExtractor` in the KernelHaven properties.

### Dependencies

This extractor has no additional dependencies other than KernelHaven.

### Configuration

In addition to the default ones, this extractor has the following configuration options in the KernelHaven properties:

| Key | Mandatory | Default | Example | Description |
|-----|-----------|---------|---------|-------------|
| `build.extracator.top_folders` | No | Generated from `arch` setting | `kernel,drivers,arch/x86` | List of top-folders to analyze in the product line. |

## License
This extractor is licensed under GPLv3. Another license would be possible with following restrictions:

The extractor contains `kbuildminer.jar` which is under GPL-3.0. We do not link against kbuildminer, so technically we are not infected by GPL. However a release under a license other than GPL-3.0 would require the removal of the contained undertaker.
