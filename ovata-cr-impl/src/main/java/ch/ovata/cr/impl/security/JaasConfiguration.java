/*
 * $Id: JaasConfiguration.java 692 2017-03-06 09:45:19Z dani $
 * Created on 28.11.2016, 12:00:00
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
package ch.ovata.cr.impl.security;

import ch.ovata.cr.api.CoreNodeTypes;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Property;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.api.Workspace;
import ch.ovata.cr.api.util.PropertyComparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;

/**
 *
 * @author dani
 */
public class JaasConfiguration extends Configuration {
    
    private static final String JAAS_CONFIG_ROOT_PATH = "/security/jaas";
    
    private final List<AppConfigurationEntry> entries;
    
    public JaasConfiguration( Repository repository) {
        Session session = repository.getSession( Workspace.WORKSPACE_CONFIGURATION);
        Node jaasConfig = session.getNodeByPath( JAAS_CONFIG_ROOT_PATH);
        
        this.entries = jaasConfig.getNodes().stream()
                .filter( n -> n.isOfType( CoreNodeTypes.JAAS_ENTRY))
                .sorted( PropertyComparator.ByOrder)
                .map( this::createConfigEntry)
                .collect( Collectors.toList());
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        return this.entries.toArray( new AppConfigurationEntry[entries.size()]);
    }
    
    private AppConfigurationEntry createConfigEntry( Node node) {
        String loginModuleName = node.getProperty( "name").getValue().getString();
        LoginModuleControlFlag flag = translateFlag( node.getProperty( "flag").getValue().getString());
        Map<String, Object> options = retrieveOptions( node);
        
        options.put( "repository", node.getSession().getRepository());
        
        return new AppConfigurationEntry( loginModuleName, flag, options);
    }
    
    private Map<String, Object> retrieveOptions( Node node) {
        Map<String, Object> options = new HashMap<>();
        
        for( Property p : node.getProperties()) {
            if( "name".equals(  p.getName()) || "flag".equals( p.getName())) {
                continue;
            }
            
            Value v = p.getValue();
            
            switch( v.getType()) {
                case STRING:
                    options.put( p.getName(), v.getString());
                    break;
                case LONG:
                    options.put( p.getName(), v.getLong());
                    break;
                case DOUBLE:
                    options.put( p.getName(), v.getDouble());
                    break;
            }
        }
        
        return options;
    }
    
    private LoginModuleControlFlag translateFlag( String flagName) {
        switch( flagName) {
            case "SUFFICIENT":
                return LoginModuleControlFlag.SUFFICIENT;
            case "REQUISITE":
                return LoginModuleControlFlag.REQUISITE;
            case "REQUIRED":
                return LoginModuleControlFlag.REQUIRED;
            case "OPTIONAL":
                return LoginModuleControlFlag.OPTIONAL;
            default:
                return LoginModuleControlFlag.REQUIRED;
        }
    }
}
