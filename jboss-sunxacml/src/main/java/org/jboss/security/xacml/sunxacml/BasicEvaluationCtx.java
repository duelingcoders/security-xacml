
/*
 * @(#)BasicEvaluationCtx.java
 *
 * Copyright 2004-2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   2. Redistribution in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */

package org.jboss.security.xacml.sunxacml;





import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.security.xacml.sunxacml.attr.AttributeDesignator;
import org.jboss.security.xacml.sunxacml.attr.AttributeValue;
import org.jboss.security.xacml.sunxacml.attr.BagAttribute;
import org.jboss.security.xacml.sunxacml.attr.DateAttribute;
import org.jboss.security.xacml.sunxacml.attr.DateTimeAttribute;
import org.jboss.security.xacml.sunxacml.attr.StringAttribute;
import org.jboss.security.xacml.sunxacml.attr.TimeAttribute;
import org.jboss.security.xacml.sunxacml.cond.EvaluationResult;
import org.jboss.security.xacml.sunxacml.ctx.Attribute;
import org.jboss.security.xacml.sunxacml.ctx.RequestCtx;
import org.jboss.security.xacml.sunxacml.ctx.Subject;
import org.jboss.security.xacml.sunxacml.finder.AttributeFinder;
import org.w3c.dom.Node;


/**
 * A basic implementation of <code>EvaluationCtx</code> that is created from
 * an XACML Request and falls back on an AttributeFinder if a requested
 * value isn't available in the Request.
 * <p>
 * Note that this class can do some optional caching for current date, time,
 * and dateTime values (defined by a boolean flag to the constructors). The
 * XACML specification requires that these values always be available, but it
 * does not specify whether or not they must remain constant over the course
 * of an evaluation if the values are being generated by the PDP (if the
 * values are provided in the Request, then obviously they will remain
 * constant). The default behavior is for these environment values to be
 * cached, so that (for example) the current time remains constant over the
 * course of an evaluation.
 *
 * @since 1.2
 * @author Seth Proctor
 */
@SuppressWarnings({"unchecked", "rawtypes"}) 
public class BasicEvaluationCtx implements EvaluationCtx
{
    // the finder to use if a value isn't in the request
    private AttributeFinder finder;

    // the DOM root the original RequestContext document
    private Node requestRoot;

    // the 4 maps that contain the attribute data
    private HashMap subjectMap;
    private HashMap resourceMap;
    private HashMap actionMap;
    private HashMap environmentMap;

    // the resource and its scope
    private AttributeValue resourceId;
    private int scope;

    // the cached current date, time, and datetime, which we may or may
    // not be using depending on how this object was constructed
    private DateAttribute currentDate;
    private TimeAttribute currentTime;
    private DateTimeAttribute currentDateTime;
    private boolean useCachedEnvValues;

    // the logger we'll use for all messages
    private static final Logger logger =
        Logger.getLogger(BasicEvaluationCtx.class.getName());

    /**
     * Constructs a new <code>BasicEvaluationCtx</code> based on the given
     * request. The resulting context will cache current date, time, and
     * dateTime values so they remain constant for this evaluation.
     *
     * @param request the request
     *
     * @throws ParsingException if a required attribute is missing, or if there
     *                          are any problems dealing with the request data
     */
    public BasicEvaluationCtx(RequestCtx request) throws ParsingException {
        this(request, null, true);
    }

    /**
     * Constructs a new <code>BasicEvaluationCtx</code> based on the given
     * request.
     *
     * @param request the request
     * @param cacheEnvValues whether or not to cache the current time, date,
     *                       and dateTime so they are constant for the scope
     *                       of this evaluation
     *
     * @throws ParsingException if a required attribute is missing, or if there
     *                          are any problems dealing with the request data
     */
    public BasicEvaluationCtx(RequestCtx request, boolean cacheEnvValues)
        throws ParsingException
    {
        this(request, null, cacheEnvValues);
    }

    /**
     * Constructs a new <code>BasicEvaluationCtx</code> based on the given
     * request, and supports looking outside the original request for attribute
     * values using the <code>AttributeFinder</code>. The resulting context
     * will cache current date, time, and dateTime values so they remain
     * constant for this evaluation.
     *
     * @param request the request
     * @param finder an <code>AttributeFinder</code> to use in looking for
     *               attributes that aren't in the request
     *
     * @throws ParsingException if a required attribute is missing, or if there
     *                          are any problems dealing with the request data
     */
    public BasicEvaluationCtx(RequestCtx request, AttributeFinder finder)
        throws ParsingException
    {
        this(request, finder, true);
    }

