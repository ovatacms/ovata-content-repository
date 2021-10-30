/*
 * $Id: Node.java 679 2017-02-25 10:28:00Z dani $
 * Created on 14.11.2016, 12:00:00
 * 
 * Copyright (c) 2016 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.api;

import ch.ovata.cr.api.security.Policy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.JsonObject;

/**
 *
 * @author dani
 */
public interface Node extends Item {
    
    String ROOT_ID = "CAFEBABE-CAFE-BABE-CAFE-BABECAFEBABE";
    String UUID_FIELD = "uuid";
    String REVISION_FIELD = "revision";
    String STEP_FIELD = "step";
    String NODE_ID_FIELD = "@id";
    String PARENT_ID_FIELD = "@Parent";
    String NAME_FIELD = "@Name";
    String CREATE_TIME_FIELD = "@CreateTime";
    String TYPE_FIELD = "@Type";
    String REMOVED_FIELD = "@Removed";
    String POLICY_FIELD = "@Policy";
    String FULLTEXT_FIELD = "@fulltext";
    
    /**
     * Retrieves the unique id of the node in the revision history
     * @return the node id
     */
    NodeId getNodeId();
    
    /**
     * Retrieves the (implementation specific) string representation of the node id
     * @return the string representation of the id of the current node
     */
    String getId();
    
    /**
     * Retrieves the revision of this node
     * @return the revision
     */
    long getRevision();
    
    /**
     * Retrieves the timestamp this node was last modified.
     * @return Modification date
     */
    Date getLastModification();
    
    /**
     * Retrieves the session this node belongs to
     * @return the session this node belongs to
     */
    Session getSession();
    
    /**
     * Retrieves the type of the node
     * @return type of the node
     */
    String getType();
    
    /**
     * Tests if this node is of given type
     * @param type the type to check
     * @return true if of given type
     */
    boolean isOfType( String type);
    
    /**
     * Sets the type of this node
     * @param type name of the new type
     */
    void setType( String type);
            
    /**
     * Checks if this is the root node of a workspace
     * @return true if this is the root node of the workspace
     */
    boolean isRoot();
    
    /**
     * Retruns true if this node is not yet persisted
     * @return true for transient, not yet persisted nodes
     */
    boolean isNew();
    
    /**
     * Retrieves this nodes security policy
     * @return the associated security policy
     */
    Policy getPolicy();

    /**
     * Returns true if this node was modified
     * @return true for modified nodes
     */
    boolean isDirty();
    
    /**
     * Checks for the existence of child nodes
     * @return true if child nodes exist
     */
    boolean hasNodes();
    
    /**
     * Check for the existence of a child node with a given name
     * @param name name of the child node
     * @return true if the node exists
     */
    boolean hasNode( String name);
    
    /**
     * Retrieves the collection of immediate children
     * @return collection of child nodes
     */
    Collection<Node> getNodes();
    
    /**
     * Retrieves the child node with the given path
     * @param name the relative path of the child
     * @return the child node
     * @throws NotFoundException if child with given name does not exist
     */
    Node getNode( String name);
    
    /**
     * Retrieves the child node with the given path
     * @param name the relative path of the child
     * @return Optional representing the child node
     */
    Optional<Node> findNode( String name);
    
    /**
     * Adds a child node to the current node
     * @param name name of the new node
     * @param type name of the type of the new node
     * @return the newly created node
     */
    Node addNode( String name, String type);
    
    /**
     * Gets an existing node or adds the node if it does not yet exist.
     * @param name name of the node
     * @param type type used if the node is created
     * @return the node
     */
    Node getOrAddNode( String name, String type);
    
    /**
     * Move this node to another parent
     * @param parent the new parent node
     */
    void moveTo( Node parent);
    
    /**
     * Checks for the existence of properties
     * @return true if properties exist
     */
    boolean hasProperties();
    
    /**
     * Checks if a given property exists on this node
     * @param name name of the property to check
     * @return true if the property exists
     */
    boolean hasProperty( String name);
    
    /**
     * Removes all properties from this node
     */
    void clearProperties();
    
    /**
     * Retrieves the property with the given name on the current node
     * @param name name of the property
     * @return optional containing the value
     */
    Optional<Property> findProperty( String name);
    
    /**
     * Retrieves the property with the given name on the current node
     * @param name name of the property
     * @return value of the propert
     * @throws NotFoundException if the property does not exist
     */
    Property getProperty( String name);
    
