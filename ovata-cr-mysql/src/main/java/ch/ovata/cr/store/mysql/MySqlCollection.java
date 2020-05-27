/*
 * $Id: MySqlCollection.java 697 2017-03-06 18:12:59Z dani $
 * Created on 22.01.2018, 16:00:00
 * 
 * Copyright (c) 2018 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.store.mysql;

import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.spi.store.StoreCollection;
import ch.ovata.cr.spi.store.StoreDocument;
import ch.ovata.cr.spi.store.StoreDocumentIterable;
import ch.ovata.cr.spi.store.StoreDocumentList;
import ch.ovata.cr.bson.MongoDbDocument;
import ch.ovata.cr.bson.MongoDbDocumentList;
import ch.ovata.cr.spi.store.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bson.Document;

/**
 *
 * @author dani
 */
public class MySqlCollection implements StoreCollection {
    
    private final MySqlDatabase database;
    private final String name;
    
    public MySqlCollection( MySqlDatabase database, String name) {
        this.database = database;
        this.name = name;
    }
    
    @Override
    public void init( StoreDocument document) {
        String sql = SqlUtils.createStatement( "INSERT INTO `%s` (NODE_ID, REVISION, PARENT_ID, NAME, PAYLOAD, REMOVED) VALUES (?,?,?,?,?,?)", this.getTableName());

        try( Connection c = this.database.getDbConnection(); PreparedStatement stmt = c.prepareStatement( sql)) {
            populateStatement(stmt, document);

            stmt.execute();
            
            c.commit();
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not insert document.", e);
        }
    }

    @Override
    public void insertMany( Transaction trx, Collection<StoreDocument> documents) {
        Connection c = ((MySqlTransaction)trx).getConnection();
        String sql = SqlUtils.createStatement( "INSERT INTO `%s` (NODE_ID, REVISION, PARENT_ID, NAME, PAYLOAD, REMOVED) VALUES (?,?,?,?,?,?)", this.getTableName());
        
        try( PreparedStatement stmt = c.prepareStatement( sql)) {
            for( StoreDocument doc : documents) {
                populateStatement( stmt, doc);

                stmt.addBatch();
            }

            stmt.executeBatch();
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not insert documents.", e);
        }
    }

    @Override
    public StoreDocument findById(String nodeId, long revision) {
        String sql = SqlUtils.createStatement( "SELECT PAYLOAD, REMOVED FROM `%s` WHERE (NODE_ID = ?) AND (REVISION <= ?) ORDER BY REVISION DESC, REMOVED ASC LIMIT 1", this.getTableName());
        
        try( Connection c = this.database.getDbConnection(); PreparedStatement stmt = c.prepareStatement( sql)) {
            stmt.setString( 1, nodeId);
            stmt.setLong( 2, revision);

            try( ResultSet r = stmt.executeQuery()) {
                if( r.next() && !r.getBoolean( 2)) {
                    Document doc = Document.parse( r.getString( 1));

                    return new MongoDbDocument( doc);
                }
                else {
                    return null;
                }
            }
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not retrieve node <" + nodeId + " / " + revision + ">.", e);
        }
    }

    @Override
    public StoreDocument findByParentIdAndName(String parentId, String name, long revision) {
        String sql = SqlUtils.createStatement( "SELECT PAYLOAD FROM `%s` WHERE (PARENT_ID = ?) AND (NAME = ?) AND (REVISION <= ?) ORDER BY REVISION DESC, REMOVED ASC", this.getTableName());
        
        try( Connection c = this.database.getDbConnection(); PreparedStatement stmt = c.prepareStatement( sql)) {
            stmt.setString( 1, parentId);
            stmt.setString( 2, name);
            stmt.setLong( 3, revision);

            try( ResultSet r = stmt.executeQuery()) {
                if( r.next()) {
                    Document doc = Document.parse( r.getString( 1));

                    return new MongoDbDocument( doc);
                }
                else {
                    return null;
                }
            }
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not retrieve child by name <" + parentId + "/" + name + "/" + revision + ">", e);
        }
    }

    @Override
    public StoreDocumentIterable findByParentId(String parentId, long revision) {
        String sql = SqlUtils.createStatement( "SELECT PAYLOAD FROM `%s` WHERE (PARENT_ID = ?) AND (REVISION <= ?) ORDER BY NODE_ID ASC, REVISION DESC", this.getTableName());
        
        try( Connection c = this.database.getDbConnection(); PreparedStatement stmt = c.prepareStatement( sql)) {
            stmt.setString( 1, parentId);
            stmt.setLong( 2, revision);

            try( ResultSet r = stmt.executeQuery()) {
                List<StoreDocument> result = new ArrayList();

                while( r.next()) {
                    Document doc = Document.parse( r.getString( 1));

                    result.add( new MongoDbDocument( doc));
                }

                return new MySqlDocumentIterable( result);
            }
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not retrieve child nodes <" + parentId + "/" + revision + ">", e);
        }
    }

    @Override
    public StoreDocument newDocument() {
        return new MongoDbDocument();
    }

    @Override
    public StoreDocumentList newList() {
        return new MongoDbDocumentList();
    }

    private void populateStatement(  PreparedStatement stmt, StoreDocument document) throws SQLException {
        stmt.setString( 1, document.getDocument( Node.NODE_ID_FIELD).getString( Node.UUID_FIELD));
        stmt.setLong( 2, document.getDocument( Node.NODE_ID_FIELD).getLong( Node.REVISION_FIELD));
        stmt.setString( 3, document.getString( Node.PARENT_ID_FIELD));
        stmt.setString( 4, document.getString( Node.NAME_FIELD));
        stmt.setString( 5, ((MongoDbDocument)document).unwrap().toJson());
        
        if( document.containsKey( Node.REMOVED_FIELD)) {
            stmt.setBoolean( 6, document.getBoolean( Node.REMOVED_FIELD));
        }
        else {
            stmt.setBoolean( 6, false);
        }
    }
    
    private String getTableName() {
        return this.database.getName() + "_" + this.name;
    }
}
