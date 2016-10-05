/**
 * Contains implementations of data adapters that directly store their modeled data in mutable lists or maps instead of delegating to an
 * application business model.
 * <p>
 * Note implementing application specific adapters delegating to its business model should be the preferred way for performance reasons.
 * It should be avoided to copy large amounts of data from the application business model to instances of the classes provided here.
 * These classes are meant to be used, of according data is not modeled by the application itself at all (e.g. if a tree/network group 
 * adapter providing access to the single tree modeled by the application is needed).  
 * 
 * @author Ben St&ouml;ver
 * @author Sarah Wiechers
 */
package info.bioinfweb.jphyloio.dataadapters.implementations.store;