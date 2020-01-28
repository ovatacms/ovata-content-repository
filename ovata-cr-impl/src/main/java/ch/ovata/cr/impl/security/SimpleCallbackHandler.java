/*
 * $Id: SimpleCallbackHandler.java 679 2017-02-25 10:28:00Z dani $
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

import java.io.IOException;
import java.util.Arrays;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 *
 * @author dani
 */
public class SimpleCallbackHandler implements CallbackHandler {

    private final String username;
    private final char[] password;
    
    public SimpleCallbackHandler( String username, char[] password) {
        this.username = username;
        this.password = Arrays.copyOf( password, password.length);
    }
    
    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for( Callback callback : callbacks) {
            
            if( callback instanceof NameCallback) {
                NameCallback nameCallback = (NameCallback) callback;
                nameCallback.setName( username);
            } 
            else if( callback instanceof PasswordCallback) {
                PasswordCallback passwordCallback = (PasswordCallback) callback;
                passwordCallback.setPassword( password );
            } 
            else {
                throw new UnsupportedCallbackException( callback, "The submitted callback is not supported.");
            }
        }
    }
}