    /**
     * Constructs a new <code>BasicEvaluationCtx</code> based on the given
     * request, and supports looking outside the original request for attribute
     * values using the <code>AttributeFinder</code>.
     *
     * @param request the request
     * @param finder an <code>AttributeFinder</code> to use in looking for
     *               attributes that aren't in the request
     * @param cacheEnvValues whether or not to cache the current time, date,
     *                       and dateTime so they are constant for the scope
     *                       of this evaluation
     *
     * @throws ParsingException if a required attribute is missing, or if there
     *                          are any problems dealing with the request data
     */
    public BasicEvaluationCtx(RequestCtx request, AttributeFinder finder,
                              boolean cacheEnvValues) throws ParsingException {
        // keep track of the finder
        this.finder = finder;

        // remember the root of the DOM tree for XPath queries
        requestRoot = request.getDocumentRoot();

        // initialize the cached date/time values so it's clear we haven't
        // retrieved them yet
        this.useCachedEnvValues = cacheEnvValues;
        currentDate = null;
        currentTime = null;
        currentDateTime = null;

        // get the subjects, make sure they're correct, and setup tables
        subjectMap = new HashMap();
        setupSubjects(request.getSubjectsAsList());

        // next look at the Resource data, which needs to be handled specially
        resourceMap = new HashMap();
        setupResource(request.getResourceAsList());
        
        // setup the action data, which is generic
        actionMap = new HashMap();
        mapAttributes(request.getActionAsList(), actionMap);

        // finally, set up the environment data, which is also generic
        environmentMap = new HashMap();
        mapAttributes(request.getEnvironmentAttributesAsList(), environmentMap);
    }

    /**
     * This is quick helper function to provide a little structure for the
     * subject attributes so we can search for them (somewhat) quickly. The
     * basic idea is to have a map indexed by SubjectCategory that keeps
     * Maps that in turn are indexed by id and keep the unique ctx.Attribute
     * objects.
     */
    private void setupSubjects(List subjects) throws ParsingException {
        // make sure that there is at least one Subject
        if (subjects.size() == 0)
            throw new ParsingException("Request must a contain subject");

        // now go through the subject attributes
        Iterator it = subjects.iterator();
        while (it.hasNext()) {
            Subject subject = (Subject)(it.next());

            URI category = subject.getCategory();
            Map categoryMap = null;

            // see if we've already got a map for the category
            if (subjectMap.containsKey(category)) {
                categoryMap = (Map)(subjectMap.get(category));
            } else {
                categoryMap = new HashMap();
                subjectMap.put(category, categoryMap);
            }

            // iterate over the set of attributes
            Iterator attrIterator = subject.getAttributesAsList().iterator();

            while (attrIterator.hasNext()) {
                Attribute attr = (Attribute)(attrIterator.next());
                String id = attr.getId().toString();

                if (categoryMap.containsKey(id)) {
                    // add to the existing set of Attributes w/this id
                    List existingIds = (List)(categoryMap.get(id));
                    existingIds.add(attr);
                } else {
                    // this is the first Attr w/this id
                    List newIds = new ArrayList();
                    newIds.add(attr);
                    categoryMap.put(id, newIds);
                }
            }
        }
    }

