/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved.
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.frontend.delta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import abs.frontend.analyser.ErrorMessage;
import abs.frontend.analyser.SemanticErrorList;
import abs.frontend.analyser.TypeError;
import abs.frontend.ast.*;

public class ProgramTypeAbstraction {
    private final Map<String, Map<String, Set<String>>> classes;
    private final SemanticErrorList errors;

    // Constructor
    public ProgramTypeAbstraction(SemanticErrorList errors) {
        this.errors = errors;
        classes = new HashMap<String, Map<String, Set<String>>>();
    }

    // Copy constructor
    public ProgramTypeAbstraction(ProgramTypeAbstraction sourceTA) {
        this.errors = sourceTA.errors;
        classes = new HashMap<String, Map<String, Set<String>>>();
        for (String className : sourceTA.classes.keySet()) {
            addClass(className);
            for (String name : sourceTA.classes.get(className).get("fields"))
                classes.get(className).get("fields").add(name);
            for (String name : sourceTA.classes.get(className).get("methods"))
                classes.get(className).get("methods").add(name);
        }
    }


    public void applyDelta(DeltaDecl delta) {
        for (ModuleModifier mod : delta.getModuleModifiers()) {
            mod.applyToTypeAbstraction(this);
        }
    }

    public void addClass(AddClassModifier node) {
        String className = node.qualifiedName();
        if (classes.containsKey(className))
            errors.add(new TypeError(node, ErrorMessage.DUPLICATE_CLASS_NAME, className));
        else
            addClass(className);
    }
    private void addClass(String className) {
        classes.put(className, new HashMap<String, Set<String>>());
        classes.get(className).put("fields", new HashSet<String>());
        classes.get(className).put("methods", new HashSet<String>());
    }

    public void removeClass(RemoveClassModifier node) {
        String className = node.qualifiedName();
        if (classes.containsKey(className))
            classes.remove(className);
        else
            errors.add(new TypeError(node, ErrorMessage.NO_CLASS_DECL, className));
    }

    public void addField(String className, AddFieldModifier node) {
        String name = node.getFieldDecl().getName();
        if (! classes.get(className).get("fields").contains(name))
            classes.get(className).get("fields").add(name);
        else
            errors.add(new TypeError(node, ErrorMessage.DUPLICATE_FIELD_NAME, name));
    }

    public void removeField(String className, RemoveFieldModifier node) {
        String name = node.getFieldDecl().getName();
        if (classes.get(className).get("fields").contains(name))
            classes.get(className).get("fields").remove(name);
        else
            errors.add(new TypeError(node, ErrorMessage.NO_FIELD_DECL, name));
    }

    public void addMethod(String className, AddMethodModifier node) {
        String name = node.getMethodImpl().getMethodSig().getName();
        if (! classes.get(className).get("methods").contains(name))
            classes.get(className).get("methods").add(name);
        else
            errors.add(new TypeError(node, ErrorMessage.DUPLICATE_METHOD_NAME, name));
    }

    public void modifyMethod(String className, AddMethodModifier node) {
        String name = node.getMethodImpl().getMethodSig().getName();
        if (! classes.get(className).get("methods").contains(name))
            errors.add(new TypeError(node, ErrorMessage.NO_METHOD_IMPL, name));  // FIXME Error message probably not suitable
    }

    public void removeMethod(String className, RemoveMethodModifier node) {
        String name = node.getMethodSig().getName();
        if (classes.get(className).get("methods").contains(name))
            classes.get(className).get("methods").remove(name);
        else
            errors.add(new TypeError(node, ErrorMessage.NO_METHOD_IMPL, name));  // FIXME Error message probably not suitable
    }


    // helper method
    public boolean existsClass(ModifyClassModifier node) {
        String className = node.qualifiedName();
        if (classes.containsKey(className)) {
            return true;
        } else {
            errors.add(new TypeError(node, ErrorMessage.NO_CLASS_DECL, className));
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (String cls : classes.keySet()) {
            s.append(cls + ";");
        }
        return "<<" + s.toString() + ">>";
    }
}
