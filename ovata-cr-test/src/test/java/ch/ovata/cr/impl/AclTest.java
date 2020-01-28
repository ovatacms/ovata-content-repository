/*
 * $Id: AclTest.java 690 2017-03-02 22:03:30Z dani $
 * Created on 06.12.2016, 22:00:00
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

import ch.ovata.cr.api.security.Permission;
import ch.ovata.cr.api.security.Policy;
import ch.ovata.cr.api.security.RolePrincipal;
import ch.ovata.cr.api.security.UserPrincipal;
import ch.ovata.cr.bson.MongoDbDocument;
import ch.ovata.cr.bson.MongoDbDocumentList;
import ch.ovata.cr.spi.store.StoreDocument;
import ch.ovata.cr.spi.store.StoreDocumentList;
import java.util.Arrays;
import java.util.UUID;
import javax.security.auth.Subject;
import org.junit.Assert;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author dani
 */
public class AclTest {
    
    @Test
    public void testPolicy() {
        Subject anonymous = createAnonymous();
        Subject testUser = createUser();
        RepositoryImpl repository = mock( RepositoryImpl.class);
        SessionImpl session = mock( SessionImpl.class);
        
        when( session.getRepository()).thenReturn( repository);
        when( repository.getSubject()).thenReturn( anonymous);
        
        NodeImpl node = new NodeImpl( session, createNodeDocument(), true);
        
        Assert.assertEquals( Policy.Result.GRANTED, node.getPolicy().check( Permission.READ));
        Assert.assertEquals( Policy.Result.INDETERMINATE, node.getPolicy().check( Permission.WRITE));
        Assert.assertEquals( Policy.Result.DENIED, node.getPolicy().check( Permission.ADD));
        
        when( repository.getSubject()).thenReturn( testUser);
        NodeImpl node2 = new NodeImpl( session, createNodeDocument(), true);
        
        Assert.assertEquals( Policy.Result.INDETERMINATE, node2.getPolicy().check( Permission.READ));
    }
    
    private Subject createUser() {
        Subject subject = new Subject();
        
        subject.getPrincipals().add( new UserPrincipal( "test"));
        subject.getPrincipals().add( new RolePrincipal( "other"));
        
        return subject;
    }
    
    private Subject createAnonymous() {
        Subject subject = new Subject();
        
        subject.getPrincipals().add( new UserPrincipal( "anonymous"));
        subject.getPrincipals().add( new RolePrincipal( "everyone"));
        
        return subject;
    }
    
    private StoreDocument createNodeDocument() {
        StoreDocument rawNode = new MongoDbDocument();
        StoreDocument rawId = new MongoDbDocument();
        StoreDocument rawPolicy = new MongoDbDocument();
        
        rawId.append( "uuid", UUID.randomUUID().toString());
        rawId.append( "revision", 0l);
        
        rawNode.append( NodeImpl.NODE_ID_FIELD, rawId);
        rawNode.append( NodeImpl.POLICY_FIELD, rawPolicy);
        
        StoreDocument rawPrincipal = new MongoDbDocument();
        
        rawPrincipal.append( "type", "role");
        rawPrincipal.append( "name", "everyone");
        
        StoreDocument rawAcl = new MongoDbDocument();
        rawAcl.append( "principal", rawPrincipal);
        rawAcl.append( "is_negative", false);
        rawAcl.append( "permissions", Arrays.asList( "READ"));

        StoreDocument rawAcl2 = new MongoDbDocument();
        rawAcl2.append( "principal", rawPrincipal);
        rawAcl2.append( "is_negative", true);
        rawAcl2.append( "permissions", Arrays.asList( "ADD"));
        
        StoreDocumentList list = new MongoDbDocumentList();
        
        list.add( rawAcl);
        list.add( rawAcl2);
        
        rawPolicy.append( "acls", list);
        
        return rawNode;
    }
}