    /**
     * This basically does the same thing that the other types need
     * to do, except that we also look for a resource-id attribute, not
     * because we're going to use, but only to make sure that it's actually
     * there, and for the optional scope attribute, to see what the scope
     * of the attribute is
     */
    private void setupResource(List resource) throws ParsingException {
        mapAttributes(resource, resourceMap);

        // make sure there resource-id attribute was included
        if (! resourceMap.containsKey(RESOURCE_ID)) { 
            System.err.println("Resource must contain resource-id attr");
            //throw new ParsingException("resource missing resource-id");
        } /*else { 
            // make sure there's only one value for this
            Set set = (Set)(resourceMap.get(RESOURCE_ID));
            if (set.size() > 1) {
                System.err.println("Resource may contain only one " +
                                   "resource-id Attribute");
                throw new ParsingException("too many resource-id attrs");
            } else {
                // keep track of the resource-id attribute
                resourceId = ((Attribute)(set.iterator().next())).getValue();
            }
        
        } */
        
        //SECURITY-162: Relax resource-id requirement
        if(this.resourceId == null)
           this.resourceId = new StringAttribute("");
        
        // see if a resource-scope attribute was included
        if (resourceMap.containsKey(RESOURCE_SCOPE)) {
            Set set = (Set)(resourceMap.get(RESOURCE_SCOPE));

            // make sure there's only one value for resource-scope
            if (set.size() > 1) {
                System.err.println("Resource may contain only one " +
                                   "resource-scope Attribute");
                throw new ParsingException("too many resource-scope attrs");
            }

            Attribute attr = (Attribute)(set.iterator().next());
            AttributeValue attrValue = attr.getValue();

            // scope must be a string, so throw an exception otherwise
            if (! attrValue.getType().toString().
                equals(StringAttribute.identifier))
                throw new ParsingException("scope attr must be a string");

            String value = ((StringAttribute)attrValue).getValue();
            
            if (value.equals("Immediate")) {
                scope = SCOPE_IMMEDIATE;
            } else if (value.equals("Children")) {
                scope = SCOPE_CHILDREN;
            } else if (value.equals("Descendants")) {
                scope = SCOPE_DESCENDANTS;
            } else {
                System.err.println("Unknown scope type: " + value);
                throw new ParsingException("invalid scope type: " + value);
            }
        } else {
            // by default, the scope is always Immediate
            scope = SCOPE_IMMEDIATE;
        }
    }

    /**
     * Generic routine for resource, attribute and environment attributes
     * to build the lookup map for each. The Form is a Map that is indexed
     * by the String form of the attribute ids, and that contains Sets at
     * each entry with all attributes that have that id
     */
    private void mapAttributes(List input, Map output) {
        Iterator it = input.iterator();
        while (it.hasNext()) {
            Attribute attr = (Attribute)(it.next());
            String id = attr.getId().toString();

            if (output.containsKey(id)) {
                List set = (List)(output.get(id));
                set.add(attr);
            } else {
                List set = new ArrayList();
                set.add(attr);
                output.put(id, set);
            }
        }
    }

    /**
     * Returns the DOM root of the original RequestType XML document.
     *
     * @return the DOM root node
     */
    public Node getRequestRoot() {
        return requestRoot;
    }

    /**
     * Returns the resource scope of the request, which will be one of the
     * three fields denoting Immediate, Children, or Descendants.
     *
     * @return the scope of the resource in the request
     */
    public int getScope() {
        return scope;
    }

    /**
     * Returns the resource named in the request as resource-id.
     *
     * @return the resource
     */
    public AttributeValue getResourceId() {
        return resourceId;
    }

    /**
     * Changes the value of the resource-id attribute in this context. This
     * is useful when you have multiple resources (ie, a scope other than
     * IMMEDIATE), and you need to keep changing only the resource-id to
     * evaluate the different effective requests.
     *
     * @param resourceId the new resource-id value
     */
    public void setResourceId(AttributeValue resourceId) {
        this.resourceId = resourceId;

        // there will always be exactly one value for this attribute
        Set attrSet = (Set)(resourceMap.get(RESOURCE_ID));
        Attribute attr = (Attribute)(attrSet.iterator().next());
        
        // remove the old value...
        attrSet.remove(attr);

        // ...and insert the new value
        attrSet.add(new Attribute(attr.getId(), attr.getIssuer(),
                                  attr.getIssueInstant(), resourceId));
    }

    /**
     * Returns the value for the current time. The current time, current
     * date, and current dateTime are consistent, so that they all
     * represent the same moment. If this is the first time that one
     * of these three values has been requested, and caching is enabled,
     * then the three values will be resolved and stored.
     * <p>
     * Note that the value supplied here applies only to dynamically
     * resolved values, not those supplied in the Request. In other words,
     * this always returns a dynamically resolved value local to the PDP,
     * even if a different value was supplied in the Request. This is
     * handled correctly when the value is requested by its identifier.
     *
     * @return the current time
     */
    public synchronized TimeAttribute getCurrentTime() {
        long millis = dateTimeHelper();

        if (useCachedEnvValues)
            return currentTime;
        else
            return new TimeAttribute(new Date(millis));
    }

