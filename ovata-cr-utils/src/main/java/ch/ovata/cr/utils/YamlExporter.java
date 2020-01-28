/*
 * $Id: YamlExporter.java 2712 2019-09-28 09:40:17Z dani $
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

import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Value;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dani
 */
public class YamlExporter extends AbstractYamlBase {

    public List<Map<String, Object>> generateData(Collection<? extends Node> nodes) {
        List<Map<String, Object>> objects = new ArrayList<>( nodes.size());

        for (Node node : nodes) {
            Map<String, Object> o = new LinkedHashMap<>();

            o.put( "relpath", node.getName());
            o.put( "type", node.getType());
            
            node.getProperties().forEach( p -> o.put( p.getName(), convertToYaml( p.getValue())));

            List<Map<String, Object>> children = generateData( node.getNodes());

            if (!children.isEmpty()) {
                o.put("children", children);
            }

            objects.add(o);
        }

        return objects;
    }
    
    private Object convertToYaml( Value value) {
        switch( value.getType()) {
            case STRING:
                return value.getString();
            case LONG:
                return value.getLong();
            case DOUBLE:
                return value.getDouble();
            case REFERENCE:
                return REFERENCE_PREFIX + value.getReference().getWorkspace() + ":" + value.getReference().getPath();
            case DECIMAL:
                return DECIMAL_PREFIX + value.getDecimal().toPlainString();
            case LOCAL_DATE:
                return LOCAL_DATE_PREFIX + DateTimeFormatter.ISO_LOCAL_DATE.format( value.getDate());
            case ZONED_DATETIME:
                return ZONED_DATETIME_PREFIX + DateTimeFormatter.ISO_DATE_TIME.format( value.getTime());
            case BOOLEAN:
                return value.getBoolean();
            case ARRAY:
                Value[] values = value.getArray();
                Object[] yamlValues = new Object[ values.length];
                
                for( int i = 0; i < values.length; i++) {
                    yamlValues[i] = convertToYaml( values[i]);
                }
                
                return yamlValues;
        }
        
        return null;
    }
}
