/*
 * $Id: YamlImporter.java 2712 2019-09-28 09:40:17Z dani $
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
package ch.ovata.cr.utils;

import ch.ovata.cr.api.CoreNodeTypes;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.api.ValueFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author dani
 */
public class YamlImporter extends AbstractYamlBase {
    
    private static final Logger logger = LoggerFactory.getLogger( YamlImporter.class);
    
    public void importFile( Node node, File file) throws IOException {
        importStream( node, new FileInputStream( file));
    }
    
    public void importStream( Node node, InputStream in) {
        Object list = new Yaml().load( in);
        
        importObject( node, (List<Map<String, Object>>)list);
    }
    
    public void importObject( Node node, List<Map<String, Object>> objects) {
        ValueFactory vf = node.getSession().getValueFactory();
        
        if (objects != null) {
            for (Map<String, Object> o : objects) {
                String relpath = (String) o.get("relpath");
                String type = (String) o.get("type");
                List<Map<String, Object>> children = (List<Map<String, Object>>) o.get("children");

                Node child = node.addNode(relpath, (type == null) ? CoreNodeTypes.UNSTRUCTURED : type);

                for (Map.Entry<String, Object> entry : o.entrySet()) {
                    if (!"relpath".equals(entry.getKey()) && !"children".equals(entry.getKey()) && !"type".equals(entry.getKey())) {
                        Value value = createValue( vf, entry.getValue());
                        
                        if( value != null) {
                            child.setProperty( entry.getKey(), value);
                        }
                    }
                }

                importObject(child, children);
            }
        }
    }
    
    private Value createValue( ValueFactory vf, Object value) {
        if(value instanceof String) {
            String svalue = (String)value;
            
            if( svalue.startsWith( REFERENCE_PREFIX)) {
                String ref = svalue.substring( REFERENCE_PREFIX.length());
                int index = ref.indexOf( ':');
                
                if( index != -1) {
                    return vf.referenceByPath( ref.substring( 0, index), ref.substring( index +1));
                }
                else {
                    logger.warn( "Could not parse reference <{}>.", value);
                    return null;
                }
            }
            else if( svalue.startsWith( DECIMAL_PREFIX)) {
                String decimal = svalue.substring( DECIMAL_PREFIX.length());
                
                return vf.of( new BigDecimal( decimal));
            }
            else if( svalue.startsWith( LOCAL_DATE_PREFIX)) {
                String ldate = svalue.substring( LOCAL_DATE_PREFIX.length());
                LocalDate date = LocalDate.parse( ldate, DateTimeFormatter.ISO_LOCAL_DATE);

                return vf.of( date);
            }
            else if( svalue.startsWith( ZONED_DATETIME_PREFIX)) {
                String zdate = svalue.substring( ZONED_DATETIME_PREFIX.length());
                ZonedDateTime date = ZonedDateTime.parse( zdate, DateTimeFormatter.ISO_DATE_TIME);
                
                return vf.of( date);
            }
            else {
                return vf.of( svalue);
            }
        } 
        else if (value instanceof Long || value instanceof Integer) {
            return vf.of( ((Number)value).longValue());
        }
        else if (value instanceof Double) {
            return vf.of( ((Double)value));
        }
        else if( value instanceof Boolean) {
            return vf.of( (Boolean)value);
        }
        else if( value instanceof List) {
            List objects = (List)value;
            List<Value> values = new ArrayList<>( objects.size());
            
            for( Object o : objects) {
                values.add( createValue( vf, o));
            }
            
            return vf.of( values);
        }
        
        return null;
    }
}