    /**
     * Returns the value for the current date. The current time, current
     * date, and current dateTime are consistent, so that they all
     * represent the same moment. If this is the first time that one
     * of these three values has been requested, and caching is enabled,
     * then the three values will be resolved and stored.
     * <p>
     * Note that the value supplied here applies only to dynamically
     * resolved values, not those supplied in the Request. In other words,
     * this always returns a dynamically resolved value local to the PDP,
     * even if a different value was supplied in the Request. This is
     * handled correctly when the value is requested by its identifier.
     *
     * @return the current date
     */
    public synchronized DateAttribute getCurrentDate() {
        long millis = dateTimeHelper();

        if (useCachedEnvValues)
            return currentDate;
        else
            return new DateAttribute(new Date(millis));
    }

    /**
     * Returns the value for the current dateTime. The current time, current
     * date, and current dateTime are consistent, so that they all
     * represent the same moment. If this is the first time that one
     * of these three values has been requested, and caching is enabled,
     * then the three values will be resolved and stored.
     * <p>
     * Note that the value supplied here applies only to dynamically
     * resolved values, not those supplied in the Request. In other words,
     * this always returns a dynamically resolved value local to the PDP,
     * even if a different value was supplied in the Request. This is
     * handled correctly when the value is requested by its identifier.
     *
     * @return the current dateTime
     */
    public synchronized DateTimeAttribute getCurrentDateTime() {
        long millis = dateTimeHelper();

        if (useCachedEnvValues)
            return currentDateTime;
        else
            return new DateTimeAttribute(new Date(millis));
    }

    /**
     * Private helper that figures out if we need to resolve new values,
     * and returns either the current moment (if we're not caching) or
     * -1 (if we are caching)
     */
    private long dateTimeHelper() {
        // if we already have current values, then we can stop (note this
        // always means that we're caching)
        if (currentTime != null)
            return -1;

        // get the current moment
        Date time = new Date();
        long millis = time.getTime();

        // if we're not caching then we just return the current moment
        if (! useCachedEnvValues) {
            return millis;
        } else {
            // we're caching, so resolve all three values, making sure
            // to use clean copies of the date object since it may be
            // modified when creating the attributes
            currentTime = new TimeAttribute(time);
            currentDate = new DateAttribute(new Date(millis));
            currentDateTime = new DateTimeAttribute(new Date(millis));
        }
        
        return -1;
    }

    /**
     * Returns attribute value(s) from the subject section of the request
     * that have no issuer.
     *
     * @param type the type of the attribute value(s) to find
     * @param id the id of the attribute value(s) to find
     * @param category the category the attribute value(s) must be in
     *
     * @return a result containing a bag either empty because no values were
     * found or containing at least one value, or status associated with an
     * Indeterminate result
     */
    public EvaluationResult getSubjectAttribute(URI type, URI id,
                                                URI category) {
        return getSubjectAttribute(type, id, null, category);
    }

    /**
     * Returns attribute value(s) from the subject section of the request.
     *
     * @param type the type of the attribute value(s) to find
     * @param id the id of the attribute value(s) to find
     * @param issuer the issuer of the attribute value(s) to find or null
     * @param category the category the attribute value(s) must be in
     *
     * @return a result containing a bag either empty because no values were
     * found or containing at least one value, or status associated with an
     * Indeterminate result
     */
    public EvaluationResult getSubjectAttribute(URI type, URI id, URI issuer,
                                                URI category) {
        // This is the same as the other three lookups except that this
        // has an extra level of indirection that needs to be handled first
        Map map = (Map)(subjectMap.get(category));

        if (map == null) {
            // the request didn't have that category, so we should try asking
            // the attribute finder
            return callHelper(type, id, issuer, category,
                              AttributeDesignator.SUBJECT_TARGET);
        }
        
        return getGenericAttributes(type, id, issuer, map, category,
                                    AttributeDesignator.SUBJECT_TARGET);
    }
    
    /**
     * Returns attribute value(s) from the resource section of the request.
     *
     * @param type the type of the attribute value(s) to find
     * @param id the id of the attribute value(s) to find
     * @param issuer the issuer of the attribute value(s) to find or null
     *
     * @return a result containing a bag either empty because no values were
     * found or containing at least one value, or status associated with an
     * Indeterminate result
     */
    public EvaluationResult getResourceAttribute(URI type, URI id,
                                                 URI issuer) {
        return getGenericAttributes(type, id, issuer, resourceMap, null,
                                    AttributeDesignator.RESOURCE_TARGET);
    }

