/*
 * $Id: PasswordChecker.java 679 2017-02-25 10:28:00Z dani $
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
package ch.ovata.cr.tools;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.FailedLoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public final class PasswordChecker {
    
    private final Map<String, PasswordVerifier> verifiers = new HashMap<>();
    
    private static final Logger logger = LoggerFactory.getLogger( PasswordChecker.class);
    private static final PasswordChecker instance = new PasswordChecker();
    
    public static PasswordChecker instance() {
        return instance;
    }
    
    private PasswordChecker() {
        verifiers.put( "plain", new PlainTextPasswordVerifier());
        verifiers.put( "md5", new MD5PasswordVerifier());
        verifiers.put( "sha1", new SHA1PasswordVerifier());
        verifiers.put( "sha256", new SHA256PasswordVerifier());
        verifiers.put( "sha512", new SHA512PasswordVerifier());
    }
    
    public boolean verifyPassword( String password, String token) throws FailedLoginException {
        Pattern p = Pattern.compile( "\\{(.*)\\}(.*)");
        Matcher m = p.matcher( token);
        
        if( m.matches()) {
            String algo = m.group( 1);
            String bytes = m.group( 2);
            
            PasswordVerifier verifier = this.verifiers.get( algo);
            
            if( verifier != null) {
                return verifier.verify( password, bytes);
            }
        }
        
        throw new FailedLoginException( "Password could not be verified.");
    }
    
    public String generateToken( String algo, String password) throws CredentialException {
        PasswordVerifier verifier = this.verifiers.get( algo);
        
        if( verifier == null) {
            throw new CredentialException( "Specified algorithm <" + algo + "> unknown.");
        }
        
        return "{" + algo + "}" + verifier.generateToken( password);
    }
    
    interface PasswordVerifier {
        String getAlgoName();
        boolean verify( String password, String token);
        String generateToken( String password);
    }
    
    public static class PlainTextPasswordVerifier implements PasswordVerifier {

        @Override
        public String getAlgoName() {
            return "plain";
        }

        @Override
        public boolean verify(String password, String token) {
            return password.equals( token);
        }

        @Override
        public String generateToken(String password) {
            return password;
        }
    }
    
    public static class AbstractHashPasswordVerifier implements PasswordVerifier {

        private final String javaAlgorithm;
        
        protected AbstractHashPasswordVerifier( String javaAlgorithm) {
            this.javaAlgorithm = javaAlgorithm;
        }
        
        @Override
        public String getAlgoName() {
            return "sha256";
        }

        @Override
        public boolean verify(String password, String token) {
            try {
                MessageDigest digest = MessageDigest.getInstance( javaAlgorithm);
                byte[] hash = digest.digest( password.getBytes(StandardCharsets.UTF_8));
                String hash64 = Base64.getEncoder().encodeToString( hash);
                
                return hash64.equals( token);
            } 
            catch (NoSuchAlgorithmException e) {
                logger.warn( "Could not verify password.", e);
                return false;
            }
        }

        @Override
        public String generateToken(String password) {
            try {
                MessageDigest digest = MessageDigest.getInstance( javaAlgorithm);
                byte[] hash = digest.digest( password.getBytes(StandardCharsets.UTF_8));
                
                return Base64.getEncoder().encodeToString( hash);
            } 
            catch (NoSuchAlgorithmException e) {
                logger.warn( "Could not verify password.", e);
                return "";
            }
        }
    }
    
    public static class SHA256PasswordVerifier extends AbstractHashPasswordVerifier {
        
        public SHA256PasswordVerifier() {
            super( "SHA-256");
        }
    }
    
    public static class MD5PasswordVerifier extends AbstractHashPasswordVerifier {

        public MD5PasswordVerifier() {
            super( "MD5");
        }
    }
    
    public static class SHA1PasswordVerifier extends AbstractHashPasswordVerifier {
        
        public SHA1PasswordVerifier() {
            super( "SHA-1");
        }
    }
    
    public static class SHA512PasswordVerifier extends AbstractHashPasswordVerifier {
        
        public SHA512PasswordVerifier() {
            super( "SHA-512");
        }
    }
}
