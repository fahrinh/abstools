/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.backend.go;

public class GoCodeGenerationException extends Exception {
    private static final long serialVersionUID = 4444515759094137086L;

    public GoCodeGenerationException(String message) {
        super(message);
    }

    public GoCodeGenerationException(Throwable t) {
        super(t);
    }

    public GoCodeGenerationException(String m, Throwable t) {
        super(m,t);
    }
}
