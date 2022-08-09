package org.mooner.inventoryshare.exception;

import java.io.IOException;

public class RefreshError extends IOException {
    public RefreshError(Throwable a) {
        super(a.getMessage());
    }

    public RefreshError(String s) {
        super(s);
    }
}
