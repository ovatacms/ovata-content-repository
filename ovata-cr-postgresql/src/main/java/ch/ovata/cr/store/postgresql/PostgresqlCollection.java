/*
 * $Id: PostgresqlCollection.java 2897 2019-12-18 12:48:09Z dani $
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
package ch.ovata.cr.store.postgresql;

import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.NodeId;
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
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class PostgresqlCollection implements StoreCollection {
    
    private static final Logger logger = LoggerFactory.getLogger( PostgresqlCollection.class);
    
    private final PostgresqlDatabase database;
    private final String name;
    
    public PostgresqlCollection( PostgresqlDatabase database, String name) {
        this.database = database;
        this.name = name;
    }
    
    @Override
    public void init( StoreDocument document) {
        String sql = SqlUtils.createStatement( "INSERT INTO %s (NODE_ID, REVISION, STEP, PARENT_ID, NAME, PAYLOAD, REMOVED) VALUES (?,?,?,?,?,?,?)", getTableName());
        
        logger.trace( "init: {}.", sql);
        
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
        Connection c = ((PostgresqlTransaction)trx).getConnection();
        String sql = SqlUtils.createStatement( "INSERT INTO %s (NODE_ID, REVISION, STEP, PARENT_ID, NAME, PAYLOAD, REMOVED) VALUES (?,?,?,?,?,?,?)", getTableName());
        
        logger.trace( "insertMany: {}.", sql);
        
        try( PreparedStatement stmt = c.prepareStatement( sql)) {
            long step = 0;
            
            for( StoreDocument doc : documents) {
                doc.getDocument( Node.NODE_ID_FIELD).append( NodeId.STEP_FIELD, step);
                populateStatement(stmt, doc);

                stmt.addBatch();
                step++;
            }

            stmt.executeBatch();
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not insert documents.", e);
        }
    }

    @Override
    public StoreDocument findById(String nodeId, long revision) {
        String sql = SqlUtils.createStatement( "SELECT PAYLOAD, REMOVED FROM %s WHERE (NODE_ID = ?) AND (REVISION <= ?) ORDER BY REVISION DESC, STEP DESC LIMIT 1", getTableName());
        
        logger.trace( "findById: {}.", sql);
        
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
        String sql = SqlUtils.createStatement( "SELECT PAYLOAD, REMOVED FROM %s WHERE (PARENT_ID = ?) AND (NAME = ?) AND (REVISION <= ?) ORDER BY REVISION DESC, STEP DESC LIMIT 1", getTableName());
        
        logger.trace( "findByParentIdAndName: {} for parent {} and name {} on revision {}.", sql, parentId, name, revision);
        
        try( Connection c = this.database.getDbConnection(); PreparedStatement stmt = c.prepareStatement( sql)) {
            stmt.setString( 1, parentId);
            stmt.setString( 2, name);
            stmt.setLong( 3, revision);

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
            throw new RepositoryException( "Could not retrieve child by name <" + parentId + "/" + name + "/" + revision + ">", e);
        }
    }

    @Override
    public StoreDocumentIterable findByParentId(String parentId, long revision) {
        String sql = String.format( "SELECT p.PAYLOAD from %s p, " +
                                    "(SELECT t.NODE_ID, i.MREV, MAX(t.STEP) as MSTEP FROM %s t, " +
                                    "(SELECT NODE_ID, MAX(REVISION) as MREV FROM %s WHERE (PARENT_ID = ?) AND (REVISION <= ?) GROUP BY NODE_ID) i " +
                                    "where t.NODE_ID = i.NODE_ID and t.revision = i.MREV GROUP BY t.node_id, i.MREV) j " +
                                    "where p.node_id = j.node_id and p.revision = j.mrev and p.step = j.mstep", 
                                    getTableName(), getTableName(), getTableName());
        
        logger.trace( "findByParentId: {}.", sql);
        
        try( Connection c = this.database.getDbConnection(); PreparedStatement stmt = c.prepareStatement( sql)) {
            stmt.setString( 1, parentId);
            stmt.setLong( 2, revision);

            try( ResultSet r = stmt.executeQuery()) {
                List<StoreDocument> result = new ArrayList();

                while( r.next()) {
                    Document doc = Document.parse( r.getString( 1));

                    result.add( new MongoDbDocument( doc));
                }

                return new PostgresqlDocumentIterable( result);
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

    private static final JsonWriterSettings JSON_SETTINGS = JsonWriterSettings.builder().outputMode(JsonMode.EXTENDED).build();
    
    private void populateStatement( PreparedStatement stmt, StoreDocument document) throws SQLException {
        stmt.setString( 1, document.getDocument( Node.NODE_ID_FIELD).getString( Node.UUID_FIELD));
        stmt.setLong( 2, document.getDocument( Node.NODE_ID_FIELD).getLong( Node.REVISION_FIELD));
        stmt.setLong( 3, document.getDocument( Node.NODE_ID_FIELD).getLong( Node.STEP_FIELD));
        stmt.setString( 4, document.getString( Node.PARENT_ID_FIELD));
        stmt.setString( 5, document.getString( Node.NAME_FIELD));
        
        PGobject jsonObject = new PGobject();
        jsonObject.setType("json");
        jsonObject.setValue(((MongoDbDocument)document).unwrap().toJson(JSON_SETTINGS));
        
        stmt.setObject( 6, jsonObject);
        
        if( document.containsKey( Node.REMOVED_FIELD)) {
            stmt.setBoolean( 7, document.getBoolean( Node.REMOVED_FIELD));
        }
        else {
            stmt.setBoolean( 7, false);
        }
    }
    
    private String getTableName() {
        return this.database.getName() + "_" + this.name;
    }
}
