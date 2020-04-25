/*
 * $Id: JsonImporter.java 2859 2019-12-02 13:34:08Z dani $
 * Created on 05.07.2019, 12:00:00
 * 
 * Copyright (c) 2019 by Ovata GmbH,
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
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.api.ValueFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.json.Json;
import javax.json.stream.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class JsonImporter {
    
    private static final Logger logger = LoggerFactory.getLogger( JsonImporter.class);
    
    public void importStream( InputStream in, Node node) {
        importJson( Json.createParser( in), node);
    }

    private Importer importer = null;
    
    public void importJson( JsonParser reader, Node node) {
        while( reader.hasNext()) {
            JsonParser.Event event = reader.next();
            
            switch( event) {
                case START_ARRAY:
                    importer = importer.startArray(reader);
                    break;
                case START_OBJECT:
                    if( importer == null) {
                        importer = new NodeImporter( new ImporterContext( node), node, null);
                    }
                    else {
                        importer = importer.startObject(reader);
                    }
                    break;
                case END_ARRAY:
                    importer = importer.endArray(reader);
                    break;
                case END_OBJECT:
                    importer = importer.endObject(reader);
                    break;
                case KEY_NAME:
                    importer = importer.keyName(reader);
                    break;
                case VALUE_FALSE:
                    importer = importer.valueFalse(reader);
                    break;
                case VALUE_TRUE:
                    importer = importer.valueTrue(reader);
                    break;
                case VALUE_NULL:
                    importer = importer.valueNull(reader);
                    break;
                case VALUE_NUMBER:
                    importer = importer.valueNumber(reader);
                    break;
                case VALUE_STRING:
                    importer = importer.valueString(reader);
                    break;
            }
        }
    }
    
    private static interface Importer {
        Importer startObject( JsonParser reader);
        Importer endObject( JsonParser reader);
        Importer startArray( JsonParser reader);
        Importer endArray( JsonParser reader);
        Importer keyName( JsonParser reader);
        Importer valueFalse( JsonParser reader);
        Importer valueTrue( JsonParser reader);
        Importer valueNull( JsonParser reader);
        Importer valueNumber( JsonParser reader);
        Importer valueString( JsonParser reader);
    }
    
    private static class ImporterContext {
        
        private final Node rootNode;
        
        public ImporterContext( Node rootNode) {
            this.rootNode = rootNode;
        }
        
        public Node getRootNode() {
            return this.rootNode;
        }
        
        public Session getSession() {
            return this.rootNode.getSession();
        }
        
        public ValueFactory getValueFactory() {
            return this.rootNode.getSession().getValueFactory();
        }
    }
    
    private abstract static class AbstractImporter implements Importer {

        protected final ImporterContext context;
        protected final Importer parent;
        
        protected AbstractImporter( ImporterContext context, Importer parent) {
            this.context = context;
            this.parent = parent;
        }
        
        @Override
        public Importer startObject(JsonParser reader) {
            throw new IllegalImporterStateException( "Unexpected start of object.", reader);
        }

        @Override
        public Importer endObject(JsonParser reader) {
            throw new IllegalImporterStateException( "Unexpected end of object.", reader);
        }

        @Override
        public Importer startArray(JsonParser reader) {
            throw new IllegalImporterStateException( "Unexpected start of array.", reader);
        }

        @Override
        public Importer endArray(JsonParser reader) {
            throw new IllegalImporterStateException( "Unexpected end of array.", reader);
        }

        @Override
        public Importer keyName(JsonParser reader) {
            throw new IllegalImporterStateException( "Unexpected key name.", reader);
        }

        @Override
        public Importer valueFalse(JsonParser reader) {
            throw new IllegalImporterStateException( "Unexpected false value.", reader);
        }

        @Override
        public Importer valueTrue(JsonParser reader) {
            throw new IllegalImporterStateException( "Unexpected true value.", reader);
        }

        @Override
        public Importer valueNull(JsonParser reader) {
            throw new IllegalImporterStateException( "Unexpected null value.", reader);
        }

        @Override
        public Importer valueNumber(JsonParser reader) {
            throw new IllegalImporterStateException( "Unexpected number value.", reader);
        }

        @Override
        public Importer valueString(JsonParser reader) {
            throw new IllegalImporterStateException( "Unexpected string value.", reader);
        }
    }
    
    private static class NodeImporter extends AbstractImporter {

        private final Node node;
        
        public NodeImporter( ImporterContext context, Node node, Importer parent) {
            super( context, parent);

            this.node = node;
        }
        
        @Override
        public Importer startObject( JsonParser reader) {
            return this;
        }
        
        @Override
        public Importer keyName(JsonParser reader) {
            String v = reader.getString();
            
            switch( v) {
                case "@type":
                    return new NodeTypeImporter( this.context, this.node, this);
                case "properties":
                    return new PropertiesImporter( this.context, this.node, this);
                case "nodes":
                    return new NodesImporter( this.context, this.node, this);
                default:
                    throw new IllegalImporterStateException( "Unexpected property <" + v + "> on node.", reader);
            }
        }

        @Override
        public Importer endObject(JsonParser reader) {
            return this.parent;
        }
    }
    
    private static class NodeTypeImporter extends AbstractImporter {
        
        private final Node node;
        
        public NodeTypeImporter( ImporterContext context, Node node, Importer parent) {
            super( context, parent);

            this.node = node;
        }
        
        @Override
        public Importer valueString( JsonParser reader) {
            this.node.setType( reader.getString());
  
            return this.parent;
        }
    }
    
    private static class NodesImporter extends AbstractImporter {
        
        private final Node node;
        
        public NodesImporter( ImporterContext context, Node node, Importer parent) {
            super( context, parent);

            this.node = node;
        }
        
        @Override
        public Importer startObject(JsonParser reader) {
            return this;
        }

        @Override        
        public Importer keyName( JsonParser reader) {
            String name = reader.getString();
            
            return new NodeImporter( this.context, this.node.getOrAddNode( name, CoreNodeTypes.UNSTRUCTURED), this);
        }

        @Override
        public Importer endObject(JsonParser reader) {
            return parent;
        }
    }
    
    private static class PropertiesImporter extends AbstractImporter {

        private final Node node;
        
        public PropertiesImporter( ImporterContext context, Node node, Importer parent) {
            super( context, parent);

            this.node = node;
        }
        
        @Override
        public Importer startObject( JsonParser reader) {
            return this;
        }
        
        @Override
        public Importer keyName(JsonParser reader) {
            return new PropertyImporter( this.context, node, reader.getString(), this);
        }

        @Override
        public Importer endObject(JsonParser reader) {
            return parent;
        }
    }
    
    private static class TypeImporter extends AbstractImporter {
        
        private Value.Type type;
        
        public TypeImporter( ImporterContext context, Importer parent) {
            super( context, parent);
        }
        
        public Value.Type getType() {
            return this.type;
        }
        
        @Override
        public Importer valueString( JsonParser reader) {
            this.type = Value.Type.valueOf( reader.getString());

            return parent;
        }
    }
    
    private abstract static class ValueImporter extends AbstractImporter {
        
        protected ValueImporter( ImporterContext context, Importer parent) {
            super( context, parent);
        }
        
        public abstract Value getValue();
    }
    
    private static class StringValueImporter extends ValueImporter {
        protected String value;
        
        public StringValueImporter( ImporterContext context, Importer parent) {
            super( context, parent);
        }
        
        @Override
        public Importer valueString( JsonParser reader) {
            this.value = reader.getString();
            
            return parent;
        }
        
        @Override
        public Value getValue() {
            return this.context.getValueFactory().of( this.value);
        }
    }
    
    private static class LongValueImporter extends ValueImporter {
        
        private BigDecimal value;
        
        public LongValueImporter( ImporterContext context, Importer parent) {
            super( context, parent);
        }
        
        @Override
        public Importer valueNumber( JsonParser reader) {
            this.value = reader.getBigDecimal();
            
            return parent;
        }
        
        @Override
        public Value getValue() {
            return this.context.getValueFactory().of( this.value.longValue());
        }
    }
    
    private static class DoubleValueImporter extends ValueImporter {
        
        private BigDecimal value;
        
        public DoubleValueImporter( ImporterContext context, Importer parent) {
            super( context, parent);
        }
        
        @Override
        public Importer valueNumber( JsonParser reader) {
            this.value = reader.getBigDecimal();
            
            return parent;
        }
        
        @Override
        public Value getValue() {
            return this.context.getValueFactory().of( this.value.doubleValue());
        }
    }
    
    private static class DecimalValueImporter extends ValueImporter {

        private BigDecimal value;
        
        public DecimalValueImporter( ImporterContext context, Importer parent) {
            super( context, parent);
        }
        
        @Override
        public Importer valueNumber( JsonParser reader) {
            this.value =reader.getBigDecimal();
            
            return parent;
        }
        
        @Override
        public Value getValue() {
            return this.context.getValueFactory().of( this.value);
        }
    }

    private static class BooleanValueImporter extends ValueImporter {
    
        private Boolean value;
        
        public BooleanValueImporter( ImporterContext context, Importer reader) {
            super( context, reader);
        }
    
        @Override
        public Importer valueTrue( JsonParser reader) {
            this.value = Boolean.valueOf( reader.getString());
            
            return parent;
        }

        @Override
        public Importer valueFalse( JsonParser reader) {
            this.value = Boolean.valueOf( reader.getString());
            
            return parent;
        }
        
        @Override
        public Value getValue() {
            return this.context.getValueFactory().of( Boolean.TRUE.equals( this.value));
        }
    }
    
    private static class StringImporter extends AbstractImporter {
        
        private String value;
        
        public StringImporter( ImporterContext context, Importer parent) {
            super( context, parent);
        }
        
        @Override
        public Importer valueString( JsonParser reader) {
            this.value = reader.getString();
            
            return this.parent;
        }
        
        public String getValue() {
            return this.value;
        }
    }
    
    private static class NumberImporter extends AbstractImporter {
        
        private BigDecimal value;
        
        public NumberImporter( ImporterContext context, Importer parent) {
            super( context, parent);
        }
        
        @Override
        public Importer valueNumber( JsonParser reader) {
            this.value = reader.getBigDecimal();
            
            return this.parent;
        }
        
        public BigDecimal getValue() {
            return this.value;
        }
    }
    
    private static class ReferenceValueImporter extends StringValueImporter {
        
        public ReferenceValueImporter( ImporterContext context, Importer parent) {
            super( context, parent);
        }

        @Override
        public Value getValue() {
            ValueFactory vf = this.context.getValueFactory();
            String ref = this.value;
            String[] parts = ref.split( ":");
            String workspaceName = parts[0];
            String path = parts[1];

            if( "<this>".equals( workspaceName)) {
                return vf.referenceByPath( vf.getSession().getWorkspace().getName(), path);
            }
            else {
                return vf.referenceByPath( workspaceName, path);
            }
        }
    }
    
    private static class BinaryValueImporter extends ValueImporter {
        
        private final StringImporter vict;
        private final StringImporter vifn;
        private final NumberImporter vil;
        private final StringImporter vib;
        private Value value;
        
        public BinaryValueImporter( ImporterContext context, Importer parent) {
            super( context, parent);
            
            this.vict = new StringImporter( context, this);
            this.vifn = new StringImporter( context, this);
            this.vil = new NumberImporter( context, this);
            this.vib = new StringImporter( context, this);
        }
        
        @Override
        public Importer startObject( JsonParser reader) {
            return this;
        }
        
        @Override
        public Importer keyName( JsonParser reader) {
            String name = reader.getString();
            
            switch( name) {
                case "contenttype":
                    return vict;
                case "filename":
                    return vifn;
                case "length":
                    return vil;
                case "binary":
                    return vib;
                default:
                    throw new IllegalImporterStateException( "Unexpected property <" + name + "> on binary value.", reader);
            }
        }
        
        @Override
        public Importer endObject( JsonParser reader) {
            String b = vib.getValue();

            try( InputStream in = Base64.getDecoder().wrap( new ByteArrayInputStream( b.getBytes( StandardCharsets.UTF_8)))) {
                this.value = this.context.getValueFactory().binary( in, vifn.getValue(), vict.getValue());
            }
            catch( IOException e) {
                logger.warn( "Could not read binary value form dump.");
                
                throw new IllegalImporterStateException( "Could not decode binary.", reader);
            }
            
            return this.parent;
        }
        
        @Override
        public Value getValue() {
            return this.value;
        }
        
        public String getContentType() {
            return vict.getValue();
        }
        
        public String getFilename() {
            return vifn.getValue();
        }
    }
    
    private static class LocalDateValueImporter extends StringValueImporter {
        
        public LocalDateValueImporter( ImporterContext context, Importer parent) {
            super( context, parent);
        }
        
        @Override
        public Value getValue() {
            return this.context.getValueFactory().of( LocalDate.from( DateTimeFormatter.ISO_LOCAL_DATE.parse( this.value)));
        }
    }
    
    private static class ZonedDateTimeValueImporter extends StringValueImporter {
        
        public ZonedDateTimeValueImporter( ImporterContext context, Importer parent) {
            super( context, parent);
        }
        
        @Override
        public Value getValue() {
            return this.context.getValueFactory().of( ZonedDateTime.from( DateTimeFormatter.ISO_ZONED_DATE_TIME.parse( this.value)));
        }
    }
    
    private static class ArrayValueImporter extends ValueImporter {
        
        private final List<Value> values = new ArrayList<>();
        
        public ArrayValueImporter( ImporterContext context, Importer parent) {
            super( context, parent);
        }
        
        @Override
        public Importer startArray( JsonParser parser) {
            return this;
        }
        
        @Override
        public Importer endArray( JsonParser parser) {
            return this.parent;
        }
        
        @Override
        public Importer startObject( JsonParser parser) {
            return new SimpleValueImporter( this.context, this, this::addValue);
        }
        
        private void addValue( ValueImporter importer) {
            values.add( importer.getValue());
        }
        
        @Override
        public Value getValue() {
            return this.context.getValueFactory().of( this.values);
        }
    }
    
    private static class MapValueImporter extends ValueImporter {
        
        private final Map<String, Value> values = new HashMap<>();
        private String name;
        
        public MapValueImporter( ImporterContext context, Importer parent) {
            super( context, parent);
        }
        
        @Override
        public Importer startObject( JsonParser parser) {
            return this;
        }
        
        @Override
        public Importer keyName( JsonParser parser) {
            this.name = parser.getString();
            
            return new SimpleValueImporter( this.context, this, this::addValue);
        }
        
        @Override
        public Importer endObject( JsonParser parser) {
            return this.parent;
        }
        
        private void addValue( ValueImporter importer) {
            this.values.put( this.name, importer.getValue());
        }
        
        @Override
        public Value getValue() {
            return this.context.getValueFactory().of( this.values);
        }
    }
    
    private static class SimpleValueImporter extends AbstractImporter {
        private final TypeImporter ti;
        private ValueImporter vi;
        private final Consumer<ValueImporter> adder;
        
        public SimpleValueImporter( ImporterContext context, Importer parent, Consumer<ValueImporter> adder) {
            super( context, parent);
            
            this.ti = new TypeImporter( context, this);
            this.adder = adder;
        }
        
        @Override
        public Importer startObject( JsonParser parser) {
            return this;
        }
        
        @Override
        public Importer keyName( JsonParser reader) {
            String name = reader.getString();
            
            switch( name) {
                case "type":
                    return ti;
                case "value":
                    this.vi = createValueImporter( this.context, ti.getType(), this);
                    
                    return vi;
                default:
                    throw new IllegalImporterStateException( "Unknown property <" + name + "> on property.", reader);
            }
        }
        
        @Override
        public Importer endObject( JsonParser parser) {
            adder.accept( this.vi);
            
            return this.parent;
        }
        
        public Value getValue() {
            return vi.getValue();
        }
    }
    
    private static class PropertyImporter extends AbstractImporter {
        private final Node node;
        private final String name;
        
        private ValueImporter vi;
        private final TypeImporter ti;
        
        public PropertyImporter( ImporterContext context, Node node, String name, Importer parent) {
            super( context, parent);
            
            this.ti = new TypeImporter( context, this);
            this.node = node;
            this.name = name;
        }
        
        @Override
        public Importer startObject( JsonParser reader) {
            return this;
        }
        
        @Override
        public Importer keyName( JsonParser reader) {
            String propertyname = reader.getString();
            
            switch( propertyname) {
                case "type":
                    return ti;
                case "value":
                    vi = createValueImporter( this.context, ti.getType(), this);
                    return vi;
                default:
                    throw new IllegalImporterStateException( "Unexpected name <" + name + "> on property.", reader);
            }
        }
        
        @Override
        public Importer endObject(JsonParser reader) {
            this.node.setProperty( this.name, vi.getValue());

            return this.parent;
        }
    }

    private static ValueImporter createValueImporter( ImporterContext context, Value.Type type, Importer parent) {
        switch( type) {
            case STRING:
                return new StringValueImporter( context, parent);
            case LONG:
                return new LongValueImporter( context, parent);
            case DOUBLE:
                return new DoubleValueImporter( context, parent);
            case DECIMAL:
                return new DecimalValueImporter( context, parent);
            case BOOLEAN:
                return new BooleanValueImporter( context, parent);
            case REFERENCE:
                return new ReferenceValueImporter( context, parent);
            case LOCAL_DATE:
                return new LocalDateValueImporter( context, parent);
            case ZONED_DATETIME:
                return new ZonedDateTimeValueImporter( context, parent);
            case BINARY:
                return new BinaryValueImporter( context, parent);
            case ARRAY:
                return new ArrayValueImporter( context, parent);
            case MAP:
                return new MapValueImporter( context, parent);
            default:
                throw new IllegalImporterStateException( "Unsupported value type <" + type + ">.");
        }
    }
}
