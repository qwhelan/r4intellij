/*
 * Copyright 2011 Holger Brandl
 *
 * This code is licensed under BSD. For details see
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.r4intellij.psi;

import java.util.List;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;


public interface RExprlist extends RCompositeElement {

    @Nullable
    public RExprOrAssign getExprOrAssign();

    @Nullable
    public PsiElement getWhitespace();

}
