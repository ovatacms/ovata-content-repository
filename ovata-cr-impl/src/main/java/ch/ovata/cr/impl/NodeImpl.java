/*
 * $Id: NodeImpl.java 679 2017-02-25 10:28:00Z dani $
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
package ch.ovata.cr.impl;

import ch.ovata.cr.api.NodeId;
import ch.ovata.cr.api.Binary;
import ch.ovata.cr.api.LockInfo;
import ch.ovata.cr.impl.values.AbstractValue;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.NotAuthorizedException;
import ch.ovata.cr.api.NotFoundException;
import ch.ovata.cr.api.Property;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.api.Reference;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.Roles;
import ch.ovata.cr.api.SameNameSiblingException;
import ch.ovata.cr.api.security.Acl;
import ch.ovata.cr.api.security.Permission;
import ch.ovata.cr.api.security.Policy;
import ch.ovata.cr.api.security.RolePrincipal;
import ch.ovata.cr.api.security.UserPrincipal;
import ch.ovata.cr.impl.values.ValueFactoryImpl;
import ch.ovata.cr.spi.store.StoreDocument;
import ch.ovata.cr.spi.store.StoreDocumentList;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import javax.security.auth.Subject;

/**
 *
 * @author dani
 */
public class NodeImpl implements Node {

    public static final String PATH_DELIMITER = "/";
    
    public static final String VALUE_TYPE_FIELD = "@ValueType";
    public static final String VALUE_FIELD = "@Value";
    
    private final SessionImpl session;
    private final NodeId nodeId;
    private final Policy accessPolicy;
    private final boolean isNew;
    private final Map<String, PropertyImpl> properties = new HashMap<>();
    private boolean isDirty;
    private StoreDocument document;
    
    public NodeImpl( SessionImpl session, StoreDocument document, boolean isNew) {
        this.session = session;
        this.document = document;
        this.nodeId = new NodeId( document.getDocument(NODE_ID_FIELD));
        this.isNew = isNew;
        this.isDirty = isNew;
        this.document.entrySet().stream()
                                    .filter(e -> !e.getKey().startsWith( "@") && !NODE_ID_FIELD.equals( e.getKey()))
                                    .forEach( e -> properties.put( e.getKey(), new PropertyImpl( this, e.getKey())));
        this.accessPolicy = new PolicyImpl();
    }
    
    @Override
    public Session getSession() {
        return this.session;
    }
    
    @Override
    public NodeId getNodeId() {
        return this.nodeId;
    }

    @Override
    public String getId() {
        return this.nodeId.getUUID();
    }
    
    @Override
    public long getRevision() {
        return this.nodeId.getRevision();
    }
    
    @Override
    public Date getLastModification() {
        return document.getDate( Node.CREATE_TIME_FIELD);
    }
    
    @Override
    public String getName() {
        return document.getString( NAME_FIELD);
    }
    
    @Override
    public void rename( String name) {
        Node.validateName( name);
        checkPermission( Permission.ADD);
        
        if( !name.equals( this.getName())) {
            checkSameNameSibling( this.getParent(), name);
        
            copyOnWrite();
            markDirty();

            session.renameNode( this, name);
        }
    }

    @Override
    public String getType() {
        return document.getString( TYPE_FIELD);
    }

    @Override
    public boolean isOfType( String type) {
        return getType().equals( type);
    }
    
    @Override
    public void setType( String type) {
        checkPermission( Permission.WRITE);
        
        copyOnWrite();
        
        document.append( TYPE_FIELD, type);
        
        markDirty();
    }

    @Override
    public String getPath() {
        String path = getParent().getPath();
        
        if( path.endsWith( PATH_DELIMITER) ) {
            return path + this.getName();
        }
        else {
            return path + PATH_DELIMITER + this.getName();
        }
    }

    @Override
    public int getDepth() {
        return getParent().getDepth() + 1;
    }

    @Override
    public boolean isRoot() {
        return false;
    }
    
    public String getParentId() {
        return document.getString( PARENT_ID_FIELD);
    }
    
    @Override
    public Node getParent() {
        return session.getNodeByIdentifier( getParentId());
    }
    
    @Override
    public boolean hasNodes() {
        return !getNodes().isEmpty();
    }
    
    @Override
    public Collection<Node> getNodes() {
        return session.getChildren( this);
    }

