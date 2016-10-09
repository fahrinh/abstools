/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.backend.go;

import abs.frontend.ast.ASTNode;

@SuppressWarnings("serial")
public class GoBackendException extends RuntimeException {
    public GoBackendException(String msg) {
        super("An exception in the Go backend of ABS occurred: " + msg);
    }

    public GoBackendException(ASTNode<?> node, String msg) {
        super("An exception in the Go backend of ABS occurred: " + node.getStartLine() + ":"
                + node.getStartColumn() + ": " + msg);
    }

}
