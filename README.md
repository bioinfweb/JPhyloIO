# JPhyloIO

JPhyloIO is an open source *Java* library for reading and writing phylogenetic file formats. The main aim is to provide a library that allows access to various formats using a single interface, while being independent of the concrete application data model, to achieve maximal flexibility. It supports event based reading and writing of the following alignment and tree formats:

* [*NeXML*](https://github.com/NeXML) including its different types of metadata
* *Nexus* (including the `TAXA`, `DATA`, `CHARACTERS`, `UNALIGNED`, `TREES` and `SETS` blocks, as well as the `MIXED` data type extension defined by [*MrBayes*](https://github.com/NBISweden/MrBayes)). In addition a *Nexus* API is offered that allows application developers to easily add support for additional (custom) blocks and commands.
* *PhyloXML*
* *FASTA* (including support for *FASTA* comments and column indices)
* *Newick* tree format
* *Phylip*
* *Extended Phylip*
* *MEGA* (including different types character set definitions, reading only)
* *PDE* (the format of the alignment editor [*PhyDE*](http://phyde.de/), reading only)
* *XTG* (the format of the phylogenetic tree editor [*TreeGraph 2*](http://treegraph.bioinfweb.info/), reading only)

Application developers are able to implement format-independent data processing by including event based readers from *JPhyloIO* via the abstract strategy pattern. All readers in *JPhyloIO* are designed to deal with large amounts of data (alignments with many and/or very long sequences, large trees) without using a great amount of resources (CPU or RAM).

*JPhyloIO* is distributed under [LGPL](http://bioinfweb.info/JPhyloIO/License/LGPL). More information can be found at http://bioinfweb.info/JPhyloIO/.

## Getting started

We have several [tutorials](http://r.bioinfweb.info/JPIODemo) available that show how to use *JPhyloIO* for reading and writing phylogentic data step by step. Additionally the [general documentation](http://bioinfweb.info/JPhyloIO/Documentation) provides and overview and contains a detailed [JavaDoc](http://bioinfweb.info/JPhyloIO/Documentation/API/Latest/).

If you have further questions, feel free to ask one on our [ResearchGate project page](http://r.bioinfweb.info/RGJPhyloIO) or contact support@bioinfweb.info.

## Source code

This *GitHub* repository in a synchronized mirror of the [master repository at bioinfweb](http://bioinfweb.info/Code/sventon/repos/JPhyloIO/list/). Feedback and pull requests are welcome. Synchronization was made possible by [*SubGit*](https://subgit.com/).

## License

The latest versions of *JPhyloIO* are distrubuted under [GNU General Lesser Public License Version 3](http://bioinfweb.info/JPhyloIO/License/LGPL). See [NOTICE.txt](https://github.com/bioinfweb/JPhyloIO/blob/master/main/info.bioinfweb.jphyloio.core/src/NOTICE.txt) for further details.

This product includes dependencies developed by the [Apache Software Foundation](http://www.apache.org/) distributed under the terms of the [Apache License Version 2.0](https://github.com/bioinfweb/JPhyloIO/blob/master/main/info.bioinfweb.jphyloio.core/src/APACHE-LICENSE.txt).

## Binary releases

Binary releases can be found at http://bioinfweb.info/JPhyloIO/Download.