    @Override
    public Node getNode(String name) {
        return this.findNode( name).orElseThrow( () -> new NotFoundException( "Could not find child node for name <" + name + ">."));
    }
    
    @Override
    public Optional<Node> findNode( String name) {
        return session.getChildByName( this, name);
    }
    
    @Override
    public boolean hasNode( String name) {
        return this.findNode( name).isPresent();
    }

    @Override
    public Node addNode(String name, String type) {
        Node.validateName( name);
        this.checkPermission( Permission.ADD);
        
        checkSameNameSibling( this, name);
        
        return session.addNode( this, name, type);
    }

    @Override
    public void moveTo( Node parent) {
        ((NodeImpl)this.getParent()).checkPermission( Permission.REMOVE);
        ((NodeImpl)parent).checkPermission( Permission.ADD);
        
        if( parent.getId().equals( this.getId())) {
            throw new RepositoryException( "Parent must be diffrent from current node.");
        }
        
        if( parent.hasNode( this.getName())) {
            throw new RepositoryException( "Same name siblings are not supported.");
        }
        
        copyOnWrite();
        markDirty();
        
        session.moveNode( this, parent);
    }
    
    void setParent( NodeImpl parent) {
        this.document.append( Node.PARENT_ID_FIELD, parent.getId());
    }
    
    void setName( String name) {
        this.document.append( Node.NAME_FIELD, name);
    }
    
    @Override
    public void remove() {
        ((NodeImpl)this.getParent()).checkPermission( Permission.REMOVE);

        this.getNodes().forEach( Node::remove);
        
        session.markNodeRemoved( this);
    }
    
    @Override
    public boolean hasProperties() {
        return !this.properties.isEmpty();
    }
    
    @Override
    public boolean hasProperty( String name) {
        return this.properties.containsKey( name);
    }
    
    @Override
    public void clearProperties() {
        checkPermission( Permission.WRITE);
        
        List<Property> props = new ArrayList<>( this.properties.values());
        
        props.stream().forEach( Property::remove);
    }
    
    @Override
    public Optional<Property> findProperty(String name) {
        return Optional.ofNullable( this.properties.get( name));
    }

    @Override
    public Property getProperty(String name) {
        return findProperty( name).orElseThrow( () -> new NotFoundException( "Could not find property of name <" + name + ">."));
    }
    
    @Override
    public Property setProperty( String name, Value value) {
        Node.validateName( name);
        this.checkPermission( Permission.WRITE);
        
        if( value == null) {
            throw new NullPointerException( "Value may not be null.");
        }
        
        copyOnWrite();
        
        PropertyImpl p = this.properties.computeIfAbsent( name, k -> new PropertyImpl( this, k));
        
        this.document.append( name, ((AbstractValue)value).toDocument());
        
        markDirty();
        
        return p;
    }
    
    @Override
    public Collection<Property> getProperties() {
        return Collections.unmodifiableCollection( this.properties.values());
    }
    
    StoreDocument getDocument() {
        return this.document;
    }

    void renameProperty(PropertyImpl property, String name) {
        Node.validateName( name);
        this.checkPermission( Permission.WRITE);
        
        copyOnWrite();
        
        StoreDocument temp = this.document.getDocument(property.getName());
        this.document.remove( property.getName());
        this.document.append( name, temp);
        
        this.properties.remove( property.getName());
        this.properties.put( name, property);
        
        property.setName( name);
        
        markDirty();
    }

    void removeProperty(PropertyImpl property) {
        this.checkPermission( Permission.WRITE);
        
        copyOnWrite();
        
        this.document.remove( property.getName());
        this.properties.remove( property.getName());
        
        markDirty();
    }
    
    private void markDirty() {
        if( !isNew) {
            this.session.markNodeDirty( this);
            isDirty = true;
        }
    }
    
    private void copyOnWrite() {
        if( !isNew && !isDirty) {
            this.document = this.document.cloneDocument();
        }
    }
    
    @Override
    public boolean isDirty() {
        return isDirty;
    }
    
    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public void accept( Visitor visitor) {
        visitor.visit( this);
    }

    @Override
    public JsonObject getGeoJSON( String name) {
        return null;
    }
    
    @Override
    public Binary getBinary( String name) {
        return this.getProperty( name).getValue().getBinary();
    }
    
    @Override
    public Boolean getBoolean( String name) {
        return this.getProperty( name).getValue().getBoolean();
    }
    