    /**
     * Sets a property. If the value is null, the property is removed.
     * @param name the name of the property
     * @param value the value of the property
     * @return the new property
     */
    Property setProperty( String name, Value value);
    
    /**
     * Retrieves all properties from the node
     * @return collection of properties
     */
    Collection<Property> getProperties();
    
    /**
     * Value representing a GeoJSON value according to rfc7946
     * @param name name of the property
     * @return GeoJSON value
     * @throws NotFoundException if the property does not exist
     * @throws IllegalStateException if the type of the value does not match
     */
    JsonObject getGeoJSON( String name);
    
    /**
     * Binary representation of this value
     * @param name name of the property
     * @return binary value
     * @throws NotFoundException if the property does not exist
     * @throws IllegalStateException if the type of the value does not match
     */
    Binary getBinary( String name);
    
    /**
     * Boolean representation of this value
     * @param name name of the property
     * @return boolean value
     * @throws NotFoundException if the property does not exist
     * @throws IllegalStateException if the type of the value does not match
     */
    Boolean getBoolean( String name);
    
    /**
     * LocalDate representation of this value
     * @param name name of the property
     * @return LocalDate value
     * @throws NotFoundException if the property does not exist
     * @throws IllegalStateException if the type of the value does not match
     */
    LocalDate getDate( String name);
    
    /**
     * ZonedDateTime representation of this value
     * @param name name of the property
     * @return ZonedDateTime value
     * @throws NotFoundException if the property does not exist
     * @throws IllegalStateException if the type of the value does not match
     */
    ZonedDateTime getTime( String name);
    
    /**
     * BigDecimal representation of this value
     * @param name name of the property
     * @return BigDecimal value
     * @throws NotFoundException if the property does not exist
     * @throws IllegalStateException if the type of the value does not match
     */
    BigDecimal getDecimal( String name);
    
    /**
     * Double represenation of this value
     * @param name name of the property
     * @return Double value
     * @throws NotFoundException if the property does not exist
     * @throws IllegalStateException if the type of the value does not match
     */
    Double getDouble( String name);
    
    /**
     * Long representation of this value
     * @param name name of the property
     * @return Long value
     * @throws NotFoundException if the property does not exist
     * @throws IllegalStateException if the type of the value does not match
     */
    Long getLong( String name);
    
    /**
     * String representation of this value
     * @param name name of the property
     * @return String value
     * @throws NotFoundException if the property does not exist
     * @throws IllegalStateException if the type of the value does not match
     */
    String getString( String name);
    
    /**
     * Retrieves a reference to another node
     * @param name name of the property
     * @return the node reference
     * @throws NotFoundException if the property does not exist
     * @throws IllegalStateException if the type of the value does not match
     */
    Reference getReference( String name);
    
    /**
     * Retrieves an array of values
     * @param name name of the property
     * @return Array value
     * @throws NotFoundException if the property does not exist
     * @throws IllegalStateException if the type of the value does not match
     */
    Value[] getArray( String name);
    
    /**
     * Retrieves a map of values
     * @param name name of the property
     * @return Map value
     * @throws NotFoundException if the property does not exist
     * @throws IllegalStateException if the type of the value does not match
     */
    Map<String, Value> getMap( String name);
    
    /**
     * Locks the node for modification.
     * Locks are release on session commit or rollback.
     * @throws NodeLockedException if another user holds a lock on this node
     */
    void lock();
    
    /**
     * Administrators may break the lock
     */
    void breakLock();
    
    /**
     * Determines if the node is locked by another user
     * @return true if another user owns a lock on this node
     */
    boolean isLocked();
    
    /**
     * Retrieves information about the owner of the lock
     * @return information about the lock or empty optional if not locked
     */
    Optional<LockInfo> getLockInfo();

    static Pattern NAME_PATTERN = Pattern.compile( "[A-Za-z0-9\\@\\$][ A-Za-z0-9\\!\\@\\$\\^\\&\\(\\)\\'\\;\\{\\}\\[\\]\\=\\+\\-\\_\\~\\`\\.]*");
    
    public static void validateName( String name) {
        if( (name == null) || name.trim().isEmpty()) {
            throw new IllegalNameException( "Name may not be null or blank.");
        }

        Matcher matcher = NAME_PATTERN.matcher( name);
        
        if( !matcher.matches()) {
            throw new IllegalNameException( "Name not valid: <" + name + ">.");
        }
    }
}