    /**
     * Returns attribute value(s) from the action section of the request.
     *
     * @param type the type of the attribute value(s) to find
     * @param id the id of the attribute value(s) to find
     * @param issuer the issuer of the attribute value(s) to find or null
     *
     * @return a result containing a bag either empty because no values were
     * found or containing at least one value, or status associated with an
     * Indeterminate result
     */
    public EvaluationResult getActionAttribute(URI type, URI id, URI issuer) {
        return getGenericAttributes(type, id, issuer, actionMap, null,
                                    AttributeDesignator.ACTION_TARGET);
    }

    /**
     * Returns attribute value(s) from the environment section of the request.
     *
     * @param type the type of the attribute value(s) to find
     * @param id the id of the attribute value(s) to find
     * @param issuer the issuer of the attribute value(s) to find or null
     *
     * @return a result containing a bag either empty because no values were
     * found or containing at least one value, or status associated with an
     * Indeterminate result
     */
    public EvaluationResult getEnvironmentAttribute(URI type, URI id,
                                                    URI issuer) {
        return getGenericAttributes(type, id, issuer, environmentMap, null,
                                    AttributeDesignator.ENVIRONMENT_TARGET);
    }

    /**
     * Helper function for the resource, action and environment methods
     * to get an attribute.
     */
    private EvaluationResult getGenericAttributes(URI type, URI id, URI issuer,
                                                  Map map, URI category,
                                                  int designatorType) {
        // try to find the id
        List attrSet = (List)(map.get(id.toString()));
        if (attrSet == null) {
            // the request didn't have an attribute with that id, so we should
            // try asking the attribute finder
            return callHelper(type, id, issuer, category, designatorType);
        }

        // now go through each, considering each Attribute object
        List attributes = new ArrayList();
        Iterator it = attrSet.iterator();

        while (it.hasNext()) {
            Attribute attr = (Attribute)(it.next());

            // make sure the type and issuer are correct
            if ((attr.getType().equals(type)) &&
                ((issuer == null) ||
                 ((attr.getIssuer() != null) &&
                  (attr.getIssuer().equals(issuer.toString()))))) {

                // if we got here, then we found a match, so we want to pull
                // out the values and put them in out list
                attributes.addAll(attr.getValues());
            }
        }

        // see if we found any acceptable attributes
        if (attributes.size() == 0) {
            // we failed to find any that matched the type/issuer, or all the
            // Attribute types were empty...so ask the finder
            if (logger.isLoggable(Level.FINE))
                logger.fine("Attribute not in request: " + id.toString() +
                            " ... querying AttributeFinder");

            return callHelper(type, id, issuer, category, designatorType);
        }
                
        // if we got here, then we found at least one useful AttributeValue
        return new EvaluationResult(new BagAttribute(type, attributes));
    }

    /**
     * Private helper that calls the finder if it's non-null, or else returns
     * an empty bag
     */
    private EvaluationResult callHelper(URI type, URI id, URI issuer,
                                        URI category, int adType) {
        if (finder != null) {
            return finder.findAttribute(type, id, issuer, category,
                                        this, adType);
        } else {
            logger.warning("Context tried to invoke AttributeFinder but was " +
                           "not configured with one");

            return new EvaluationResult(BagAttribute.createEmptyBag(type));
        }
    }

    /**
     * Returns the attribute value(s) retrieved using the given XPath
     * expression.
     *
     * @param contextPath the XPath expression to search
     * @param namespaceNode the DOM node defining namespace mappings to use,
     *                      or null if mappings come from the context root
     * @param type the type of the attribute value(s) to find
     * @param xpathVersion the version of XPath to use
     *
     * @return a result containing a bag either empty because no values were
     * found or containing at least one value, or status associated with an
     * Indeterminate result
     */
    public EvaluationResult getAttribute(String contextPath,
                                         Node namespaceNode, URI type,
                                         String xpathVersion) {
        if (finder != null) {
            return finder.findAttribute(contextPath, namespaceNode, type, this,
                                        xpathVersion);
        } else {
            logger.warning("Context tried to invoke AttributeFinder but was " +
                           "not configured with one");

            return new EvaluationResult(BagAttribute.createEmptyBag(type));
        }
    }

}