    @Override
    public LocalDate getDate( String name) {
        return this.getProperty( name).getValue().getDate();
    }
    
    @Override
    public ZonedDateTime getTime( String name) {
        return this.getProperty( name).getValue().getTime();
    }
    
    @Override
    public BigDecimal getDecimal( String name) {
        return this.getProperty( name).getValue().getDecimal();
    }
    
    @Override
    public Double getDouble( String name) {
        return this.getProperty( name).getValue().getDouble();
    }
    
    @Override
    public Long getLong( String name) {
        return this.getProperty( name).getValue().getLong();
    }
    
    @Override
    public String getString( String name) {
        return this.getProperty( name).getValue().getString();
    }
    
    @Override
    public Reference getReference( String name) {
        return this.getProperty( name).getValue().getReference();
    }
    
    @Override
    public Value[] getArray( String name) {
        return this.getProperty( name).getValue().getArray();
    }
    
    @Override
    public Map<String, Value> getMap( String name) {
        return this.getProperty( name).getValue().getMap();
    }

    void updateValue( PropertyImpl property, AbstractValue value) {
        if( value == null) {
            this.remove();
        }
        else {
            copyOnWrite();

            this.document.append( property.getName(), value.toDocument());

            markDirty();
        }
    }
    
    Value getValue( Property property) {
        StoreDocument value = this.document.getDocument(property.getName());
        
        if( value != null) {
            return ((ValueFactoryImpl)this.getSession().getValueFactory()).of( value);
        }
        else {
            return null;
        }
    }
    
    @Override
    public Policy getPolicy() {
        return this.accessPolicy;
    }
    
    void checkPermission( Permission permission) {
        if( !hasPermission( permission)) {
            throw new NotAuthorizedException( "Not authorized to " + permission + ".");
        }
    }
    
    protected boolean hasPermission( Permission permission) {
        try {
            Policy.Result result = this.accessPolicy.check( permission);

            if( result == Policy.Result.INDETERMINATE) {
                return ((NodeImpl)this.getParent()).hasPermission( permission);
            }

            return (result == Policy.Result.GRANTED);
        }
        catch( NotFoundException e) {
            return false;
        }
    }

    @Override
    public Node getOrAddNode(String name, String type) {
        return this.findNode( name).orElseGet( () -> this.addNode( name, type));
    }

    @Override
    public void lock() {
        this.session.lockNode( this);
    }

    @Override
    public void breakLock() {
        this.session.breakLock( this);
    }

    @Override
    public boolean isLocked() {
        return session.isNodeLocked( this);
    }

    @Override
    public Optional<LockInfo> getLockInfo() {
        return session.getLockInfo( this);
    }
    
    public class PolicyImpl implements Policy {

        public static final String PROP_ACLS = "acls";
        
        private final List<AclImpl> acls = new ArrayList<>();
        private final Map<Permission, Policy.Result> results = new EnumMap<>(Permission.class);
        
        public PolicyImpl() {
            StoreDocument policy = NodeImpl.this.document.getDocument( Node.POLICY_FIELD);
            
            if( policy != null) {
                StoreDocumentList rawAcls = policy.getDocumentList( PROP_ACLS);
                
                this.acls.addAll(rawAcls.stream().map( AclImpl::new).collect( Collectors.toList()));
            }
        }
        
        @Override
        public List<Acl> getAcls() {
            return (List)this.acls;
        }

        @Override
        public Policy.Result check( Permission permission) {
            Policy.Result result = results.get( permission);
            
            if(result != null) {
                return result;
            }
            
            result = check( NodeImpl.this.session.getRepository().getSubject(), permission);
            
            results.put( permission, result);
            
            return result;
        }
        
        private Policy.Result check( Subject subject, Permission permission) {
            Policy.Result result = Policy.Result.INDETERMINATE;
            
            for( Principal p : subject.getPrincipals()) {
                if( (p instanceof RolePrincipal) && (p.getName().equals( Roles.ADMINISTRATOR) || p.getName().equals( "administrators"))) {
                    return Policy.Result.GRANTED;
                }
                
                for( Acl acl : this.acls) {
                    if( p.equals( acl.getPrincipal()) && acl.getPermissions().contains( permission)) {
                        result = acl.isNegative() ? Policy.Result.DENIED : Policy.Result.GRANTED;
                    }
                }
            }
            
            return result;
        }

