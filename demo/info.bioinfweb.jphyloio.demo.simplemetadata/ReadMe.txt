Simple alignment demo application for JPhyloIO
==============================================

This application is a minimal example demonstrating how to write  
metadata using JPhyloIO in a format-independent way. For more
realistic examples on how to write phylogenetic trees refer
to <http://r.bioinfweb.info/JPhyloIODemoTree> and 
<http://r.bioinfweb.info/JPhyloIODemoMetadata>.

(Note that this demo application writes trees with only one node
and attached support values. This is for demonstration purposes of
writing metadata only. Support values on root nodes of trees with 
only one node would of course make no sense in reality.) 

In order to run this demo application in your own Eclipse Workspace you
need to checkout additional dependency projects from the following SVN 
locations:

- <https://secure.bioinfweb.info/Code/svn/commons.java/trunk/main/info.bioinfweb.commons.core>
- <https://secure.bioinfweb.info/Code/svn/commons.java/trunk/main/info.bioinfweb.commons.bio>
- <https://secure.bioinfweb.info/Code/svn/JPhyloIO/trunk/main/info.bioinfweb.jphyloio.core>