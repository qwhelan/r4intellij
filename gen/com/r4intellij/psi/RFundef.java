/*
 * Copyright 2012 Holger Brandl
 *
 * This code is licensed under BSD. For details see
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.r4intellij.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RFundef extends RCompositeElement {

    @NotNull
    RExprOrAssign getExprOrAssign();


    @Nullable
    RFundefArgs getFundefArgs();

}