        @Override
        public void updateAcls( List<Acl> acls) {
            NodeImpl.this.checkPermission( Permission.MODIFY_ACL);

            this.acls.clear();
            
            for( Acl acl : acls) {
                AclImpl newAcl = new AclImpl( acl.getPrincipal(), acl.isNegative(), acl.getPermissions());

                Iterator<AclImpl> i = this.acls.listIterator();

                while( i.hasNext()) {
                    Acl eacl = i.next();
                    if( eacl.getPrincipal().equals( newAcl.getPrincipal()) && ( eacl.isNegative() == newAcl.isNegative())) {
                        i.remove();
                    }
                }

                this.acls.add( newAcl);
            }
            
            updatePolicy();
        }
        
        private void updatePolicy() {
            copyOnWrite();
            
            StoreDocument rawPolicy = NodeImpl.this.session.getValueFactory().newDocument();
            StoreDocumentList list = NodeImpl.this.session.getValueFactory().newList();
            
            this.acls.stream().map( AclImpl::toDocument).forEach( list::add);
            
            rawPolicy.append( PolicyImpl.PROP_ACLS, list);
            
            NodeImpl.this.document.append( Node.POLICY_FIELD, rawPolicy);
            
            NodeImpl.this.markDirty();
        }
    
        public class AclImpl implements Acl {

            public static final String PROP_PRINCIPAL = "principal";
            public static final String PROP_PERMISSIONS = "permissions";
            public static final String PROP_IS_NEGATIVE = "is_negative";
            public static final String PROP_TYPE = "type";
            public static final String PROP_NAME = "name";
            public static final String TYPE_USER = "user";
            public static final String TYPE_ROLE = "role";
            
            private final Principal principal;
            private final boolean isNegative;
            private final Set<Permission> permissions = EnumSet.noneOf( Permission.class);

            public AclImpl( Principal p, boolean isNegative, Collection<Permission> permissions) {
                this.principal = p;
                this.isNegative = isNegative;
                this.permissions.addAll( permissions);
            }

            public AclImpl( StoreDocument document) {
                StoreDocument rawPrincipal = document.getDocument( PROP_PRINCIPAL);
                List<String> rawPermissions = (List<String>)document.getList( PROP_PERMISSIONS);

                this.principal = createPrincipal( rawPrincipal);
                this.isNegative = document.getBoolean( PROP_IS_NEGATIVE);
                this.permissions.addAll( rawPermissions.stream().map( Permission::valueOf).collect( Collectors.toSet()));
            }

            private Principal createPrincipal( StoreDocument principal) {
                if( TYPE_USER.equals( principal.getString( PROP_TYPE))) {
                    return new UserPrincipal( principal.getString( PROP_NAME));
                }
                else {
                    return new RolePrincipal( principal.getString( PROP_NAME));
                }
            }

            @Override
            public Principal getPrincipal() {
                return this.principal;
            }

            @Override
            public boolean isNegative() {
                return this.isNegative;
            }

            @Override
            public Set<Permission> getPermissions() {
                return this.permissions;
            }

            private StoreDocument toDocument() {
                StoreDocument rawAcl = NodeImpl.this.getSession().getValueFactory().newDocument();
                StoreDocument rawPrincipal = NodeImpl.this.getSession().getValueFactory().newDocument();

                rawPrincipal.append( PROP_TYPE, (this.principal instanceof UserPrincipal) ? TYPE_USER : TYPE_ROLE);
                rawPrincipal.append( PROP_NAME, this.principal.getName());

                List<String> rawPermissions = this.permissions.stream().map( Permission::name).collect( Collectors.toList());
                
                rawAcl.append( PROP_PRINCIPAL, rawPrincipal)
                      .append( PROP_PERMISSIONS, rawPermissions)
                      .append( PROP_IS_NEGATIVE, this.isNegative);

                return rawAcl;
            }
        }
    }

    private void checkSameNameSibling( Node node, String name) {
        if( node.hasNode( name)) {
            throw new SameNameSiblingException( "Same name siblings are not supported.");
        }
    }
    
    @Override
    public boolean equals( Object other) {
        if( other instanceof NodeImpl) {
            return this.nodeId.equals( ((NodeImpl)other).nodeId);
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.nodeId.hashCode();
    }
}
